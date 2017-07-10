package net.acomputerdog.picam;

import java.io.IOException;

public class Camera {
    private final PiCamController controller;
    private final int cameraNumber;

    private boolean recording = false;

    public Camera(PiCamController controller, int cameraNumber) {
        this.controller = controller;
        this.cameraNumber = cameraNumber;
    }

    public int getCameraNumber() {
        return cameraNumber;
    }

    public boolean isRecording() {
        return recording;
    }

    public void recordFor(int time) {
        recording = true;

        try {
            Runtime.getRuntime().exec("xterm");
            //TODO run real command
        } catch (IOException e) {
            throw new RuntimeException("Exception running record command", e);
        }
    }

    public void stop() {
        try {
            Runtime.getRuntime().exec(new String[]{"killall", "xterm"});
            recording = false;
        } catch (IOException e) {
            throw new RuntimeException("Exception stopping recording", e);
        }
    }
}
