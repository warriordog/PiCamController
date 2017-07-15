package net.acomputerdog.picam.web;

import com.sun.net.httpserver.HttpExchange;

public class BasicWebHandler extends WebHandler {
    private final BasicExchangeAcceptor handler;

    public BasicWebHandler(BasicExchangeAcceptor handler) {
        this.handler = handler;
    }

    @Override
    public void handleExchange(HttpExchange e, String getData, String postData) throws Exception {
        handler.accept();
        sendResponse("200 OK", 200, e);
    }

    public interface BasicExchangeAcceptor {
        void accept() throws Exception;
    }
}
