package net.acomputerdog.picam.camera;

public class Setting {
    private final String name;
    private final boolean supportsPicture;

    private String value;
    private boolean included;

    public Setting(SettingsList settings, String name) {
        this(settings, name, false);
    }

    public Setting(SettingsList settings, String name, boolean supportsPicture) {
        this(settings, name, null, supportsPicture);
    }

    public Setting(SettingsList settings, String name, Object value, boolean supportsPicture) {
        this.name = name;
        this.supportsPicture = supportsPicture;
        setValue(value);

        settings.addSetting(this);
    }


    public boolean isIncluded() {
        return included;
    }

    public String getKey() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean supportsPicture() {
        return supportsPicture;
    }

    public void setValue(Object value) {
        this.value = String.valueOf(value);
        if (value == null) {
            included = false;
        } else {
            included = true;
        }
    }
}
