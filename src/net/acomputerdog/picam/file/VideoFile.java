package net.acomputerdog.picam.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class VideoFile {
    private final File file;

    public VideoFile(File dir, String name) {
        int offset = 0;
        File f = new File(dir, name + ".0.h264");
        while (f.exists()) {
            offset++;
            //f = new File(String.format("%s.%3d.h264", name, offset));
            f = new File(dir, name + "." + offset + ".h264");
        }

        this.file = f;
    }

    public File getFile() {
        return file;
    }

    public OutputStream getOutStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }
}
