package net.acomputerdog.picam.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Proc {
    public static void execSync(String ... cmd) throws IOException {
        System.out.printf("Executing: '%s'\n", Arrays.toString(cmd));
        try {
            Process p = execAsync(cmd);
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

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        pb.inheritIO();
        return pb.start();
    }
}
