package com.example.koth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class KothListener implements Listener {

    private final GameManager gameManager;

    public KothListener(GameManager gm) {
        this.gameManager = gm;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        gameManager.leave(e.getPlayer());
    }
}
