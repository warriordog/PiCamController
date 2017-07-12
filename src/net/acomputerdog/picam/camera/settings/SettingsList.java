package net.acomputerdog.picam.camera.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsList {
    private final Map<String, Setting> keyMap;
    private final List<Setting> allSettings;

    public SettingsList() {
        keyMap = new HashMap<>();
        allSettings = new ArrayList<>();
    }

    public void addSetting(Setting setting) {
        keyMap.put(setting.getKey(), setting);
        allSettings.add(setting);
    }

    public List<Setting> getAllSettings() {
        return allSettings;
    }

    public Setting getSetting(String key) {
        return keyMap.get(key);
    }
}
