package net.acomputerdog.picam.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class H264Converter extends InputStream {
    private final Process mp4box;
    private final InputStream mp4Stream;

    public H264Converter(File vidDir, File tmpDir, String vidName) throws IOException {
        File vidFile = new File(vidDir, vidName);
        File tmpFile = new File(tmpDir, vidName + ".tmp");
        if (tmpFile.exists() && !tmpFile.delete()) {
            System.err.println("Unable to remove temp file");
        }

        ProcessBuilder builder = new ProcessBuilder();
        List<String> command = new ArrayList<>();

        command.add("bash");
        command.add("-c");
        command.add("MP4Box -add " + vidFile.getPath() + " -new " + tmpFile.getPath());

        builder.command(command);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);

        mp4box = builder.start();
        long time = 0;
        while (!tmpFile.exists()) {
            try {
                Thread.sleep(1);
                time++;
            } catch (InterruptedException ignored) {
                //break in case thread is being stopped
                break;
            }
            if (time >= 2000) {
                mp4box.destroy();
                throw new RuntimeException("Timeout waiting for MP4Box");
            }
        }
        mp4Stream = new FileInputStream(tmpFile);
    }

    @Override
    public int read() throws IOException {
        return mp4Stream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return mp4Stream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return mp4Stream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return mp4Stream.skip(n);
    }

    @Override
    public int available() throws IOException {
        int avail = mp4Stream.available();
        if (avail < 1 && mp4box.isAlive()) {
            avail = 1;
        }
        return avail;
    }

    @Override
    public void close() throws IOException {
        if (mp4Stream.available() > 0) {
            System.err.println("Closing before end of stream!");
        }
        mp4Stream.close();
        mp4box.destroy();
    }

    @Override
    public synchronized void mark(int readlimit) {
        mp4Stream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        mp4Stream.reset();
    }

    @Override
    public boolean markSupported() {
        return mp4Stream.markSupported();
    }


}
