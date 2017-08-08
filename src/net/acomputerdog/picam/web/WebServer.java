package net.acomputerdog.picam.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.acomputerdog.picam.PiCamController;
import net.acomputerdog.picam.camera.setting.PicSettings;
import net.acomputerdog.picam.camera.setting.Settings;
import net.acomputerdog.picam.camera.setting.VidSettings;
import net.acomputerdog.picam.file.H264File;
import net.acomputerdog.picam.file.JPGFile;
import net.acomputerdog.picam.system.Power;
import net.acomputerdog.picam.util.H264Converter;
import net.acomputerdog.picam.util.OutputStreamSplitter;
import net.acomputerdog.picam.web.handler.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
        server.createContext("/func/", new SimpleWebHandler((h, ex) -> h.sendSimpleResponse("404 Unknown function", 404, ex)));

        // admin functions
        server.createContext("/func/admin", new SimpleWebHandler((h, ex) -> h.sendSimpleResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/admin/exit", new BasicWebHandler(controller::exit));
        server.createContext("/func/admin/version", new SimpleWebHandler((h, ex) -> h.sendOKJson(h.createJson("version", controller.getVersionString()), ex)));
        server.createContext("/func/admin/reboot", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                // don't use a simpler handler, because response must be sent before controller shuts down
                sendSimpleResponse("200 OK", 200, e);
                Power.reboot();
                controller.exit();
            }
        });
        server.createContext("/func/admin/exit", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                // don't use a simpler handler, because response must be sent first
                sendSimpleResponse("200 OK", 200, e);
                controller.exit();
            }
        });
        server.createContext("/func/admin/shutdown", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                // don't use a simpler handler, because response must be sent before controller shuts down
                sendSimpleResponse("200 OK", 200, e);
                Power.shutdown();
                controller.exit();
            }
        });

        // filesystem functions
        server.createContext("/func/admin/fs", new SimpleWebHandler((h, ex) -> h.sendSimpleResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/admin/fs/mount", new JsonWebHandler((h, e, json) -> {
            try {
                if (getJsonField(json, "state").getAsBoolean()) {
                    controller.getFS().mountRW();
                    h.sendSimpleResponse("200 OK", 200, e);
                } else {
                    controller.getFS().mountRO();
                    h.sendSimpleResponse("200 OK", 200, e);
                }
            } catch (RuntimeException ex) {
                h.sendSimpleResponse("500 Internal Error: unable to execute mount commands", 500, e);
            }
        }));
        server.createContext("/func/admin/fs/sync", new BasicWebHandler(() -> controller.getFS().sync()));
        server.createContext("/func/admin/fs/clear_cache", new BasicWebHandler(controller::clearCache));

        // recording functions
        server.createContext("/func/record", new SimpleWebHandler((h, ex) -> h.sendSimpleResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/record/status", new SimpleWebHandler((h, ex) -> {
            JsonObject json = new JsonObject();
            json.addProperty("is_recording", controller.getCamera(0).isRecording());
            json.addProperty("recording_path", controller.getCamera(0).getRecordingPath());
            json.addProperty("recording_time", controller.getCamera(0).getRecordingTime());
            h.sendOKJson(json, ex);
        }));
        server.createContext("/func/record/video", new JsonWebHandler((h, e, json) -> {
            if (!controller.getCamera(0).isRecording()) {
                int time = getJsonField(json, "time").getAsInt();
                String fileName = getJsonField(json, "filename").getAsString();

                controller.getCamera(0).recordFor(time, new H264File(controller.getVidDir(), fileName));

                h.sendSimpleResponse("200 OK", 200, e);
            } else {
                h.sendSimpleResponse("409 Conflict: Already recording.", 409, e);
            }
        }));
        server.createContext("/func/record/stop", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                // don't use a simpler handler because this must be out of order
                sendSimpleResponse("200 OK", 200, e);
                controller.getCamera(0).stop();
            }
        });
        server.createContext("/func/record/snapshot", new JsonWebHandler((h, e, json) -> {
            JPGFile file = new JPGFile(controller.getPicDir(), getJsonField(json, "filename").getAsString());
            controller.getCamera(0).takeSnapshot(file);

            h.sendSimpleResponse("200 OK", 200, e);
        }));

        // media settings
        server.createContext("/func/media", new SimpleWebHandler((h, ex) -> h.sendSimpleResponse("404 Unknown function", 404, ex)));
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
                                    sendSimpleResponse("404 Not Found: Filesystem error", 404, e);
                                } catch (IOException ex) {
                                    System.err.println("IO error while sending file");
                                    ex.printStackTrace();
                                }
                            } else {
                                sendSimpleResponse("404 Not Found: No file could be found by that name", 404, e);
                            }
                        } else {
                            sendSimpleResponse("400 Malformed Input: unknown resource type", 400, e);
                        }
                    } else {
                        sendSimpleResponse("400 Malformed Input: resource path is invalid", 400, e);
                    }
                } else {
                    sendSimpleResponse("400 Malformed Input: resource type not specified", 400, e);
                }
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "GET".equals(e.getRequestMethod());
            }
        });
        server.createContext("/func/media/list", new JsonWebHandler((h, e, json) -> {
            JsonObject response = new JsonObject();

            File dir = getJsonField(json, "is_video").getAsBoolean() ? controller.getVidDir() : controller.getPicDir();
            File[] files = dir.listFiles();
            if (files != null) {
                // sort by modified time
                Arrays.sort(files, (o1, o2) -> (int) (o2.lastModified() - o1.lastModified()));

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                JsonArray fileArray = new JsonArray();
                for (File file : files) {
                    JsonObject fObject = new JsonObject();
                    fObject.addProperty("name", file.getName());
                    fObject.addProperty("size", formatFileSize(file.length()));
                    fObject.addProperty("modified", dateFormat.format(file.lastModified()));
                    fileArray.add(fObject);
                }
                response.add("files", fileArray);
            }
            h.sendOKJson(response, e);
        }));
        server.createContext("/func/media/delete", new JsonWebHandler((h, e, json) -> {
            File dir = getJsonField(json, "is_video").getAsBoolean() ? controller.getVidDir() : controller.getPicDir();

            String path = getJsonField(json, "path").getAsString();
            if (!path.contains("/") && !path.contains("\\")) {
                File file = new File(dir, path);
                if (file.exists()) {
                    if (file.delete()) {
                        // cached streaming file
                        File streamFile = new File(controller.getStreamDir(), path + ".mp4");
                        if (streamFile.isFile()) {
                            if (streamFile.delete()) {
                                h.sendSimpleResponse("200 OK", 200, e);
                            } else {
                                System.out.printf("Unable to delete stream cache '%s'\n", file.getPath());
                                h.sendSimpleResponse("500 Internal Error: unable to cache file", 500, e);
                            }
                        } else {
                            h.sendSimpleResponse("200 OK", 200, e);
                        }
                    } else {
                        System.out.printf("Unable to delete file '%s'\n", file.getPath());
                        h.sendSimpleResponse("500 Internal Error: unable to delete file", 500, e);
                    }
                } else {
                    h.sendSimpleResponse("400 Malformed Input: file does not exist", 400, e);
                }
            } else {
                h.sendSimpleResponse("400 Malformed Input: invalid file name", 400, e);
            }
        }));
        server.createContext("/func/media/lastsnap", new WebHandler() {
            @Override
            public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
                if (controller.getCamera(0).currentSnapshotReady()) {
                    String snapName = controller.getCamera(0).getLastSnapshot().getName();
                    sendOKJson(createJson("name", snapName), e);
                } else {
                    sendSimpleResponse("202 Accepted: waiting for snapshot", 202, e);
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

                                // we may get ahead of converter on Pi Zero
                                if (count > 0) {
                                    streamOut.write(buff, 0, count);
                                }
                            }
                        } catch (FileNotFoundException ex) {
                            sendSimpleResponse("404 Not Found: Filesystem error", 404, e);
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
                        sendSimpleResponse("404 Not Found: No file could be found by that name", 404, e);
                    }
                } else {
                    sendSimpleResponse("400 Malformed Input: resource path is invalid", 400, e);
                }
            }

            @Override
            public boolean acceptRequest(HttpExchange e) {
                return "GET".equals(e.getRequestMethod());
            }
        });

        // settings and config
        server.createContext("/func/settings", new SimpleWebHandler((h, ex) -> h.sendSimpleResponse("404 Unknown function", 404, ex)));
        // camera settings
        server.createContext("/func/settings/camera", new SimpleWebHandler((h, ex) -> h.sendSimpleResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/settings/camera/get", new JsonWebHandler((h, e, json) -> {
            JsonObject response = new JsonObject();

            Settings settings = getJsonField(json, "is_video").getAsBoolean() ? controller.getVidSettings() : controller.getPicSettings();
            response.add("settings", controller.getGson().toJsonTree(settings));

            h.sendOKJson(response, e);
        }));
        server.createContext("/func/settings/camera/set", new JsonWebHandler((h, e, json) -> {
            String contents = getJsonField(json, "settings").getAsString();
            if (getJsonField(json, "is_video").getAsBoolean()) {
                VidSettings settings = controller.getGson().fromJson(contents, VidSettings.class);
                controller.getVidSettings().mixIn(settings);
            } else {
                PicSettings settings = controller.getGson().fromJson(contents, PicSettings.class);
                controller.getPicSettings().mixIn(settings);
            }
            h.sendSimpleResponse("200 OK", 200, e);
        }));
        server.createContext("/func/settings/camera/reset", new JsonWebHandler((h, e, json) -> {
            if (getJsonField(json, "is_video").getAsBoolean()) {
                controller.resetVidSettings();
            } else {
                controller.resetPicSettings();
            }
            h.sendSimpleResponse("200 OK", 200, e);
        }));
        server.createContext("/func/settings/camera/save", new JsonWebHandler((h, e, json) -> {
            if (getJsonField(json, "is_video").getAsBoolean()) {
                controller.saveVideoConfig();
            } else {
                controller.savePicConfig();
            }
            h.sendSimpleResponse("200 OK", 200, e);
        }));

        // system settings
        server.createContext("/func/settings/system", new SimpleWebHandler((h, ex) -> h.sendSimpleResponse("404 Unknown function", 404, ex)));
        server.createContext("/func/settings/system/set", new JsonWebHandler((h, e, json) -> {
            controller.updateConfig(json);
            controller.getNetwork().applyNetworkSettings();
            h.sendSimpleResponse("200 OK", 200, e);
        }));
        server.createContext("/func/settings/system/get", new SimpleWebHandler((h, ex) -> {
            JsonObject response = new JsonObject();
            response.add("settings", controller.getConfigJson());
            h.sendOKJson(response, ex);
        }));
        server.createContext("/func/settings/system/reset", new BasicWebHandler(controller::resetConfig));
        server.createContext("/func/settings/system/save", new BasicWebHandler(controller::saveConfig));

        // debug functions
        server.createContext("/func/debug", new SimpleWebHandler((h, ex) -> h.sendSimpleResponse("404 Unknown function", 404, ex)));
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
                            sendSimpleResponse("400 Malformed Input: size must be 1 - 1024", 400, e);
                        }
                    } catch (NumberFormatException ex) {
                        sendSimpleResponse("400 Malformed Input: size must be an integer", 400, e);
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

    private static JsonElement getJsonField(JsonObject obj, String name) {
        JsonElement element = obj.get(name);
        if (element == null) {
            throw new JsonSyntaxException("Missing field: " + name);
        }
        return element;
    }
}
