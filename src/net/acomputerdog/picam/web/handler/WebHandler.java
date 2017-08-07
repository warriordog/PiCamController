package net.acomputerdog.picam.web.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public abstract class WebHandler implements HttpHandler {
    private boolean responseSent = false;

    @Override
    public void handle(HttpExchange e) throws IOException {
        responseSent = false;
        if (acceptRequest(e)) {
            try {
                String query = e.getRequestURI().getQuery();
                String data = new BufferedReader(new InputStreamReader(e.getRequestBody())).lines().collect(Collectors.joining());
                try {
                    handleExchange(e, query, data);
                } catch (Exception ex) {
                    System.err.println("Exception handling request");
                    ex.printStackTrace();
                    if (!responseSent) {
                        sendResponse("500 Internal Server Error: " + ex.toString(), 500, e);
                    }
                }
            } catch (IOException ex) {
                System.err.println("IO error handling request");
                ex.printStackTrace();
                if (!responseSent) {
                    sendResponse("500 Internal I/O Error: " + ex.toString(), 500, e);
                }
            }
        } else {
            e.sendResponseHeaders(405, 0);
        }
        e.close();
    }

    public JsonObject createJson(Object ... items) {
        if (items == null || items.length % 2 == 1) {
            throw new IllegalArgumentException("JSON parameters must be in key, value order");
        }

        JsonObject object = new JsonObject();
        for (int i = 0; i < items.length; i += 2) {
            object.add(String.valueOf(items[i]), jsonValueOf(items[i + 1]));
        }

        return object;
    }

    public JsonElement jsonValueOf(Object obj) {
        if (obj == null) {
            return JsonNull.INSTANCE;
        }
        if (obj instanceof List) {
            JsonArray arr = new JsonArray();
            for (Object subItem : (List)obj) {
                arr.add(jsonValueOf(subItem));
            }
            return arr;
        }
        if (obj instanceof Number) {
            return new JsonPrimitive((Number)obj);
        }
        if (obj instanceof Boolean) {
            return new JsonPrimitive((Boolean)obj);
        }
        if (obj instanceof Character) {
            return new JsonPrimitive((Character)obj);
        }
        return new JsonPrimitive(String.valueOf(obj));
    }

    /*
        Override to change type
     */
    public boolean acceptRequest(HttpExchange e) {
        return "GET".equals(e.getRequestMethod()) || "POST".equals(e.getRequestMethod());
    }

    public abstract void handleExchange(HttpExchange e, String getData, String postData) throws Exception;

    protected void sendSimpleResponse(String message, int code, HttpExchange e) throws IOException {
        sendResponse("{\"message\":\"" + message.replace("\"", "\\\"") + "\"}", code, e);
    }

    protected void sendOKJson(JsonObject object, HttpExchange e) throws IOException {
        sendJson(object, "200 OK", 200, e);
    }

    protected void sendJson(JsonObject object, String response, int code, HttpExchange e) throws IOException {
        object.addProperty("message", response);
        sendJson(object, code, e);
    }

    protected void sendJson(JsonObject object, int code, HttpExchange e) throws IOException {
        e.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(object.toString(), code, e);
    }

    protected void sendResponse(String response, int code, HttpExchange e) throws IOException {
        if (!responseSent) {
            try {
                byte[] bytes = response.getBytes();
                e.sendResponseHeaders(code, bytes.length);
                e.getResponseBody().write(bytes);
            } finally {
                responseSent = true;
                e.getResponseBody().close();
                e.close();
            }
        } else {
            System.err.println("Warning: attempted to send multiple responses");
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
