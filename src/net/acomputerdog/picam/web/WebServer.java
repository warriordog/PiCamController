package net.acomputerdog.picam.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.acomputerdog.picam.PiCamController;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;

public class WebServer {
    private final PiCamController controller;
    private final HttpServer server;

    public WebServer(PiCamController controller) throws IOException {
        this.controller = controller;

        // who even knows what that 0 is for
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", e -> redirect("/html/main.html", e));
        server.createContext("/html/", this::sendFile);
        server.createContext("/img/", this::sendFile);
        server.createContext("/include/", this::sendFile);
        server.createContext("/func/", e -> sendResponse("404 Unknown function", 404, e));
        server.createContext("/func/exit", e -> {
            sendResponse("200 OK", 200, e);
            controller.shutdown();
        });
        server.createContext("/func/version", e -> sendResponse("Pi Camera Controller v0.0.0", 200, e));
        server.createContext("/func/status", e -> sendResponse(controller.getStatusString(), 200, e));
        server.createContext("/func/record", e -> {
            if ("POST".equals(e.getRequestMethod())) {
                String request = new BufferedReader(new InputStreamReader(e.getRequestBody())).lines().collect(Collectors.joining());

                int idx = request.indexOf('=');
                if (idx > -1 && request.length() - idx > 1) {
                    try {
                        int time = Integer.parseInt(request.substring(idx + 1));

                        controller.recordFor(time);
                    } catch (NumberFormatException ex) {
                        sendResponse("400 Malformed Input: time must be an integer", 400, e);
                    }
                } else {
                    sendResponse("400 Malformed Input: time is missing", 400, e);
                }
            } else {
                sendResponse("405 Method Not Allowed: use POST", 405, e);
            }
        });
        server.createContext("/func/record_stop", e -> {
            sendResponse(controller.getStatusString(), 200, e);

            controller.stopRecording();
        });
    }

    public void start() {
        server.start();
    }

    private void redirect(String path, HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Location", path);
        sendResponse("308 Permanent Redirect", 308, exchange);
    }

    private void sendFile(HttpExchange exchange) throws IOException {
        String fullPath = "/web/" + exchange.getRequestURI().getPath().replace("..", "");
        InputStream in = getClass().getResourceAsStream(fullPath);

        if (in == null) {
            sendResponse("404 not found", 404, exchange);
        } else {
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
}
