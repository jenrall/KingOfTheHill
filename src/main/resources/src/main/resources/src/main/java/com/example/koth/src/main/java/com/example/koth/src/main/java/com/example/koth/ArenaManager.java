package com.example.koth;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ArenaManager {

    private final KingOfTheHill plugin;
    private final Map<String, Arena> arenas = new HashMap<>();

    public ArenaManager(KingOfTheHill plugin) {
        this.plugin = plugin;
        loadArenas();
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public boolean createArena(String name) {
        if (arenas.containsKey(name.toLowerCase())) return false;
        FileConfiguration cfg = plugin.getConfig();
        int sec = cfg.getInt("defaults.capture-seconds", 30);
        Material mat = Material.matchMaterial(cfg.getString("defaults.reward-material", "DIAMOND"));
        if (mat == null) mat = Material.DIAMOND;
        int amount = cfg.getInt("defaults.reward-amount", 5);

        arenas.put(name.toLowerCase(), new Arena(name, sec, mat, amount));
        return true;
    }

    public boolean deleteArena(String name) {
        if (!arenas.containsKey(name.toLowerCase())) return false;
        arenas.remove(name.toLowerCase());
        plugin.getConfig().set("arenas." + name.toLowerCase(), null);
        plugin.saveConfig();
        return true;
    }

    public void saveArenas() {
        FileConfiguration cfg = plugin.getConfig();
        for (Arena a : arenas.values()) {
            String path = "arenas." + a.getName().toLowerCase();
            cfg.set(path + ".pos1", a.getPos1());
            cfg.set(path + ".pos2", a.getPos2());
            cfg.set(path + ".center", a.getCenter());
            cfg.set(path + ".capture-seconds", a.getCaptureSeconds());
            cfg.set(path + ".reward-material", a.getRewardMaterial().name());
            cfg.set(path + ".reward-amount", a.getRewardAmount());
        }
        plugin.saveConfig();
    }

    private void loadArenas() {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection section = cfg.getConfigurationSection("arenas");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String path = "arenas." + key;
            int sec = cfg.getInt(path + ".capture-seconds", 30);
            Material mat = Material.matchMaterial(cfg.getString(path + ".reward-material", "DIAMOND"));
            if (mat == null) mat = Material.DIAMOND;
            int amount = cfg.getInt(path + ".reward-amount", 5);

            Arena arena = new Arena(key, sec, mat, amount);
            arena.setPos1(cfg.getLocation(path + ".pos1"));
            arena.setPos2(cfg.getLocation(path + ".pos2"));
            arena.setCenter(cfg.getLocation(path + ".center"));
            arenas.put(key, arena);
        }
        plugin.getLogger().info("Loaded " + arenas.size() + " arena(s).");
    }

    public Map<String, Arena> getArenas() { return arenas; }
}
