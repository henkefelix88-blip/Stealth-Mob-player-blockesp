package de.sodium.highlighter.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class KeybindConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("sodium-keybinds.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // action key → GLFW key code
    public Map<String, Integer> bindings = new HashMap<>();

    private static KeybindConfig instance;

    public static KeybindConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    public static KeybindConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                KeybindConfig cfg = GSON.fromJson(r, KeybindConfig.class);
                if (cfg != null) {
                    instance = cfg;
                    // Fill any missing defaults
                    instance.applyDefaults();
                    return instance;
                }
            } catch (Exception ignored) {}
        }
        instance = new KeybindConfig();
        instance.resetDefaults();
        return instance;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetDefaults() {
        bindings.clear();
        applyDefaults();
    }

    private void applyDefaults() {
        bindings.putIfAbsent("open_gui",         GLFW.GLFW_KEY_H);
        bindings.putIfAbsent("toggle",           GLFW.GLFW_KEY_J);
        bindings.putIfAbsent("open_keybind_gui", GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public int getKey(String action) {
        return bindings.getOrDefault(action, GLFW.GLFW_KEY_UNKNOWN);
    }

    public void setKey(String action, int glfwKey) {
        bindings.put(action, glfwKey);
    }
}
