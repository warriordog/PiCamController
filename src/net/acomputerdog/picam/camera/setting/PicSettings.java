package net.acomputerdog.picam.camera.setting;

public class PicSettings extends Settings {
    // camera mode, 0 is auto
    public final Setting camMode = new Setting(list, "md", 2);

    // width of picture (limited by mode)
    public final Setting width = new Setting(list, "w", 2592);

    // height of picture (limited by mode)
    public final Setting height = new Setting(list, "h", 1944);

    // video stabilization
    public final ValuelessSetting stabilization = new ValuelessSetting(list, "vs", null);

    // average white balance
    public final Setting awb = new Setting(list, "awb", "fluorescent");

    // dynamic range compensation
    public final Setting drc = new Setting(list, "drc", "med");

    // exposure mode
    public final Setting exposure = new Setting(list, "ex", "antishake");

    // JPG quality
    public final Setting quality = new Setting(list, "q", "90");

    // frameburst mode
    public final ValuelessSetting frameburst = new ValuelessSetting(list, "bm", true);
}
