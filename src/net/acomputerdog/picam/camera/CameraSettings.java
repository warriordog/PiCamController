package net.acomputerdog.picam.camera;

import java.util.ArrayList;
import java.util.List;

public class CameraSettings {
    private final SettingsList list = new SettingsList();

    // camera mode, 0 is auto
    public final Setting camMode = new Setting(list, "md", 1, true);

    // frames per second, limited by mode
    public final Setting fps = new Setting(list, "fps", 30, false);

    // width of video (limited by mode)
    public final Setting width = new Setting(list, "w", 1920, true);

    // height of video (limited by mode)
    public final Setting height = new Setting(list, "h", 1080, true);

    // stream bitrate, 0 is VBR (recommended)
    public final Setting bitrate = new Setting(list, "b", 0, false);

    // quantazation parameter, 0 is VBR (recommended)
    public final Setting qp = new Setting(list, "qp", 0, false);

    // intra-refresh interval (time between full redraws)
    public final Setting intra = new Setting(list, "g", 150, false);

    // h264 profile (base, main, high)
    public final Setting profile = new Setting(list, "pf", "high", false);

    // h264 level
    public final Setting level = new Setting(list, "lev", null, false);

    // include inline headers
    public final ValuelessSetting inlineHeaders = new ValuelessSetting(list, "ih", true, false);

    // flush buffers
    public final ValuelessSetting flush = new ValuelessSetting(list, "fl", true, false);

    // video stabilization
    public final ValuelessSetting stabilization = new ValuelessSetting(list, "vs", null, false);

    // average white balance
    public final Setting awb = new Setting(list, "awb", "fluorescent", true);

    // dynamic range compensation
    public final Setting drc = new Setting(list, "drc", "med", true);

    // exposure mode
    public final Setting exposure = new Setting(list, "ex", "antishake", true);

    public void buildPictureCommandLine(List<String> cmd) {
        for (Setting setting : list.getAllSettings()) {
            if (setting.isIncluded() && setting.supportsPicture()) {
                cmd.add("-" + setting.getKey());

                if (setting.getValue() != null) {
                    cmd.add(setting.getValue());
                }
            }
        }
    }

    public void buildVideoCommandLine(List<String> cmd) {
        for (Setting setting : list.getAllSettings()) {
            if (setting.isIncluded()) {
                cmd.add("-" + setting.getKey());

                if (setting.getValue() != null) {
                    cmd.add(setting.getValue());
                }
            }
        }
    }

    public String[] buildVideoCommandLine() {
        List<String> cmd = new ArrayList<>();
        buildVideoCommandLine(cmd);
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
