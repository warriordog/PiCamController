package net.acomputerdog.picam.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsList {
    private final Map<String, Setting> keyMap;
    private final List<Setting> allSettings;

    private final List<Setting> allPictureSettings;

    public SettingsList() {
        keyMap = new HashMap<>();
        allSettings = new ArrayList<>();
        allPictureSettings = new ArrayList<>();
    }

    public void addSetting(Setting setting) {
        keyMap.put(setting.getKey(), setting);
        allSettings.add(setting);

        if (setting.supportsPicture()) {
            allPictureSettings.add(setting);
        }
    }

    public List<Setting> getAllSettings() {
        return allSettings;
    }

    public Setting getSetting(String key) {
        return keyMap.get(key);
    }
}
