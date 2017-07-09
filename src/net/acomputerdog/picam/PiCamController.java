package net.acomputerdog.picam;

import net.acomputerdog.picam.web.WebServer;

import java.io.IOException;

public class PiCamController {

    private final WebServer webServer;

    private PiCamController() {
        try {
            this.webServer = new WebServer(this);
        } catch (IOException e) {
            throw new RuntimeException("Exception setting up camera", e);
        }
    }

    private void start() {
        webServer.start();
        System.out.println("Started.");
    }

    public void shutdown() {
        System.out.println("Shutting down controller.");
        System.exit(0);
    }

    public static void main(String[] args) {
        new PiCamController().start();
    }
}
