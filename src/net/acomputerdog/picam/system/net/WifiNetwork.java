package net.acomputerdog.picam.system.net;

public class WifiNetwork {
    public String ssid;
    public String passcode;

    public boolean dhcpEnabled;

    // only if DHCP is off
    public String netmask;
    public String gateway;
    public String address;

    public static WifiNetwork createDefault() {
        WifiNetwork net = new WifiNetwork();
        net.ssid = "WifiName";
        net.passcode = "WifiPassword";
        net.dhcpEnabled = true;

        // ignore rest because DHCP is on
        net.netmask = "";
        net.gateway = "";
        net.address = "";

        return net;
    }
}
