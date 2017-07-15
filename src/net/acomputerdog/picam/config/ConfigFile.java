package net.acomputerdog.picam.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigFile {
    private final Map<String, ConfigItem> configMap = new HashMap<>();

    public ConfigFile(BufferedReader reader) throws IOException {
        long lineNum = 0;
        while (reader.ready()) {
            String line = reader.readLine();
            String trimmed = line.trim();

            // skip comments
            if (!trimmed.startsWith("#")) {
                int eqIdx = trimmed.indexOf('=');
                if (eqIdx > 0) {
                    String key = trimmed.substring(0, eqIdx);

                    ConfigItem item;
                    if (trimmed.length() - eqIdx > 1) {
                        // has a value
                        String value = trimmed.substring(eqIdx + 1);
                        item = new ConfigItem(key, value);
                    } else {
                        // has no value
                        item = new ConfigItem(key);
                    }

                    configMap.put(key, item);
                } else if (eqIdx == 0) {
                    System.err.printf("Config error: line %d (%s) has no key.\n", lineNum, line);
                } else {
                    System.err.printf("Config error: line %d (%s) has no key or value.\n", lineNum, line);
                }
            }
            lineNum++;
        }
    }

    public ConfigItem getItem(String key) {
        return configMap.get(key);
    }

    public String getString(String key, String def) {
        String str = getString(key);
        if (str == null) {
            return def;
        } else {
            return str;
        }
    }

    public String getString(String key) {
        ConfigItem item = configMap.get(key);
        if (item == null) {
            return null;
        }
        if (!item.isSet()) {
            return null;
        }
        return item.getValue();
    }

    public int getInt(String key, int def) {
        ConfigItem item = configMap.get(key);
        if (item == null) {
            return def;
        }
        if (!item.isSet()) {
            return def;
        }
        return Integer.parseInt(item.getValue());
    }

    public int getInt(String key) {
        ConfigItem item = configMap.get(key);
        if (item == null) {
            return -1;
        }
        if (!item.isSet()) {
            return -1;
        }
        return Integer.parseInt(item.getValue());
    }

    public float getFloat(String key, float def) {
        ConfigItem item = configMap.get(key);
        if (item == null) {
            return def;
        }
        if (!item.isSet()) {
            return def;
        }
        return Float.parseFloat(item.getValue());
    }

    public float getFloat(String key) {
        ConfigItem item = configMap.get(key);
        if (item == null) {
            return Float.NaN;
        }
        if (!item.isSet()) {
            return Float.NaN;
        }
        return Float.parseFloat(item.getValue());
    }

    public boolean getBoolean(String key, boolean def) {
        ConfigItem item = configMap.get(key);
        if (item == null) {
            return def;
        }
        if (!item.isSet()) {
            return def;
        }
        return "true".equals(item.getValue());
    }

    public boolean getBoolean(String key) {
        ConfigItem item = configMap.get(key);
        if (item == null) {
            return false;
        }
        if (!item.isSet()) {
            return false;
        }
        return "true".equals(item.getValue());
    }
}
