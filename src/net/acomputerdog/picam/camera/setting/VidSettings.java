package net.acomputerdog.picam.camera.setting;

public class VidSettings extends Settings {
    // camera mode, 0 is auto
    public final Setting camMode = new Setting(list, "md", 1);

    // frames per second, limited by mode
    public final Setting fps = new Setting(list, "fps", 30);

    // width of video (limited by mode)
    public final Setting width = new Setting(list, "w", 1920);

    // height of video (limited by mode)
    public final Setting height = new Setting(list, "h", 1080);

    // stream bitrate, 0 is VBR (recommended)
    public final Setting bitrate = new Setting(list, "b", 0);

    // quantazation parameter, 0 is VBR (recommended)
    public final Setting qp = new Setting(list, "qp", 0);

    // intra-refresh interval (time between full redraws)
    public final Setting intra = new Setting(list, "g", 150);

    // h264 profile (base, main, high)
    public final Setting profile = new Setting(list, "pf", "high");

    // h264 level
    public final Setting level = new Setting(list, "lev", null);

    // include inline headers
    public final ValuelessSetting inlineHeaders = new ValuelessSetting(list, "ih", true);

    // flush buffers
    public final ValuelessSetting flush = new ValuelessSetting(list, "fl", true);

    // video stabilization
    public final ValuelessSetting stabilization = new ValuelessSetting(list, "vs", null);

    // average white balance
    public final Setting awb = new Setting(list, "awb", "fluorescent");

    // dynamic range compensation
    public final Setting drc = new Setting(list, "drc", "med");

    // exposure mode
    public final Setting exposure = new Setting(list, "ex", "antishake");

    // rotation (0, 90, 180, 270)
    public final Setting rotation = new Setting(list, "rot", 0);
}
