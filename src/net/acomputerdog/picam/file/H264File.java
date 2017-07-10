package net.acomputerdog.picam.file;

import java.io.File;

public class H264File extends NumberedFile {

    public H264File(File dir, String name) {
        super(dir, name, "h264");
    }
}
