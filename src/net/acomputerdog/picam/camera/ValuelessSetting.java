package net.acomputerdog.picam.camera;

public class ValuelessSetting extends Setting {
    public ValuelessSetting(SettingsList settings, String name, boolean supportsPicture) {
        super(settings, name, supportsPicture);
    }

    public ValuelessSetting(SettingsList settings, String name, Object value, boolean supportsPicture) {
        super(settings, name, value, supportsPicture);
    }

    @Override
    public String getValue() {
        return null;
    }
}
