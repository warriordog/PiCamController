package net.acomputerdog.picam.file;

import java.io.File;

public class MP4File extends NumberedFile {
    public MP4File(File dir, String name) {
        super(dir, name, "mp4");
    }
}
