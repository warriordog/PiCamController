package net.acomputerdog.picam.config;

import net.acomputerdog.picam.system.net.WifiNetwork;

public class PiConfig {
    public String baseDirectory;
    public String rootFS;
    public boolean readOnlyRoot;

    public boolean networkEnabled;
    public String wifiInterface;

    public WifiNetwork primaryWifi;
    public WifiNetwork secondaryWifi;

    public static PiConfig createDefault() {
        PiConfig cfg = new PiConfig();
        cfg.baseDirectory = "./";
        cfg.rootFS = "/";
        cfg.networkEnabled = true;
        cfg.readOnlyRoot = true;
        cfg.wifiInterface = "wlan0";
        cfg.primaryWifi = WifiNetwork.createDefault();
        cfg.secondaryWifi = WifiNetwork.createDefault();

        return cfg;
    }
}
