package net.acomputerdog.picam;

import net.acomputerdog.picam.web.WebServer;

import java.io.File;

public class PiCamController {

    private final WebServer webServer;
    private final Camera[] cameras;
    private final File vidDir;

    private PiCamController() {
        try {
            this.webServer = new WebServer(this);
            this.cameras = new Camera[]{new Camera(this, 0)};

            vidDir = new File("./videos/");
            if (!vidDir.isDirectory() && !vidDir.mkdir()) {
                System.out.println("Unable to create video directory");
            }
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

    public Camera getCamera(int num) {
        return cameras[num];
    }

    public File getVidDir() {
        return vidDir;
    }

    public static void main(String[] args) {
        new PiCamController().start();
    }
}
