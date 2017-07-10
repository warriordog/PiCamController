package net.acomputerdog.picam.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class NumberedFile {
    private final File file;

    public NumberedFile(File dir, String name, String ext) {
        if (!dir.exists() && !dir.mkdir()) {
            throw new RuntimeException("Unable to create directory");
        }

        int offset = 0;

        File f;
        do {
            f = new File(dir, name + "." + offset + "." + ext);
            offset++;
        } while (f.exists());

        this.file = f;
    }

    public File getFile() {
        return file;
    }

    public OutputStream getOutStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }
}
