package com.example.koth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KothCommand implements CommandExecutor {

    private final KingOfTheHill plugin;
    private final GameManager gameManager;
    private final ArenaManager arenaManager;

    public KothCommand(KingOfTheHill plugin, GameManager gm, ArenaManager am) {
        this.plugin = plugin;
        this.gameManager = gm;
        this.arenaManager = am;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) { showHelp(player); return true; }

        switch (args[0].toLowerCase()) {
            case "help" -> showHelp(player);
            case "join" -> gameManager.join(player);
            case "leave" -> gameManager.leave(player);
            case "list" -> listArenas(player);
            case "create" -> createArena(player, args);
            case "delete" -> deleteArena(player, args);
            case "pos1" -> setPos1(player, args);
            case "pos2" -> setPos2(player, args);
            case "setcenter" -> setCenter(player, args);
            case "setduration" -> setDuration(player, args);
            case "setreward" -> setReward(player, args);
            case "start" -> startGame(player, args);
            case "stop" -> stopGame(player);
            case "reload" -> reloadConfig(player);
            default -> player.sendMessage(Component.text("دستور ناشناخته. /koth help", NamedTextColor.RED));
        }
        return true;
    }

    private void showHelp(Player p) {
        p.sendMessage(Component.text("=== King of the Hill ===", NamedTextColor.GOLD));
        p.sendMessage(Component.text("/koth join - ورود به بازی", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth leave - خروج", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth list - لیست آرناها", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth create <name> - ساخت آرنا", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth delete <name> - حذف آرنا", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth pos1 <arena> - گوشه ۱", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth pos2 <arena> - گوشه ۲", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth setcenter <arena> - مرکز تلپورت", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth setduration <arena> <sec> - زمان کنترل", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth setreward <arena> <mat> <amt> - جایزه", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth start <arena> - شروع", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth stop - توقف", NamedTextColor.YELLOW));
        p.sendMessage(Component.text("/koth reload - ری‌لود کانفیگ", NamedTextColor.YELLOW));
    }

    private void listArenas(Player p) {
        if (arenaManager.getArenas().isEmpty()) {
            p.sendMessage(Component.text("هیچ آرنایی وجود نداره.", NamedTextColor.RED));
            return;
        }
        p.sendMessage(Component.text("=== Arenas ===", NamedTextColor.GOLD));
        arenaManager.getArenas().forEach((name, a) ->
            p.sendMessage(Component.text("- " + name + " | " + a.getCaptureSeconds() + "s | " +
                a.getRewardMaterial().name() + " x" + a.getRewardAmount(), NamedTextColor.YELLOW))
        );
    }

    private void createArena(Player p, String[] args) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /koth create <name>", NamedTextColor.RED)); return; }
        if (arenaManager.createArena(args[1])) {
            p.sendMessage(Component.text("آرنا «" + args[1] + "» ساخته شد.", NamedTextColor.GREEN));
        } else {
            p.sendMessage(Component.text("این آرنا از قبل وجود داره.", NamedTextColor.RED));
        }
    }

    private void deleteArena(Player p, String[] args) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /koth delete <name>", NamedTextColor.RED)); return; }
        if (arenaManager.deleteArena(args[1])) {
            p.sendMessage(Component.text("آرنا حذف شد.", NamedTextColor.GREEN));
        } else {
            p.sendMessage(Component.text("آرنا پیدا نشد.", NamedTextColor.RED));
        }
    }

    private void setPos1(Player p, String[] args) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /koth pos1 <arena>", NamedTextColor.RED)); return; }
        Arena a = arenaManager.getArena(args[1]);
        if (a == null) { p.sendMessage(Component.text("آرنا پیدا نشد.", NamedTextColor.RED)); return; }
        a.setPos1(p.getLocation());
        arenaManager.saveArenas();
        p.sendMessage(Component.text("pos1 تنظیم شد.", NamedTextColor.GREEN));
    }

    private void setPos2(Player p, String[] args) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /koth pos2 <arena>", NamedTextColor.RED)); return; }
        Arena a = arenaManager.getArena(args[1]);
        if (a == null) { p.sendMessage(Component.text("آرنا پیدا نشد.", NamedTextColor.RED)); return; }
        a.setPos2(p.getLocation());
        arenaManager.saveArenas();
        p.sendMessage(Component.text("pos2 تنظیم شد.", NamedTextColor.GREEN));
    }

    private void setCenter(Player p, String[] args) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /koth setcenter <arena>", NamedTextColor.RED)); return; }
        Arena a = arenaManager.getArena(args[1]);
        if (a == null) { p.sendMessage(Component.text("آرنا پیدا نشد.", NamedTextColor.RED)); return; }
        a.setCenter(p.getLocation());
        arenaManager.saveArenas();
        p.sendMessage(Component.text("مرکز تنظیم شد.", NamedTextColor.GREEN));
    }

    private void setDuration(Player p, String[] args) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        if (args.length < 3) { p.sendMessage(Component.text("Usage: /koth setduration <arena> <sec>", NamedTextColor.RED)); return; }
        Arena a = arenaManager.getArena(args[1]);
        if (a == null) { p.sendMessage(Component.text("آرنا پیدا نشد.", NamedTextColor.RED)); return; }
        try {
            a.setCaptureSeconds(Integer.parseInt(args[2]));
            arenaManager.saveArenas();
            p.sendMessage(Component.text("زمان تنظیم شد.", NamedTextColor.GREEN));
        } catch (NumberFormatException e) {
            p.sendMessage(Component.text("عدد نامعتبر.", NamedTextColor.RED));
        }
    }

    private void setReward(Player p, String[] args) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        if (args.length < 4) { p.sendMessage(Component.text("Usage: /koth setreward <arena> <mat> <amt>", NamedTextColor.RED)); return; }
        Arena a = arenaManager.getArena(args[1]);
        if (a == null) { p.sendMessage(Component.text("آرنا پیدا نشد.", NamedTextColor.RED)); return; }
        Material mat = Material.matchMaterial(args[2]);
        if (mat == null) { p.sendMessage(Component.text("Material نامعتبر.", NamedTextColor.RED)); return; }
        try {
            a.setRewardMaterial(mat);
            a.setRewardAmount(Integer.parseInt(args[3]));
            arenaManager.saveArenas();
            p.sendMessage(Component.text("جایزه تنظیم شد.", NamedTextColor.GREEN));
        } catch (NumberFormatException e) {
            p.sendMessage(Component.text("تعداد نامعتبر.", NamedTextColor.RED));
        }
    }

    private void startGame(Player p, String[] args) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        if (args.length < 2) { p.sendMessage(Component.text("Usage: /koth start <arena>", NamedTextColor.RED)); return; }
        if (!gameManager.startGame(args[1])) {
            p.sendMessage(Component.text("شروع نشد. آرنا چک کن.", NamedTextColor.RED));
        }
    }

    private void stopGame(Player p) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        gameManager.stopGame(false);
        p.sendMessage(Component.text("بازی متوقف شد.", NamedTextColor.YELLOW));
    }

    private void reloadConfig(Player p) {
        if (!p.hasPermission("koth.admin")) { noPerm(p); return; }
        plugin.reloadConfig();
        p.sendMessage(Component.text("کانفیگ ری‌لود شد.", NamedTextColor.GREEN));
    }

    private void noPerm(Player p) {
        p.sendMessage(Component.text("دسترسی نداری.", NamedTextColor.RED));
    }
}
