package net.acomputerdog.picam.web;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.acomputerdog.picam.PiCamController;
import net.acomputerdog.picam.camera.setting.Setting;
import net.acomputerdog.picam.camera.setting.Settings;
import net.acomputerdog.picam.file.H264File;
import net.acomputerdog.picam.file.JPGFile;
import net.acomputerdog.picam.system.Power;
import net.acomputerdog.picam.util.H264Converter;
import net.acomputerdog.picam.util.OutputStreamSplitter;
import net.acomputerdog.picam.web.handler.BasicWebHandler;
import net.acomputerdog.picam.web.handler.SimpleWebHandler;
import net.acomputerdog.picam.web.handler.WebFileHandler;
import net.acomputerdog.picam.web.handler.WebHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WebServer {
    // 30 seconds
    private static final int SPEED_TEST_TIMEOUT = 30000;

    private final PiCamController controller;
    private final HttpServer server;

    public WebServer(PiCamController controller) throws IOException {
        this.controller = controller;

        // zero means "use system default value for maximum number of backlogged requests"
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);

        // web file handlers
        server.createContext("/", new SimpleWebHandler((h, ex) -> h.redirect("/html/main.html", ex)));
        server.createContext("/html/", new WebFileHandler());
        server.createContext("/img/", new WebFileHandler());
        server.createContext("/include/", new WebFileHandler());

        // general functions
        server.createContext("/func/", new SimpleWebHandler((h, ex) -> h.sendResponse("404 Unknown function", 404, ex)));

        // admin functions
        server.createContext("/func/admin", new SimpleWebHandler((h, ex) -> h.sendResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/admin/exit", new BasicWebHandler(controller::exit));
        server.createContext("/func/admin/version", new SimpleWebHandler((h, ex) -> h.sendResponse(controller.getVersionString(), 200, ex)));
        server.createContext("/func/admin/reboot", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                // don't use a simpler handler, because response must be sent before controller shuts down
                sendResponse("200 OK", 200, e);
                Power.reboot();
                controller.exit();
            }
        });
        server.createContext("/func/admin/exit", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                // don't use a simpler handler, because response must be sent first
                sendResponse("200 OK", 200, e);
                controller.exit();
            }
        });
        server.createContext("/func/admin/shutdown", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                // don't use a simpler handler, because response must be sent before controller shuts down
                sendResponse("200 OK", 200, e);
                Power.shutdown();
                controller.exit();
            }
        });

        // filesystem functions
        server.createContext("/func/admin/fs", new SimpleWebHandler((h, ex) -> h.sendResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/admin/fs/mount", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                if ("1".equals(getData)) {
                    controller.getFS().mountRW();
                    sendResponse("200 OK", 200, e);
                } else if ("0".equals(getData)) {
                    controller.getFS().mountRO();
                    sendResponse("200 OK", 200, e);
                } else {
                    sendResponse("400 Malformed Input: mode must be 1 or 0", 400, e);
                }

            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "GET".equals(e.getRequestMethod());
            }
        });
        server.createContext("/func/admin/fs/sync", new BasicWebHandler(() -> controller.getFS().sync()));

        // recording functions
        server.createContext("/func/record", new SimpleWebHandler((h, ex) -> h.sendResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/record/status", new SimpleWebHandler((h, ex) -> {
            String resp = controller.getCamera(0).isRecording() ? "1" : "0";
            resp += "|";
            resp += controller.getCamera(0).getRecordingPath();
            resp += "|";
            resp += controller.getCamera(0).getRecordingTime();
            h.sendResponse(resp, 200, ex);
        }));
        server.createContext("/func/record/video", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                if (!controller.getCamera(0).isRecording()) {
                    String[] parts = postData.split("\\|");
                    if (parts.length == 2) {
                        try {
                            int time = Integer.parseInt(parts[0]) * 1000;
                            String fileName = parts[1].replace('.', '_').replace('/', '_').replace('~', '_');

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
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "POST".equals(e.getRequestMethod());
            }
        });
        server.createContext("/func/record/stop", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                // don't use a simpler handler because this must be out of order
                sendResponse("200 OK", 200, e);
                controller.getCamera(0).stop();
            }
        });
        server.createContext("/func/record/snapshot", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                String fileName = postData.replace('.', '_').replace('/', '_').replace('~', '_');
                JPGFile file = new JPGFile(controller.getPicDir(), fileName);
                controller.getCamera(0).takeSnapshot(file);

                sendResponse("200 OK", 200, e);
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "POST".equals(e.getRequestMethod());
            }
        });

        // media settings
        server.createContext("/func/media", new SimpleWebHandler((h, ex) -> h.sendResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/media/download", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                int eIdx = getData.indexOf('=');
                if (eIdx > -1 && getData.length() - eIdx > 1) {
                    String resourceType = getData.substring(0, eIdx);
                    String resourcePath = getData.substring(eIdx + 1);
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
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "GET".equals(e.getRequestMethod());
            }
        });
        server.createContext("/func/media/list", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {

                File dir = null;
                if ("v".equals(getData)) {
                    dir = controller.getVidDir();
                } else if ("p".equals(getData)) {
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
            }
        });
        server.createContext("/func/media/delete", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                int split = postData.indexOf('|');
                if (split > 0 && postData.length() - split > 1) {
                    String type = postData.substring(0, split);
                    File dir = null;
                    if ("v".equals(type)) {
                        dir = controller.getVidDir();
                    } else if ("p".equals(type)) {
                        dir = controller.getPicDir();
                    }

                    if (dir != null) {
                        String path = postData.substring(split + 1);
                        if (!path.contains("/") && !path.contains("\\")) {
                            File file = new File(dir, path);
                            if (file.exists()) {
                                if (file.delete()) {
                                    // cached streaming file
                                    File streamFile = new File(controller.getStreamDir(), path + ".mp4");
                                    if (streamFile.isFile()) {
                                        if (streamFile.delete()) {
                                            sendResponse("200 OK", 200, e);
                                        } else {
                                            System.out.printf("Unable to delete stream cache '%s'\n", file.getPath());
                                            sendResponse("500 Internal Error: unable to cache file", 500, e);
                                        }
                                    } else {
                                        sendResponse("200 OK", 200, e);
                                    }
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
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "POST".equals(e.getRequestMethod());
            }
        });
        server.createContext("/func/media/lastsnap", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                if (controller.getCamera(0).currentSnapshotReady()) {
                    String snapName = controller.getCamera(0).getLastSnapshot().getName();
                    sendResponse(snapName, 200, e);
                } else {
                    sendResponse("202 Accepted: waiting for snapshot", 202, e);
                }
            }
        });
        server.createContext("/func/media/stream", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                if (!getData.contains("/") && !getData.contains("\\")) {
                    File vidFile = new File(controller.getVidDir(), getData);
                    if (vidFile.isFile()) {
                        File streamFile = new File(controller.getStreamDir(), getData + ".mp4");

                        InputStream streamIn = null;
                        OutputStream fileOut = null;
                        OutputStream streamOut = null;
                        try {
                            if (streamFile.isFile()) {
                                //System.err.println("Streaming from cache: " + request);
                                streamIn = new FileInputStream(streamFile);
                                fileOut = null;
                            } else {
                                //System.err.println("Streaming in realtime: " + request);
                                streamIn = new H264Converter(controller.getVidDir(), controller.getTmpDir(), getData);
                                fileOut = new FileOutputStream(streamFile);
                            }

                            e.getResponseHeaders().add("Content-Disposition", "filename=\"" + getData + "\"");
                            e.getResponseHeaders().add("Content-Type", "video/mp4");
                            e.sendResponseHeaders(200, 0); // chunked encoding, no size

                            if (fileOut != null) {
                                streamOut = new OutputStreamSplitter(fileOut, e.getResponseBody());
                            } else {
                                streamOut = e.getResponseBody();
                            }

                            byte[] buff = new byte[128];
                            // H264 converter will always return at least 1 while process is active
                            while (streamIn.available() > 0) {
                                int count = streamIn.read(buff);
                                // stop if end of stream
                                if (count == -1) {
                                    break;
                                }
                                streamOut.write(buff, 0, count);
                            }
                        } catch (FileNotFoundException ex) {
                            sendResponse("404 Not Found: Filesystem error", 404, e);
                        } catch (IOException ex) {
                            System.err.println("IO error while sending file");
                            ex.printStackTrace();
                        } finally {
                            if (streamIn != null) {
                                streamIn.close();
                            }
                            if (streamOut != null) {
                                streamOut.flush();
                                streamOut.close();
                            }
                            if (fileOut != null) {
                                fileOut.flush();
                                fileOut.close();
                            }
                            e.close();
                        }
                    } else {
                        sendResponse("404 Not Found: No file could be found by that name", 404, e);
                    }
                } else {
                    sendResponse("400 Malformed Input: resource path is invalid", 400, e);
                }
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "GET".equals(e.getRequestMethod());
            }
        });

        // settings and config
        server.createContext("/func/settings", new SimpleWebHandler((h, ex) -> h.sendResponse("404 Unknown function", 404, ex)));
        // camera settings
        server.createContext("/func/settings/camera", new SimpleWebHandler((h, ex) -> h.sendResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/settings/camera/get", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                Settings settings = null;
                if ("v".equals(getData)) {
                    settings = controller.getCamera(0).getVidSettings();
                } else if ("p".equals(getData)) {
                    settings = controller.getCamera(0).getPicSettings();
                }

                if (settings != null) {
                    StringBuilder builder = new StringBuilder();
                    List<Setting> allSettings = settings.getList().getAllSettings();
                    for (int i = 0; i < allSettings.size(); i++) {
                        if (i > 0) {
                            builder.append("|");
                        }
                        Setting setting = allSettings.get(i);
                        builder.append(setting.getKey());
                        builder.append("=");
                        // an empty value means null
                        if (setting.isIncluded()) {
                            builder.append(setting.getValue());
                        }
                    }
                    sendResponse(builder.toString(), 200, e);
                } else {
                    sendResponse("400 Malformed Input: unknown resource type", 400, e);
                }
            }
        });
        server.createContext("/func/settings/camera/set", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                int split = postData.indexOf('&');
                if (split > 0 && postData.length() - split > 1) {

                    String type = postData.substring(0, split);
                    if ("v".equals(type)) {
                        controller.getCamera(0).getVidSettings().addSettingPairs(postData.substring(split + 1).split(" "));
                        sendResponse("200 OK", 200, e);
                    } else if ("p".equals(type)) {
                        controller.getCamera(0).getPicSettings().addSettingPairs(postData.substring(split + 1).split(" "));
                        sendResponse("200 OK", 200, e);
                    } else {
                        sendResponse("400 Malformed Input: unknown resource type", 400, e);
                    }
                }
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "POST".equals(e.getRequestMethod());
            }
        });
        server.createContext("/func/settings/camera/reset", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                if ("v".equals(getData)) {
                    controller.getCamera(0).resetVidSettings();
                    sendResponse("200 OK", 200, e);
                } else if ("p".equals(getData)) {
                    controller.getCamera(0).resetPicSettings();
                    sendResponse("200 OK", 200, e);
                } else {
                    sendResponse("400 Malformed Input: unknown resource type", 400, e);
                }
            }
        });
        //TODO find or add camera "set" function

        // system settings
        server.createContext("/func/settings/system", new SimpleWebHandler((h, ex) -> h.sendResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/settings/system/apply", new BasicWebHandler(() -> {
            controller.getNetwork().backupNetSettings();
            controller.getNetwork().applyNetworkSettings();
        }));
        server.createContext("/func/settings/system/set", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                try {
                    controller.updateConfig(postData);
                    sendResponse("200 OK", 200, e);
                } catch (JsonSyntaxException ex) {
                    sendResponse("400 Malformed Input: invalid json: " + ex.getMessage(), 400, e);
                }
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "POST".equals(e.getRequestMethod());
            }
        });
        server.createContext("/func/settings/system/get", new SimpleWebHandler((h, ex) -> h.sendResponse(controller.getConfigJson(), 200, ex)));
        server.createContext("/func/settings/system/reset", new BasicWebHandler(controller::resetConfig));
        server.createContext("/func/settings/system/save", new BasicWebHandler(controller::saveConfig));

        // debug functions
        server.createContext("/func/debug", new SimpleWebHandler((h, ex) -> h.sendResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/debug/speedtest", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                int size = 64;
                if (!getData.isEmpty()) {
                    try {
                        int newSize = Integer.parseInt(getData);
                        if (newSize > 0 && newSize <= 1024) {
                            size = newSize;
                        } else {
                            sendResponse("400 Malformed Input: size must be 1 - 1024", 400, e);
                        }
                    } catch (NumberFormatException ex) {
                        sendResponse("400 Malformed Input: size must be an integer", 400, e);
                    }
                }

                e.getResponseHeaders().set("Content-Type", "application/octet-stream");
                e.sendResponseHeaders(200, 0);
                Random random = new Random();
                OutputStream out = e.getResponseBody();
                long timeStart = System.currentTimeMillis();
                long bytes = 0;
                try {
                    byte[] buff = new byte[size];
                    while (System.currentTimeMillis() - timeStart < SPEED_TEST_TIMEOUT) {
                        random.nextBytes(buff);
                        out.write(buff);
                        bytes += size;
                    }
                } catch (IOException ignored) {
                    long timeEnd = System.currentTimeMillis();
                    long totalTime = timeEnd - timeStart;
                    long seconds = totalTime / 1000L;
                    long kilobytes = bytes >> 10;
                    System.out.printf("Speed test finished, rate was %.2f KBps\n", (double)kilobytes / (double)seconds);
                } finally {
                    e.close();
                }
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "GET".equals(e.getRequestMethod());
            }
        });
    }

    public void start() {
        server.start();
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
