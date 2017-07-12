package net.acomputerdog.picam.camera;

import net.acomputerdog.picam.PiCamController;
import net.acomputerdog.picam.camera.settings.PicSettings;
import net.acomputerdog.picam.camera.settings.VidSettings;
import net.acomputerdog.picam.file.JPGFile;
import net.acomputerdog.picam.file.MP4File;
import net.acomputerdog.picam.file.NumberedFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Camera {
    private final PiCamController controller;
    private final int cameraNumber;

    private VidSettings vidSettings;
    private PicSettings picSettings;

    private boolean recording = false;
    private long recordStart = 0;

    private NumberedFile recordFile;
    private Thread copyThread;
    private Process recordProc1;
    private Process recordProc2;

    private InputStream recordIn;
    private OutputStream recordOut;

    private File lastSnapshot;

    public Camera(PiCamController controller, int cameraNumber) {
        this.controller = controller;
        this.cameraNumber = cameraNumber;
        this.vidSettings = new VidSettings();
        this.picSettings = new PicSettings();
    }

    public int getCameraNumber() {
        return cameraNumber;
    }

    public boolean isRecording() {
        return recording && recordProc2.isAlive();
    }

    public void recordFor(int time, MP4File videoFile) {
        if (recording) {
            System.out.println("Already recording");
        } else {
            recording = true;
            recordStart = System.currentTimeMillis();
            this.recordFile = videoFile;

            List<String> cmd1 = new ArrayList<>();
            cmd1.add("raspivid");
            cmd1.add("-o");
            cmd1.add("-");
            cmd1.add("-t");
            cmd1.add(String.valueOf(time));
            cmd1.add("-n");
            vidSettings.buildCommandLine(cmd1);

            List<String> cmd2 = new ArrayList<>();
            cmd2.add("avconv");

            cmd2.add("-framerate");
            if (vidSettings.fps.isIncluded()) {
                cmd2.add(vidSettings.fps.getValue());
            } else {
                cmd2.add("30");
            }

            cmd2.add("-i");
            cmd2.add("-");
            cmd2.add("-loglevel");
            cmd2.add("panic");
            cmd2.add("-c");
            cmd2.add("copy");
            cmd2.add(videoFile.getFile().getPath());

            //printList(command);

            ProcessBuilder pb1 = new ProcessBuilder();
            pb1.command(cmd1);
            pb1.redirectOutput(ProcessBuilder.Redirect.PIPE);
            pb1.redirectError(ProcessBuilder.Redirect.INHERIT);

            ProcessBuilder pb2 = new ProcessBuilder();
            pb2.command(cmd2);
            pb2.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb2.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb2.redirectInput(ProcessBuilder.Redirect.PIPE);

            copyThread = new Thread(() -> {
                try {
                    byte[] buff = new byte[512];
                    while (recording && recordProc2.isAlive()) {
                        // copy some of the file
                        int count = recordIn.read(buff);

                        // stop at end of stream
                        if (count < 0) {
                            break;
                        }

                        recordOut.write(buff, 0, count);
                    }
                    System.out.print("Flushing buffers...");
                    flushBuffers(recordIn, recordOut);
                    System.out.println("done.");
                } catch (IOException e) {
                    System.err.println("IO error while recording");
                    e.printStackTrace();
                    stop();
                } finally {
                    // once this thread stops, we HAVE to mark as not recording
                    recording = false;

                    close(recordIn);
                    close(recordOut);
                }
            });
            copyThread.setName("record_thread");
        copyThread.setDaemon(false);

            try {
                recordProc1 = pb1.start();
                recordProc2 = pb2.start();

                recordOut = recordProc2.getOutputStream();
                recordIn = recordProc1.getInputStream();

                copyThread.start();
            } catch (IOException e) {
                close(recordOut);
                close(recordIn);
                throw new RuntimeException("Exception running record command", e);
            }
        }
    }

    public void stop() {
        recordProc1.destroy();
    }

    public long getRecordingTime() {
        return System.currentTimeMillis() - recordStart;
    }

    public String getRecordingPath() {
        return recordFile == null ? "N/A" : recordFile.getFile().getAbsolutePath();
    }

    public VidSettings getVidSettings() {
        return vidSettings;
    }

    public PicSettings getPicSettings() {
        return picSettings;
    }

    public void takeSnapshot(JPGFile file) throws IOException {
        // erase last snapshot
        lastSnapshot = null;

        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> command = new ArrayList<>();
        command.add("raspistill");
        command.add("-o");
        command.add("-");
        command.add("-t");
        command.add("1");
        command.add("-n");

        picSettings.buildCommandLine(command);

        //printList(command);

        processBuilder.command(command);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process proc = processBuilder.start();
        OutputStream out = file.getOutStream();
        InputStream in = proc.getInputStream();

        copyThread = new Thread(() -> {
            try {
                byte[] buff = new byte[512];
                while (proc.isAlive()) {
                    // copy some of the file
                    int count = in.read(buff);

                    if (count == -1) {
                        // end of file
                        break;
                    }

                    out.write(buff, 0, count);
                }
                System.out.print("Flushing buffers...");
                flushBuffers(in, out);
                System.out.println("done.");
                lastSnapshot = file.getFile();
            } catch (IOException e) {
                System.err.println("IO error while taking snapshot");
                e.printStackTrace();
            } finally {
                close(in);
                close(out);
            }
        });
        copyThread.setName("copy_thread");
        copyThread.setDaemon(false);
        copyThread.start();
    }

    public File getLastSnapshot() {
        return lastSnapshot;
    }

    public boolean currentSnapshotReady() {
        return lastSnapshot != null;
    }

    public void resetVidSettings() {
        vidSettings = new VidSettings();
    }

    public void resetPicSettings() {
        picSettings = new PicSettings();
    }

    public NumberedFile getRecordingFile() {
        return recordFile;
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
