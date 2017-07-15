package net.acomputerdog.picam.config;

public class ConfigItem {
    private final String key;
    private final String value;
    private final boolean isSet;

    public ConfigItem(String key) {
        this.key = key;
        this.value = null;
        this.isSet = false;
    }

    public ConfigItem(String key, String value) {
        this.key = key;
        this.value = value;
        this.isSet = true;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isSet() {
        return isSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigItem that = (ConfigItem) o;

        if (isSet != that.isSet) return false;
        if (!key.equals(that.key)) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (isSet ? 1 : 0);
        return result;
    }
}
