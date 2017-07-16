package net.acomputerdog.picam.camera.recorder;

import net.acomputerdog.picam.camera.Camera;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VideoRecorder implements Recorder {
    private final Camera camera;

    private boolean recording = false;

    private Process recordProcess;

    private InputStream recordIn;
    private OutputStream recordOut;

    private File recordFile;

    public VideoRecorder(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void record(File outFile, int time) {
        if (recording) {
            throw new IllegalStateException("Already recording");
        } else {
            recording = true;
            recordFile = outFile;

            List<String> cmd = new ArrayList<>();
            cmd.add("raspivid");
            cmd.add("-o");
            cmd.add("-");
            cmd.add("-t");
            cmd.add(String.valueOf(time));
            cmd.add("-n");
            camera.getVidSettings().addToCommandLine(cmd);

            ProcessBuilder pb = new ProcessBuilder();
            pb.command(cmd);
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Thread recordThread = new Thread(() -> {
                try {
                    byte[] buff = new byte[512];
                    while (recording && recordProcess.isAlive()) {
                        // copy some of the file
                        int count = recordIn.read(buff);

                        if (count == -1) {
                            // end of file
                            stop();
                            break;
                        }

                        recordOut.write(buff, 0, count);
                    }
                    Recorder.flushBuffers(recordIn, recordOut);
                } catch (IOException e) {
                    System.err.println("IO error while recording");
                    e.printStackTrace();
                    stop();
                } finally {
                    // once this thread stops, we HAVE to mark as not recording
                    recording = false;

                    Recorder.close(recordIn);
                    Recorder.close(recordOut);
                }
            });
            recordThread.setName("record_thread");
            recordThread.setDaemon(false);


            try {
                recordProcess = pb.start();

                recordOut = new FileOutputStream(outFile);
                recordIn = recordProcess.getInputStream();

                recordThread.start();
            } catch (IOException e) {
                Recorder.close(recordOut);
                Recorder.close(recordIn);
                throw new RuntimeException("Exception running record command", e);
            }
        }
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
