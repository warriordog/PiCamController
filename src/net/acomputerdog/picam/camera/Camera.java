package net.acomputerdog.picam.camera;

import net.acomputerdog.picam.PiCamController;
import net.acomputerdog.picam.camera.recorder.SnapRecorder;
import net.acomputerdog.picam.camera.recorder.VideoRecorder;
import net.acomputerdog.picam.file.H264File;
import net.acomputerdog.picam.file.JPGFile;

import java.io.File;
import java.io.IOException;

public class Camera {
    private final PiCamController controller;
    private final int cameraNumber;

    private final VideoRecorder videoRecorder;
    private final SnapRecorder snapRecorder;

    private long recordStart = 0;

    private File lastSnapshot;

    public Camera(PiCamController controller, int cameraNumber) {
        this.controller = controller;
        this.cameraNumber = cameraNumber;
        this.videoRecorder = new VideoRecorder(this);
        this.snapRecorder = new SnapRecorder(this);
    }

    public int getCameraNumber() {
        return cameraNumber;
    }

    public boolean isRecording() {
        return videoRecorder.isRecording() || snapRecorder.isRecording();
    }

    public void recordFor(int time, H264File videoFile) {
        if (isRecording()) {
            throw new IllegalStateException("Already recording.");
        } else {
            recordStart = System.currentTimeMillis();
            videoRecorder.record(videoFile.getFile(), time);
        }
    }

    public void stop() {
        if (videoRecorder.isRecording()) {
            videoRecorder.stop();
        }
        if (snapRecorder.isRecording()) {
            snapRecorder.stop();
        }
    }

    public long getRecordingTime() {
        return System.currentTimeMillis() - recordStart;
    }

    public String getRecordingPath() {
        String recordPath = "N/A";
        if (videoRecorder.isRecording()) {
            recordPath = videoRecorder.getRecordFile().getAbsolutePath();
        } else if (snapRecorder.isRecording()) {
            recordPath = snapRecorder.getRecordFile().getAbsolutePath();
        }
        return recordPath;
    }

    public void takeSnapshot(JPGFile file) throws IOException {
        if (isRecording()) {
            throw new IllegalStateException("Already recording.");
        } else {
            recordStart = System.currentTimeMillis();
            lastSnapshot = file.getFile();

            snapRecorder.record(file.getFile(), 0);
        }
    }

    public File getLastSnapshot() {
        return lastSnapshot;
    }

    public boolean currentSnapshotReady() {
        // file is not ready until recorder is done
        return lastSnapshot != null && !snapRecorder.isRecording();
    }

    public PiCamController getController() {
        return controller;
    }
}
