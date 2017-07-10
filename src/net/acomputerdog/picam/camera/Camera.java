package net.acomputerdog.picam.camera;

import net.acomputerdog.picam.PiCamController;
import net.acomputerdog.picam.file.VideoFile;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Camera {
    private final PiCamController controller;
    private final int cameraNumber;
    private CameraSettings settings;

    private boolean recording = false;
    private long recordStart = 0;

    private VideoFile recordFile;
    private Thread recordThread;
    private Process recordProcess;

    private InputStream recordIn;
    private OutputStream recordOut;

    public Camera(PiCamController controller, int cameraNumber) {
        this.controller = controller;
        this.cameraNumber = cameraNumber;
        this.settings = new CameraSettings();
    }

    public int getCameraNumber() {
        return cameraNumber;
    }

    public boolean isRecording() {
        return recording && recordProcess != null && recordProcess.isAlive();
    }

    public void recordFor(int time, VideoFile videoFile) {
        if (recording) {
            System.out.println("Already recording");
        } else {
            recording = true;
            recordStart = System.currentTimeMillis();
            this.recordFile = videoFile;

            ProcessBuilder processBuilder = new ProcessBuilder();

            List<String> command = new ArrayList<>();
            command.add("raspivid");
            command.add("-o");
            command.add("-");
            command.add("-t");
            command.add(String.valueOf(time));
            command.add("-n");
            settings.buildCommandLine(command);

            printList(command);

            processBuilder.command(command);


            //processBuilder.command("raspivid", "-o", "-", "-w", "1920", "-h", "1080", "-md", "1", "-b", "0", "-t", String.valueOf(time), "-fps", "30", "-g", "150", "-ex", "antishake", "-awb", "fluorescent", "-drc", "med", "-qp", "0", "-n", "-ih", "-lev", "high", "-fl");
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            recordThread = new Thread(() -> {
                try {
                    byte[] buff = new byte[512];
                    while (recording) {
                        if (!recordProcess.isAlive()) {
                            recording = false;
                        } else {
                            // copy some of the file
                            int count = recordIn.read(buff);

                            if (count == -1) {
                                // end of file
                                stop();
                                break;
                            }

                            recordOut.write(buff, 0, count);
                        }
                    }
                    System.out.print("Flushing buffers...");
                    flushBuffers(recordIn, recordOut);
                    System.out.println("done.");
                } catch (IOException e) {
                    System.err.println("IO error while recording");
                    e.printStackTrace();
                    close(recordIn);
                    close(recordOut);
                    stop();
                }
            });
            recordThread.setName("record_thread");
            recordThread.setDaemon(false);


            try {
                recordProcess = processBuilder.start();

                recordOut = recordFile.getOutStream();
                recordIn = recordProcess.getInputStream();

                recordThread.start();
            } catch (IOException e) {
                close(recordOut);
                close(recordIn);
                throw new RuntimeException("Exception running record command", e);
            }
        }
    }

    public void stop() {
        recordProcess.destroy();
        recording = false;
    }

    public long getRecordingTime() {
        return System.currentTimeMillis() - recordStart;
    }

    public String getRecordingPath() {
        return recordFile == null ? "N/A" : recordFile.getFile().getAbsolutePath();
    }

    public CameraSettings getSettings() {
        return settings;
    }

    private static void flushBuffers(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buff = new byte[64];
            while (in.available() > 0) {
                int count = in.read(buff);
                out.write(buff, 0, count);
            }
        } catch (IOException ignored) {
            // read stream may already be opened
        } finally {
            try {
                out.flush();
            } catch (IOException ignored) {
                // out stream may already be closed
            }
            close(in);
            close(out);
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {

            }
        }
    }

    private static void printList(List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                System.out.print(" ");
            }
            Object obj = list.get(i);
            System.out.print(String.valueOf(obj));
        }
        System.out.println();
    }
}
