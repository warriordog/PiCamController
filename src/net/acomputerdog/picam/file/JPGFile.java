package net.acomputerdog.picam.file;

import java.io.File;

public class JPGFile extends NumberedFile {
    public JPGFile(File dir, String name) {
        super(dir, name, "jpg");
    }
}
