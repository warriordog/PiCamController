package net.acomputerdog.picam.system.net;

import net.acomputerdog.picam.PiCamController;
import net.acomputerdog.picam.config.PiConfig;
import net.acomputerdog.picam.util.Proc;

import java.io.*;
import java.util.Random;

public class Network {
    private final PiCamController controller;
    private final String wpaTemplate;
    private final String interfacesTemplate;
    private final Random random;

    public Network(PiCamController controller) {
        this.controller = controller;
        this.random = new Random();

        StringBuilder wpaBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/net/wpa_supplicant.conf")))) {
            while (reader.ready()) {
                wpaBuilder.append(reader.readLine());
                wpaBuilder.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("IO error reading WPA template", e);
        }
        this.wpaTemplate = wpaBuilder.toString();

        StringBuilder intBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/net/interfaces")))) {
            while (reader.ready()) {
                intBuilder.append(reader.readLine());
                intBuilder.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("IO error reading interfaces template", e);
        }
        this.interfacesTemplate = intBuilder.toString();
    }

    public void applyNetworkSettings() throws IOException {
        PiConfig config = controller.getConfig();
        if (config.networkEnabled) {
            // enable write access
            controller.getFS().mountRW();

            // shut off wifi
            Proc.execSync("sudo", "ifconfig", config.wifiInterface, "down");

            // create WPA supplicant
            setupWPA();

            // create interfaces
            setupInterfaces();

            // turn on wifi
            Proc.execSync("sudo", "ifconfig", config.wifiInterface, "up");

            // disable write access
            controller.getFS().mountRO();
        }
    }


    public void backupNetSettings() throws IOException {
        // make initial backup (if needed)
        File origWPA = new File("/etc/wpa_supplicant/wpa_supplicant.conf.original");
        if (!origWPA.exists()) {
            if (!Proc.execCMD("sudo", "cp", "/etc/wpa_supplicant/wpa_supplicant.conf", origWPA.getAbsolutePath()).trim().isEmpty()) {
                throw new RuntimeException("Unable to backup original WPA supplicant");
            }
        }

        File origInt = new File("/etc/network/interfaces.original");
        if (!origInt.exists()) {
            if (!Proc.execCMD("sudo", "cp", "/etc/network/interfaces", origInt.getAbsolutePath()).trim().isEmpty()) {
                throw new RuntimeException("Unable to backup original interfaces file");
            }
        }

        File bakWPA = new File("/etc/wpa_supplicant/wpa_supplicant.conf.bak");
        if (bakWPA.exists()) {
            if (!Proc.execCMD("sudo", "rm", bakWPA.getAbsolutePath()).trim().isEmpty()) {
                throw new RuntimeException("Unable to remove old backup of WPA supplicant");
            }
        }
        if (!Proc.execCMD("sudo", "cp", "/etc/wpa_supplicant/wpa_supplicant.conf", bakWPA.getAbsolutePath()).trim().isEmpty()) {
            throw new RuntimeException("Unable to backup WPA supplicant");
        }

        File bakInt = new File("/etc/network/interfaces.bak");
        if (bakInt.exists()) {
            if (!Proc.execCMD("sudo", "rm", bakInt.getAbsolutePath()).trim().isEmpty()) {
                throw new RuntimeException("Unable to remove old backup of interfaces");
            }
        }
        if (!Proc.execCMD("sudo", "cp", "/etc/network/interfaces", bakInt.getAbsolutePath()).trim().isEmpty()) {
            throw new RuntimeException("Unable to backup interfaces");
        }
    }

    private void setupWPA() {
        PiConfig config = controller.getConfig();
        String wpaContents = String.format(wpaTemplate, config.primaryWifi.ssid, config.primaryWifi.passcode, config.secondaryWifi.ssid, config.secondaryWifi.passcode);

        writeRootFile("/etc/wpa_supplicant/wpa_supplicant.conf", wpaContents);
    }

    private void setupInterfaces() {
        PiConfig config = controller.getConfig();
        WifiNetwork prim = config.primaryWifi;
        WifiNetwork sec = config.secondaryWifi;

        String primMode = "dhcp";
        String primSettings = "";
        if (!prim.dhcpEnabled) {
            primSettings =  "address " + prim.address + "\n" +
                            "gateway " + prim.gateway + "\n" +
                            "netmask " + prim.netmask + "\n";
            primMode = "static";
        }

        String secMode = "dhcp";
        String secSettings = "";
        if (!sec.dhcpEnabled) {
            secSettings =   "address " + sec.address + "\n" +
                            "gateway " + sec.gateway + "\n" +
                            "netmask " + sec.netmask + "\n";
            primMode = "static";
        }

        String intContents = String.format(interfacesTemplate, config.wifiInterface, primMode, primSettings, secMode, secSettings);

        writeRootFile("/etc/network/interfaces", intContents);
    }

    private void writeRootFile(String path, String contents) {
        String tempPath = "/tmp/" + getRandomString();
        // vulnerable to race condition, but it doesn't really matter
        try (Writer writer = new FileWriter(tempPath)) {
            writer.write(contents);
        } catch (IOException e) {
            throw new RuntimeException("Exception writing temporary file", e);
        }

        try {
            String rmResp = Proc.execCMD("sudo", "rm", path);
            if (!rmResp.trim().isEmpty()) {
                throw new RuntimeException("Unable to remove existing file");
            }

            String mvResp = Proc.execCMD("sudo", "mv", tempPath, path);
            if (!mvResp.trim().isEmpty()) {
                throw new RuntimeException("Unable to place new file");
            }

            String chResp = Proc.execCMD("sudo", "chown", "root:root", path);
            if (!chResp.trim().isEmpty()) {
                throw new RuntimeException("Unable to set ownership of new file");
            }
        } catch (IOException e) {
            throw new RuntimeException("Command error moving file", e);
        }
    }

    private String getRandomString() {
        char[] str = new char[32];
        for (int i = 0; i < 32; i++) {
            str[i] = (char)('A' + random.nextInt(26));
        }
        return String.valueOf(str);
    }
}
