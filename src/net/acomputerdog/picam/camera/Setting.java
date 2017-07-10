package net.acomputerdog.picam.camera;

public class Setting {
    private final String name;
    private String value;
    private boolean included;

    public Setting(SettingsList settings, String name) {
        this(settings, name, null);
    }

    public Setting(SettingsList settings, String name, Object value) {
        this.name = name;
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

    public void setValue(Object value) {
        this.value = String.valueOf(value);
        if (value == null) {
            included = false;
        } else {
            included = true;
        }
    }
}
