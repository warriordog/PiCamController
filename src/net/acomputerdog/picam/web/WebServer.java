package net.acomputerdog.picam.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.acomputerdog.picam.PiCamController;
import net.acomputerdog.picam.file.H264File;
import net.acomputerdog.picam.file.JPGFile;

import java.io.*;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

public class WebServer {
    private final PiCamController controller;
    private final HttpServer server;

    public WebServer(PiCamController controller) throws IOException {
        this.controller = controller;

        // who even knows what that 0 is for
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", e -> redirect("/html/main.html", e));
        server.createContext("/html/", this::sendWebFile);
        server.createContext("/img/", this::sendWebFile);
        server.createContext("/include/", this::sendWebFile);
        server.createContext("/func/", e -> sendResponse("404 Unknown function", 404, e));
        server.createContext("/func/exit", e -> {
            sendResponse("200 OK", 200, e);
            controller.shutdown();
        });
        server.createContext("/func/version", e -> sendResponse("Pi Camera Controller v0.1.0", 200, e));
        server.createContext("/func/status", e -> {
            String resp = controller.getCamera(0).isRecording() ? "1" : "0";
            resp += "|";
            resp += controller.getCamera(0).getRecordingPath();
            resp += "|";
            resp += controller.getCamera(0).getRecordingTime();
            sendResponse(resp, 200, e);
        });
        server.createContext("/func/record", e -> {
            if ("POST".equals(e.getRequestMethod())) {
                if (!controller.getCamera(0).isRecording()) {
                    String request = new BufferedReader(new InputStreamReader(e.getRequestBody())).lines().collect(Collectors.joining());

                    String[] parts = request.split("\\|");
                    if (parts.length == 2 || parts.length == 3) {
                        try {
                            int time = Integer.parseInt(parts[0]) * 1000;
                            String fileName = parts[1].replace('.', '_').replace('/', '_').replace('~', '_');

                            // workaround for bug where a string that ends with a delimiter is not properly split
                            String[] settings = parts.length == 3 ? parts[2].split(" ") : new String[0];

                            controller.getCamera(0).getVidSettings().addSettingPairs(settings);
                            controller.getCamera(0).recordFor(time, new H264File(controller.getVidDir(), fileName));

                            sendResponse("200 OK", 200, e);
                        } catch (NumberFormatException ex) {
                            sendResponse("400 Malformed Input: time must be an integer", 400, e);
                        }

                    } else {
                        sendResponse("400 Malformed Input: wrong number of arguments", 400, e);
                    }
                } else {
                    sendResponse("409 Conflict: Already recording.", 409, e);
                }
            } else {
                sendResponse("405 Method Not Allowed: use POST", 405, e);
            }
        });
        server.createContext("/func/record_stop", e -> {
            sendResponse("200 OK", 200, e);
            controller.getCamera(0).stop();
        });
        server.createContext("/func/snapshot", e -> {
            if ("POST".equals(e.getRequestMethod())) {
                String request = new BufferedReader(new InputStreamReader(e.getRequestBody())).lines().collect(Collectors.joining());
                String fileName = request.replace('.', '_').replace('/', '_').replace('~', '_');
                JPGFile file = new JPGFile(controller.getPicDir(), fileName);
                controller.getCamera(0).takeSnapshot(file);

                sendResponse("200 OK", 200, e);
            } else {
                sendResponse("405 Method Not Allowed: use POST", 405, e);
            }
        });
        server.createContext("/func/download", e -> {
            if ("GET".equals(e.getRequestMethod())) {
                String request = e.getRequestURI().getQuery();
                int eIdx = request.indexOf('=');
                if (eIdx > -1 && request.length() - eIdx > 1) {
                    String resourceType = request.substring(0, eIdx);
                    String resourcePath = request.substring(eIdx + 1);
                    if (!resourcePath.contains("/") && !resourcePath.contains("\\")) {
                        File dir = null;
                        String contentType = "";

                        if ("v".equals(resourceType)) {
                            dir = controller.getVidDir();
                            contentType = "video/mp4";
                        } else if ("p".equals(resourceType)) {
                            dir = controller.getPicDir();
                            contentType = "image/jpeg";
                        }

                        if (dir != null) {
                            File file = new File(dir, resourcePath);
                            if (file.isFile()) {
                                try {
                                    InputStream in = new FileInputStream(file);

                                    e.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + resourcePath + "\"");
                                    e.getResponseHeaders().add("Content-Type", contentType);

                                    sendFile(e, in);
                                } catch (FileNotFoundException ex) {
                                    sendResponse("404 Not Found: Filesystem error", 404, e);
                                } catch (IOException ex) {
                                    System.err.println("IO error while sending file");
                                    ex.printStackTrace();
                                }
                            } else {
                                sendResponse("404 Not Found: No file could be found by that name", 404, e);
                            }
                        } else {
                            sendResponse("400 Malformed Input: unknown resource type", 400, e);
                        }
                    } else {
                        sendResponse("400 Malformed Input: resource path is invalid", 400, e);
                    }
                } else {
                    sendResponse("400 Malformed Input: resource type not specified", 400, e);
                }
            } else {
                sendResponse("405 Method Not Allowed: use GET", 405, e);
            }
        });
        server.createContext("/func/listfiles", e -> {
            String request = e.getRequestURI().getQuery();

            File dir = null;
            if ("v".equals(request)) {
                dir = controller.getVidDir();
            } else if ("p".equals(request)) {
                dir = controller.getPicDir();
            }

            if (dir != null) {
                StringBuilder resp = new StringBuilder();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                File[] files = dir.listFiles();
                if (files != null) {
                    // sort by modified time
                    Arrays.sort(files, (o1, o2) -> (int) (o2.lastModified() - o1.lastModified()));

                    for (int i = 0; i < files.length; i++) {
                        if (i > 0) {
                            resp.append('|');
                        }
                        File file = files[i];
                        resp.append(file.getName());
                        resp.append(',');
                        resp.append(formatFileSize(file.length()));
                        resp.append(',');
                        resp.append(dateFormat.format(file.lastModified()));
                    }
                }
                sendResponse(resp.toString(), 200, e);
            } else {
                sendResponse("400 Malformed Input: missing arguments", 400, e);
            }
        });
        server.createContext("/func/delete", e -> {
            if ("POST".equals(e.getRequestMethod())) {
                String request = new BufferedReader(new InputStreamReader(e.getRequestBody())).lines().collect(Collectors.joining());
                int split = request.indexOf('|');
                if (split > 0 && request.length() - split > 1) {
                    String type = request.substring(0, split);
                    File dir = null;
                    if ("v".equals(type)) {
                        dir = controller.getVidDir();
                    } else if ("p".equals(type)) {
                        dir = controller.getPicDir();
                    }

                    if (dir != null) {
                        String path = request.substring(split + 1);
                        if (!path.contains("/") && !path.contains("\\")) {
                            File file = new File(dir, path);
                            if (file.exists()) {
                                if (file.delete()) {
                                    sendResponse("200 OK", 200, e);
                                } else {
                                    System.out.printf("Unable to delete file '%s'\n", file.getPath());
                                    sendResponse("500 Internal Error: unable to delete file", 500, e);
                                }
                            } else {
                                sendResponse("400 Malformed Input: file does not exist", 400, e);
                            }
                        } else {
                            sendResponse("400 Malformed Input: invalid file name", 400, e);
                        }
                    } else {
                        sendResponse("400 Malformed Input: unknown resource type", 400, e);
                    }
                } else {
                    sendResponse("400 Malformed Input: missing arguments", 400, e);
                }
            } else {
                sendResponse("405 Method Not Allowed: use POST", 405, e);
            }
        });
        server.createContext("/func/lastsnap", e -> {
            File snap = controller.getCamera(0).getLastSnapshot();
            if (snap != null && snap.exists()) {
                try {
                    InputStream in = new FileInputStream(snap);
                    e.getResponseHeaders().add("Content-Type", "image/jpeg");
                    e.getResponseHeaders().add("Cache-control", "no-cache");
                    sendFile(e, in);
                } catch (FileNotFoundException ex) {
                    sendResponse("404 Not Found: previous snapshot has been deleted.", 404, e);
                }
            } else {
                sendResponse("503 Temporarily Unavailable: snapshot is not ready", 503, e);
            }
        });
        server.createContext("/func/check_snap", e -> {
            if (controller.getCamera(0).snapshotAvailable()) {
                sendResponse("200 OK: Snapshot available", 200, e);
            } else {
                sendResponse("202 Accepted: waiting for snapshot", 202, e);
            }
        });
    }

    public void start() {
        server.start();
    }

    private void redirect(String path, HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Location", path);
        sendResponse("308 Permanent Redirect", 308, exchange);
    }

    private void sendWebFile(HttpExchange exchange) throws IOException {
        String fullPath = "/web" + exchange.getRequestURI().getPath().replace("..", "");
        InputStream in = getClass().getResourceAsStream(fullPath);

        if (in == null) {
            sendResponse("404 not found", 404, exchange);
        } else {
            sendFile(exchange, in);
        }
    }

    private void sendFile(HttpExchange exchange, InputStream in) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        byte[] buff = new byte[64];

        // autocloses out
        try (OutputStream out = exchange.getResponseBody();) {

            while (in.available() > 0) {
                int count = in.read(buff);
                out.write(buff, 0, count);
            }
        }
        exchange.close();
    }

    private void sendResponse(String response, int code, HttpExchange exchange) throws IOException {
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(code, bytes.length);
        try {
            exchange.getResponseBody().write(bytes);
        } finally {
            exchange.getResponseBody().close();
            exchange.close();
        }
    }

    private static String formatFileSize(long size) {
        String[] units = new String[]{"  B"," KB"," MB"," GB"," TB"," PB"," EB"};
        int scale = 0;

        long leftOver = size;
        while (leftOver > 1024) {
            // cut short when we have more than 1024 exobytes
            if (scale >= units.length) {
                break;
            }

            leftOver = leftOver >> 10;
            scale++;
        }

        return leftOver + units[scale];
    }
}
