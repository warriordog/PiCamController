package net.acomputerdog.picam.web;

import com.sun.net.httpserver.HttpExchange;

public class SimpleWebHandler extends WebHandler {
    private final SimpleExchangeAcceptor handler;

    public SimpleWebHandler(SimpleExchangeAcceptor handler) {
        this.handler = handler;
    }

    @Override
    public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
        handler.accept(this, e);
    }

    public interface SimpleExchangeAcceptor {
        void accept(WebHandler handler, HttpExchange e) throws Exception;
    }
}
