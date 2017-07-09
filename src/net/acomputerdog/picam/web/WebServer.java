package net.acomputerdog.picam.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.acomputerdog.picam.PiCamController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

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
    }

    public void start() {
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                System.err.println("Exception in web server thread.");
                e.printStackTrace();
                controller.shutdown();
            }
        });
        serverThread.setName("web_server_thread");
        serverThread.start();
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
        }
    }

    private void sendResponse(String response, int code, HttpExchange exchange) throws IOException {
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
