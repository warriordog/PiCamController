package net.acomputerdog.picam.system;

import net.acomputerdog.picam.config.PiConfig;
import net.acomputerdog.picam.util.Proc;

import java.io.IOException;

public class FileSystem {
    private final PiConfig config;

    public FileSystem(PiConfig config) {
        this.config = config;
    }

    public boolean mountRWSafe() {
        try {
            mountRW();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void mountRW() {
        try {
            String resp = Proc.execCMD("sudo", "mount", "-o", "remount,rw", config.rootFS);
            if (!resp.trim().isEmpty()) {
                throw new RuntimeException("Unable to remount RW: " + resp);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error running mount command", e);
        }
    }

    public boolean mountROSafe() {
        try {
            mountRO();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void mountRO() {
        try {
            String resp = Proc.execCMD("sudo", "mount", "-o", "remount,ro", config.rootFS);
            if (!resp.trim().isEmpty()) {
                throw new RuntimeException("Unable to remount RO: " + resp);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error running mount command", e);
        }
    }

    public boolean mountSyncSafe() {
        try {
            sync();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void sync() {
        try {
            String resp = Proc.execCMD("sudo", "sync");
            if (!resp.trim().isEmpty()) {
                throw new RuntimeException("Unable to sync: " + resp);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error running sync command", e);
        }
    }
}
