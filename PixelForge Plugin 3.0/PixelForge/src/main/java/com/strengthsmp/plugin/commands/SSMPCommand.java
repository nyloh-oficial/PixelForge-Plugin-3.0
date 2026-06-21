package com.strengthsmp.plugin.commands;

import com.strengthsmp.plugin.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Admin/testing command tree:
 *   /ssmp ban|unban|listbanned <player>  - manual elimination control
 *   /ssmp giveheart <player>             - give 1 of your hearts away; revives an eliminated player with 1 heart
 *   /ssmp testreset                      - TESTING ONLY: clears every effect/ban and restores everyone's hearts
 *
 * Each subcommand checks its own permission node, so individual players can
 * be granted access to specific commands (via a permissions plugin like
 * LuckPerms) without needing full server-admin/op status.
 */
public class SSMPCommand implements CommandExecutor {

    private static final double HEART = 2.0; // 1 heart = 2 max HP
    private static final double MAX_HEALTH_CAP = 40.0; // 20 hearts

    private final StrengthSMP plugin;

    public SSMPCommand(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("\u00a77Usage: /ssmp <ban|unban|listbanned|giveheart|testreset> [player]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "ban" -> {
                if (!sender.hasPermission("strengthsmp.admin.ban")) {
                    sender.sendMessage("\u00a7cYou don't have permission to do that.");
                    return true;
                }
                if (args.length < 2) { sender.sendMessage("\u00a7cUsage: /ssmp ban <player>"); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                plugin.getBanManager().banForSeason(target, "Manually banned by " + sender.getName());
                sender.sendMessage("\u00a7aBanned " + args[1] + " from the season.");
            }
            case "unban" -> {
                if (!sender.hasPermission("strengthsmp.admin.unban")) {
                    sender.sendMessage("\u00a7cYou don't have permission to do that.");
                    return true;
                }
                if (args.length < 2) { sender.sendMessage("\u00a7cUsage: /ssmp unban <player>"); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                plugin.getBanManager().revive(target.getUniqueId());
                sender.sendMessage("\u00a7aUnbanned " + args[1] + ".");
            }
            case "listbanned" -> {
                if (!sender.hasPermission("strengthsmp.admin.listbanned")) {
                    sender.sendMessage("\u00a7cYou don't have permission to do that.");
                    return true;
                }
                if (plugin.getBanManager().getBanned().isEmpty()) {
                    sender.sendMessage("\u00a77No one is banned this season.");
                } else {
                    sender.sendMessage("\u00a77Banned this season:");
                    for (Map.Entry<UUID, String> e : plugin.getBanManager().getBanned().entrySet()) {
                        sender.sendMessage(" \u00a7c- " + e.getValue());
                    }
                }
            }
            case "giveheart" -> {
                if (!sender.hasPermission("strengthsmp.giveheart")) {
                    sender.sendMessage("\u00a7cYou don't have permission to do that.");
                    return true;
                }
                if (!(sender instanceof Player from)) {
                    sender.sendMessage("\u00a7cOnly a player can give away one of their hearts.");
                    return true;
                }
                if (args.length < 2) { sender.sendMessage("\u00a7cUsage: /ssmp giveheart <player>"); return true; }

                Player to = Bukkit.getPlayer(args[1]);
                if (to == null) { sender.sendMessage("\u00a7cThat player needs to be online to receive a heart."); return true; }
                if (to.equals(from)) { sender.sendMessage("\u00a7cYou can't give a heart to yourself."); return true; }

                AttributeInstance fromAttr = from.getAttribute(Attribute.MAX_HEALTH);
                double fromMax = fromAttr.getBaseValue();
                if (fromMax - HEART < HEART) {
                    sender.sendMessage("\u00a7cYou need at least 2 hearts of your own to give one away.");
                    return true;
                }

                double newFromMax = fromMax - HEART;
                fromAttr.setBaseValue(newFromMax);
                if (from.getHealth() > newFromMax) from.setHealth(newFromMax);

                boolean wasBanned = plugin.getBanManager().isBanned(to.getUniqueId());
                AttributeInstance toAttr = to.getAttribute(Attribute.MAX_HEALTH);
                // If they were eliminated, ignore their stale stored max health (it was
                // reset to full at elimination time) and bring them back from scratch.
                double toBase = wasBanned ? 0.0 : toAttr.getBaseValue();
                double newToMax = Math.min(MAX_HEALTH_CAP, toBase + HEART);
                toAttr.setBaseValue(newToMax);
                to.setHealth(newToMax);

                if (wasBanned) {
                    plugin.getBanManager().revive(to.getUniqueId());
                    from.sendMessage("\u00a7a\u00a7lTRADED! \u00a77You gave a heart to " + to.getName() + " and brought them back into the season.");
                    to.sendMessage("\u00a7a\u00a7lBACK IN! \u00a77" + from.getName() + " traded you a heart - you're back with 1 heart.");
                    Bukkit.broadcast(net.kyori.adventure.text.Component.text("\u00a7d" + to.getName()
                            + " was traded back into the season by " + from.getName() + "!"));
                } else {
                    from.sendMessage("\u00a7a\u00a7lGIVEN! \u00a77You gave a heart to " + to.getName() + ".");
                    to.sendMessage("\u00a7a\u00a7lGIFTED! \u00a77" + from.getName() + " gave you a heart.");
                }
            }
            case "testreset" -> {
                if (!sender.hasPermission("strengthsmp.admin.testreset")) {
                    sender.sendMessage("\u00a7cYou don't have permission to do that.");
                    return true;
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    AttributeInstance maxHealthAttr = p.getAttribute(Attribute.MAX_HEALTH);
                    if (maxHealthAttr != null) {
                        maxHealthAttr.setBaseValue(20.0);
                        p.setHealth(20.0);
                    }
                    p.setGameMode(GameMode.SURVIVAL);
                }
                plugin.getEffectManager().clearEveryone();
                plugin.getBanManager().clearAllBans();
                sender.sendMessage("\u00a7a\u00a7lTEST RESET COMPLETE \u00a77- every online player's hearts and effects are back to default, and everyone's un-eliminated.");
            }
            default -> sender.sendMessage("\u00a77Usage: /ssmp <ban|unban|listbanned|giveheart|testreset> [player]");
        }
        return true;
    }
}
