package net.acomputerdog.picam.camera.setting;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    protected final SettingsList list = new SettingsList();

    public void addToCommandLine(List<String> cmd) {
        for (Setting setting : list.getAllSettings()) {
            if (setting.isIncluded()) {
                cmd.add("-" + setting.getKey());

                if (setting.getValue() != null) {
                    cmd.add(setting.getValue());
                }
            }
        }
    }

    public String[] addToCommandLine() {
        List<String> cmd = new ArrayList<>();
        addToCommandLine(cmd);
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

    public SettingsList getList() {
        return list;
    }
}
