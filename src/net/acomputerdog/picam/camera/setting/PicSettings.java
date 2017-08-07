package net.acomputerdog.picam.camera.setting;

public class PicSettings extends Settings {
    // camera mode, 0 is auto
    public int md = 2;

    // width of picture (limited by mode)
    public int w = 2592;

    // height of picture (limited by mode)
    public int h = 1944;

    // video stabilization (true = -vs, false = nothing)
    public boolean vs = false;

    // average white balance
    public String awb = "auto";

    // dynamic range compensation
    public String drc = "med";

    // exposure mode
    public String ex = "antishake";

    // JPG quality
    public int q = 90;

    // frameburst mode
    public boolean bm = true;

    // rotation (0, 90, 180, 270)
    public int rot = 0;
}
