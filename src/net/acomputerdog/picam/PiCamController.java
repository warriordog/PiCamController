package net.acomputerdog.picam;

import net.acomputerdog.picam.web.WebServer;

public class PiCamController {

    private final WebServer webServer;
    private final Camera camera;

    private PiCamController() {
        try {
            this.webServer = new WebServer(this);
            this.camera = new Camera(this, 0);
        } catch (Exception e) {
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

    public String buildStatusLine() {
        return camera.isRecording() ? "<div style=\"color: red\">recording</div>" : "<div style=\"color: green\">idle</div>";
    }

    public void recordFor(int time) {
        camera.recordFor(time);
    }

    public static void main(String[] args) {
        new PiCamController().start();
    }
}
