package net.acomputerdog.picam.config;

import net.acomputerdog.picam.system.net.WifiNetwork;

public class PiConfig {
    public boolean networkEnabled;
    public String wifiInterface;

    public WifiNetwork primaryWifi;
    public WifiNetwork secondaryWifi;

    public static PiConfig createDefault() {
        PiConfig cfg = new PiConfig();
        cfg.networkEnabled = false;
        cfg.wifiInterface = "wlan0";
        cfg.primaryWifi = WifiNetwork.createDefault();
        cfg.secondaryWifi = WifiNetwork.createDefault();

        return cfg;
    }
}
