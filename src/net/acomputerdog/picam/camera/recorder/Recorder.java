package net.acomputerdog.picam.camera.recorder;

import java.io.*;
import java.util.List;

public interface Recorder {
    void record(File outFile, int time) throws IOException;
    boolean isRecording();
    void stop();
    File getRecordFile();

    static void flushBuffers(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buff = new byte[64];
            while (in.available() > 0) {
                int count = in.read(buff);
                out.write(buff, 0, count);
            }
        } catch (IOException ignored) {
            // read stream may already be closed
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

    static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {

            }
        }
    }

    static void printList(List<?> list) {
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
