package net.acomputerdog.picam.system;

import net.acomputerdog.picam.config.PiConfig;
import net.acomputerdog.picam.util.Proc;

import java.io.IOException;

public class FileSystem {
    private final PiConfig config;

    public FileSystem(PiConfig config) {
        this.config = config;
    }

    public void mountRW() {
        if (config.readOnlyRoot) {
            try {
                String resp = Proc.execCMD("sudo", "mount", "-f", "-o", "remount,rw", config.rootFS);
                if (resp.trim().isEmpty()) {
                    throw new RuntimeException("Unable to remount RW: " + resp);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error running mount command");
            }
        }
    }

    public void mountRO() {
        if (config.readOnlyRoot) {
            try {
                String resp = Proc.execCMD("sudo", "mount", "-f", "-o", "remount,ro", config.rootFS);
                if (resp.trim().isEmpty()) {
                    throw new RuntimeException("Unable to remount RO: " + resp);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error running mount command");
            }
        }
    }
}
