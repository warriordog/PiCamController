package net.acomputerdog.picam.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Proc {
    public static void execSync(String ... cmd) throws IOException {

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            while (p.isAlive()) {
                Thread.sleep(1);
            }

        }catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }
    }

    public static String execCMD(String ... cmd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);

        StringBuilder outBuilder = new StringBuilder();
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while (p.isAlive()) {
            if (reader.ready()) {
                outBuilder.append(reader.readLine());
                outBuilder.append('\n');
            }
        }

        return outBuilder.toString();
    }

    public static Process execAsync(String ... cmd) throws IOException {
        if (cmd.length < 1) {
            throw new IllegalArgumentException("Command must be specified");
        }

        return Runtime.getRuntime().exec(cmd);
    }
}
