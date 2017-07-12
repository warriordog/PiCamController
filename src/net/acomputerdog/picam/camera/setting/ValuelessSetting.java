package net.acomputerdog.picam.camera.setting;

public class ValuelessSetting extends Setting {
    public ValuelessSetting(SettingsList settings, String name) {
        super(settings, name);
    }

    public ValuelessSetting(SettingsList settings, String name, Object value) {
        super(settings, name, value);
    }

    @Override
    public String getValue() {
        return null;
    }
}
