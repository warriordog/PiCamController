package net.acomputerdog.picam.system;

import java.io.IOException;

public class Power {
    public static void reboot() {
        try {
            Runtime.getRuntime().exec(new String[]{"sudo", "reboot"});
        } catch (IOException e) {
            throw new RuntimeException("Unable to execute reboot command", e);
        }
    }

    public static void shutdown() {
        try {
            Runtime.getRuntime().exec(new String[]{"sudo", "shutdown", "-h", "now"});
        } catch (IOException e) {
            throw new RuntimeException("Unable to execute shutdown command", e);
        }
    }
}
