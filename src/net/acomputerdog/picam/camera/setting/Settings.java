package net.acomputerdog.picam.camera.setting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public abstract class Settings {
    public void addToCommandLine(List<String> cmd) {
        for (Field f : getClass().getDeclaredFields()) {
            try {
                if (!Modifier.isTransient(f.getModifiers()) && f.isAccessible()) {
                    // boolean fields have no value
                    if (f.getType().equals(boolean.class)) {
                        if ((Boolean) f.get(this)) {
                            cmd.add("-" + f.getName());
                        }
                    } else {
                        cmd.add("-" + f.getName());
                        cmd.add(String.valueOf(f.get(this)));
                    }
                }
            } catch (ReflectiveOperationException e) {
                System.err.println("Error reading setting field");
                e.printStackTrace();
            }
        }
    }

    public void mixIn(Settings settings) {
        for (Field f : settings.getClass().getDeclaredFields()) {
            try {
                if (!Modifier.isTransient(f.getModifiers()) && f.isAccessible()) {
                    try {
                        Field mine = getClass().getDeclaredField(f.getName());
                        if (!Modifier.isTransient(mine.getModifiers()) && mine.isAccessible()) {
                            mine.set(this, f.get(settings));
                        }
                    } catch (NoSuchFieldException ignored) {
                        // it throws this instead of returning null
                    }
                }
            } catch (ReflectiveOperationException e) {
                System.err.println("Error copying setting field");
                e.printStackTrace();
            }
        }
    }
}
