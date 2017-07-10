package net.acomputerdog.picam.camera;

import java.util.ArrayList;
import java.util.List;

public class CameraSettings {
    private final SettingsList list = new SettingsList();

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

    public void buildCommandLine(List<String> cmd) {
        for (Setting setting : list.getAllSettings()) {
            if (setting.isIncluded()) {
                cmd.add("-" + setting.getKey());

                if (setting.getValue() != null) {
                    cmd.add(setting.getValue());
                }
            }
        }
    }

    public String[] buildCommandLine() {
        List<String> cmd = new ArrayList<>();
        buildCommandLine(cmd);
        return cmd.toArray(new String[cmd.size()]);
    }
    
    public void addSettingPairs(String[] pairs) {
        for (String pair : pairs) {
            int split = pair.indexOf('=');
            String key;
            String val;
            if (split > -1 && pair.length() - split > 1) {
                key = pair.substring(0, split);
                val = pair.substring(split + 1);
            } else {
                key = pair;
                val = null;
            }
            Setting setting = list.getSetting(key);
            if (setting != null) {
                setting.setValue(val);
            } else {
                System.out.println("Ignoring unknown setting pair: " + pair);
            }
        }
    }
}
