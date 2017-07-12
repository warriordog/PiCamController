package net.acomputerdog.picam.camera.recorder;

import net.acomputerdog.picam.camera.Camera;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SnapRecorder implements Recorder {
    private final Camera camera;

    private boolean recording = false;
    private Process recordProcess;
    private File recordFile;

    public SnapRecorder(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void record(File outFile, int time) throws IOException {
        this.recordFile = outFile;
        this.recording = true;

        List<String> cmd = new ArrayList<>();
        cmd.add("raspistill");
        cmd.add("-o");
        cmd.add("-");
        cmd.add("-t");
        cmd.add("1");
        cmd.add("-n");

        camera.getPicSettings().addToCommandLine(cmd);

        //printList(command);

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        recordProcess = pb.start();
        OutputStream out = new FileOutputStream(outFile);
        InputStream in = recordProcess.getInputStream();

        Thread recordThread = new Thread(() -> {
            try {
                byte[] buff = new byte[512];
                while (recordProcess.isAlive()) {
                    // copy some of the file
                    int count = in.read(buff);

                    if (count == -1) {
                        // end of file
                        break;
                    }

                    out.write(buff, 0, count);
                }
                Recorder.flushBuffers(in, out);
            } catch (IOException e) {
                System.err.println("IO error while taking snapshot");
                e.printStackTrace();
            } finally {
                recording = false;

                Recorder.close(in);
                Recorder.close(out);
            }
        });
        recordThread.setName("snapshot_thread");
        recordThread.setDaemon(false);
        recordThread.start();
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    public void stop() {
        recordProcess.destroy();
        recording = false;
    }

    @Override
    public File getRecordFile() {
        return recordFile;
    }
}
