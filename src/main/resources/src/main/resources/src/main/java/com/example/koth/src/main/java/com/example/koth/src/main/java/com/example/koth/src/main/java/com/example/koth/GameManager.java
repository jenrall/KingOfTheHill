package com.example.koth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final KingOfTheHill plugin;
    private final ArenaManager arenaManager;

    private final Set<UUID> participants = new HashSet<>();
    private Arena currentArena;
    private boolean running = false;
    private Player king;
    private int progressSeconds = 0;
    private BukkitTask task;
    private BossBar bossBar;
    private int particleTick = 0;

    public GameManager(KingOfTheHill plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.bossBar = Bukkit.createBossBar("King of the Hill", BarColor.YELLOW, BarStyle.SOLID);
    }

    public boolean isRunning() { return running; }
    public Arena getCurrentArena() { return currentArena; }
    public Set<UUID> getParticipants() { return participants; }

    public boolean join(Player player) {
        if (!running || currentArena == null) {
            player.sendMessage(Component.text("بازی در حال اجرا نیست.", NamedTextColor.RED));
            return false;
        }
        if (participants.contains(player.getUniqueId())) {
            player.sendMessage(Component.text("شما قبلاً وارد شدید.", NamedTextColor.YELLOW));
            return false;
        }
        participants.add(player.getUniqueId());
        bossBar.addPlayer(player);

        String msg = plugin.getConfig().getString("defaults.welcome-message", "&aوارد King of the Hill شدی!");
        player.sendMessage(Component.text(msg.replace("&a", ""), NamedTextColor.GREEN));

        if (currentArena.getCenter() != null) player.teleport(currentArena.getCenter());
        return true;
    }

    public void leave(Player player) {
        participants.remove(player.getUniqueId());
        bossBar.removePlayer(player);
    }

    public boolean startGame(String arenaName) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null) return false;
        if (running) return false;
        if (arena.getCenter() == null) return false;

        currentArena = arena;
        running = true;
        participants.clear();
        king = null;
        progressSeconds = 0;

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 20L);
        Bukkit.broadcast(Component.text("⚔ King of the Hill شروع شد! با /koth join وارد شو.", NamedTextColor.GOLD));
        return true;
    }

    public void stopGame(boolean announceWinner) {
        running = false;
        if (task != null) { task.cancel(); task = null; }

        if (announceWinner && king != null && currentArena != null) {
            Bukkit.broadcast(Component.text("👑 " + king.getName() + " پادشاه هیل شد!", NamedTextColor.GOLD));
            king.getInventory().addItem(new ItemStack(
                currentArena.getRewardMaterial(),
                currentArena.getRewardAmount()
            ));
        }

        for (UUID id : new HashSet<>(participants)) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) bossBar.removePlayer(p);
        }
        participants.clear();
        bossBar.removeAll();
        king = null;
        progressSeconds = 0;
        currentArena = null;
    }

    private void tick() {
        if (!running || currentArena == null) return;

        Player currentKing = null;
        for (UUID id : participants) {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline()) continue;
            if (currentArena.contains(p.getLocation())) {
                currentKing = p;
                break;
            }
        }

        if (currentKing != king) {
            king = currentKing;
            progressSeconds = 0;
        }

        if (king != null) {
            progressSeconds++;
            int req = currentArena.getCaptureSeconds();
            double pct = Math.min(1.0, (double) progressSeconds / req);

            bossBar.setProgress(pct);
            bossBar.setTitle(king.getName() + " - " + progressSeconds + "/" + req + "s");

            if (progressSeconds >= req) stopGame(true);
        } else {
            bossBar.setTitle("هیچ‌کس هیل رو کنترل نمی‌کنه");
            bossBar.setProgress(0.0);
        }

        if (plugin.getConfig().getBoolean("particles.enabled", true) && currentArena.getCenter() != null) {
            particleTick++;
            if (particleTick % 10 == 0) {
                try {
                    Particle particle = Particle.valueOf(
                        plugin.getConfig().getString("particles.type", "FLAME")
                    );
                    int amount = plugin.getConfig().getInt("particles.amount", 5);
                    Location c = currentArena.getCenter();
                    c.getWorld().spawnParticle(particle, c, amount, 1, 1, 1, 0);
                } catch (Exception ignored) {}
            }
        }
    }
}
