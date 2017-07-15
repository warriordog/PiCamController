package net.acomputerdog.picam.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;

public class WebFileHandler extends WebHandler {
    @Override
    public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
        sendWebFile(e);
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
}
