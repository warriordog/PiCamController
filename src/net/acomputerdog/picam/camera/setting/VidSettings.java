package net.acomputerdog.picam.camera.setting;

public class VidSettings extends Settings {
    // camera mode, 0 is auto
    public int md = 1;

    // width of picture (limited by mode)
    public int w = 1920;

    // height of picture (limited by mode)
    public int h = 1080;

    // frames per second, limited by mode
    public int fps = 30;

    // stream bitrate, 0 is VBR (recommended)
    public int b = 0;

    // quantazation parameter, 0 is VBR (recommended)
    public int qp = 0;

    // intra-refresh interval (time between full redraws)
    public int g = 150;

    // h264 profile (base, main, high)
    public String pf = "high";

    // h264 level
    public String lev = "4.1";

    // include inline headers
    public boolean ih = true;

    // flush buffers
    public boolean fl = true;

    // video stabilization
    public boolean vs = false;

    // average white balance
    public String awb = "auto";

    // dynamic range compensation
    public String drc = "med";

    // exposure mode
    public String ex = "antishake";
}
