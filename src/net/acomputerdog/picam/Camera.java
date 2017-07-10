package net.acomputerdog.picam;

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
        //TODO run command
    }
}
