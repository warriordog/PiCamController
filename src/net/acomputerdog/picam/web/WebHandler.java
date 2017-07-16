package net.acomputerdog.picam.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.stream.Collectors;

public abstract class WebHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange e) throws IOException {
        if (acceptRequest(e)) {
            try {
                String query = e.getRequestURI().getQuery();
                String data = new BufferedReader(new InputStreamReader(e.getRequestBody())).lines().collect(Collectors.joining());
                try {
                    handleExchange(e, query, data);
                } catch (Exception ex) {
                    System.err.println("Exception handling request");
                    ex.printStackTrace();
                    sendResponse("500 Internal Server Error: " + ex.toString(), 500, e);
                }
            } catch (IOException ex) {
                System.err.println("IO error handling request");
                ex.printStackTrace();
                sendResponse("500 Internal I/O Error: " + ex.toString(), 500, e);
            }
        } else {
            e.sendResponseHeaders(405, 0);
        }
        e.close();
    }

    /*
        Override to change type
     */
    public boolean acceptRequest(HttpExchange e) {
        return "GET".equals(e.getRequestMethod()) || "POST".equals(e.getRequestMethod());
    }

    public abstract void handleExchange(HttpExchange e, String getData, String postData) throws Exception;

    protected void sendResponse(String response, int code, HttpExchange e) throws IOException {
        byte[] bytes = response.getBytes();
        e.sendResponseHeaders(code, bytes.length);
        try {
            e.getResponseBody().write(bytes);
        } finally {
            e.getResponseBody().close();
            e.close();
        }
    }

    protected void sendFile(HttpExchange exchange, InputStream in) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        byte[] buff = new byte[128];

        // autocloses out
        try (OutputStream out = exchange.getResponseBody();) {

            while (in.available() > 0) {
                int count = in.read(buff);
                out.write(buff, 0, count);
            }
        } catch (IOException e) {
            System.err.println("IO error sending file: " + e.toString());
        } finally {
            exchange.close();
        }
    }

    protected void redirect(String path, HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Location", path);
        sendResponse("308 Permanent Redirect", 308, exchange);
    }
}
