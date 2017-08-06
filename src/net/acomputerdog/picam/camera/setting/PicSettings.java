package net.acomputerdog.picam.camera.setting;

public class PicSettings extends Settings {
    // camera mode, 0 is auto
    public final int md = 2;

    // width of picture (limited by mode)
    public final int w = 2592;

    // height of picture (limited by mode)
    public final int h = 1944;

    // video stabilization (true = -vs, false = nothing)
    public final boolean vs = false;

    // average white balance
    public final String awb = "fluorescent";

    // dynamic range compensation
    public final String drc = "med";

    // exposure mode
    public final String ex = "antishake";

    // JPG quality
    public final int q = 90;

    // frameburst mode
    public final boolean bm = true;
}
