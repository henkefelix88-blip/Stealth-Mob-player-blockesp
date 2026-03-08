package de.sodium.highlighter.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class HighlightConfig {

    public enum HighlightType { ENTITY, BLOCK, PLAYER }

    public static class HighlightEntry {
        public String id;           // e.g. "minecraft:creeper" or player name
        public HighlightType type;
        public int color;           // ARGB packed int
        public float opacity;       // 0.0 - 1.0
        public boolean throughWalls;
        public boolean enabled;

        public HighlightEntry(String id, HighlightType type, int color, float opacity, boolean throughWalls) {
            this.id = id;
            this.type = type;
            this.color = color;
            this.opacity = opacity;
            this.throughWalls = throughWalls;
            this.enabled = true;
        }
    }

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("sodium-highlighter.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean globalEnabled = true;
    public List<HighlightEntry> entries = new ArrayList<>();

    private static HighlightConfig instance;

    public static HighlightConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    public static HighlightConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                HighlightConfig cfg = GSON.fromJson(r, HighlightConfig.class);
                if (cfg != null) { instance = cfg; return cfg; }
            } catch (Exception ignored) {}
        }
        instance = new HighlightConfig();
        instance.addDefaults();
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

    private void addDefaults() {
        // Default: highlight diamonds blue, creepers green, players red
        entries.add(new HighlightEntry("minecraft:diamond_ore",   HighlightType.BLOCK,  0xFF00AAFF, 0.8f, true));
        entries.add(new HighlightEntry("minecraft:deepslate_diamond_ore", HighlightType.BLOCK, 0xFF0077CC, 0.8f, true));
        entries.add(new HighlightEntry("minecraft:creeper",       HighlightType.ENTITY, 0xFF00FF44, 0.8f, false));
        entries.add(new HighlightEntry("minecraft:player",        HighlightType.PLAYER, 0xFFFF3333, 0.8f, false));
    }

    public List<HighlightEntry> getByType(HighlightType type) {
        return entries.stream().filter(e -> e.type == type && e.enabled).toList();
    }

    public void addEntry(HighlightEntry entry) {
        entries.add(entry);
        save();
    }

    public void removeEntry(HighlightEntry entry) {
        entries.remove(entry);
        save();
    }
}
