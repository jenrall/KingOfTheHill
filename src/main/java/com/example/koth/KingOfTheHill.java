package com.example.koth;

import org.bukkit.plugin.java.JavaPlugin;

public final class KingOfTheHill extends JavaPlugin {

    private GameManager gameManager;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.arenaManager = new ArenaManager(this);
        this.gameManager = new GameManager(this, arenaManager);

        getCommand("koth").setExecutor(new KothCommand(this, gameManager, arenaManager));
        getServer().getPluginManager().registerEvents(new KothListener(gameManager), this);

        getLogger().info("KingOfTheHill v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.stopGame(false);
        if (arenaManager != null) arenaManager.saveArenas();
    }
}
