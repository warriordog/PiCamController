package net.acomputerdog.picam.web.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;

public class JsonWebHandler extends WebHandler {
    private final JsonExchangeAcceptor handler;
    private final JsonParser parser;

    public JsonWebHandler(JsonExchangeAcceptor handler) {
        this.handler = handler;
        parser = new JsonParser();
    }

    @Override
    public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
        JsonObject json = null;
        try {
            json = parser.parse(postData).getAsJsonObject();
        } catch (IllegalStateException | JsonParseException ex) {
            sendSimpleResponse("400 Malformed Input: invalid JSON", 400, e);
        }

        if (json != null) {
            try {
                handler.accept(this, e, json);
            } catch (ClassCastException ex) {
                sendSimpleResponse("400 Malformed Input: field is wrong type", 400, e);
            }
        }
    }

    @Override
    public boolean acceptRequest(HttpExchange e) {
        return "POST".equals(e.getRequestMethod());
    }

    // make public so handlers can send responses
    @Override
    public void sendSimpleResponse(String message, int code, HttpExchange e) throws IOException {
        super.sendSimpleResponse(message, code, e);
    }

    // make public so handlers can send responses
    @Override
    public void sendJson(JsonObject object, String response, int code, HttpExchange e) throws IOException {
        super.sendJson(object, response, code, e);
    }

    // make public so handlers can send responses
    @Override
    public void sendOKJson(JsonObject object, HttpExchange e) throws IOException {
        super.sendOKJson(object, e);
    }

    // make public so handlers can send responses
    @Override
    public void sendFile(HttpExchange exchange, InputStream in) throws IOException {
        super.sendFile(exchange, in);
    }

    // make public so handlers can send responses
    @Override
    public void redirect(String path, HttpExchange exchange) throws IOException {
        super.redirect(path, exchange);
    }

    public interface JsonExchangeAcceptor {
        void accept(JsonWebHandler handler, HttpExchange e, JsonObject jsonData) throws Exception;
    }
}
