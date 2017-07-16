package net.acomputerdog.picam.web.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;

public class SimpleWebHandler extends WebHandler {
    private final SimpleExchangeAcceptor handler;

    public SimpleWebHandler(SimpleExchangeAcceptor handler) {
        this.handler = handler;
    }

    @Override
    public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
        handler.accept(this, e);
    }

    // make public so handlers can send responses
    @Override
    public void sendResponse(String response, int code, HttpExchange e) throws IOException {
        super.sendResponse(response, code, e);
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

    public interface SimpleExchangeAcceptor {
        void accept(SimpleWebHandler handler, HttpExchange e) throws Exception;
    }
}
