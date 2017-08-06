package net.acomputerdog.picam.camera.setting;

public class VidSettings extends Settings {
    // camera mode, 0 is auto
    public final int md = 1;

    // width of picture (limited by mode)
    public final int w = 1920;

    // height of picture (limited by mode)
    public final int h = 1080;

    // frames per second, limited by mode
    public final int fps = 30;

    // stream bitrate, 0 is VBR (recommended)
    public final int b = 0;

    // quantazation parameter, 0 is VBR (recommended)
    public final int qp = 0;

    // intra-refresh interval (time between full redraws)
    public final int g = 150;

    // h264 profile (base, main, high)
    public final String pf = "high";

    // h264 level
    // public final String lev = null;

    // include inline headers
    public final boolean ih = true;

    // flush buffers
    public final boolean fl = true;

    // video stabilization
    public final boolean vs = false;

    // average white balance
    public final String awb = "fluorescent";

    // dynamic range compensation
    public final String drc = "med";

    // exposure mode
    public final String ex = "antishake";
}
