package com.strengthsmp.plugin.listeners;

import com.strengthsmp.plugin.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Re-applies tracked curses/blessings whenever raw potion effects wouldn't
 * otherwise survive: on login (server restart) and on respawn (death wipes
 * all active potion effects in vanilla). Also keeps eliminated players in
 * spectator mode if they rejoin before being traded a heart or revived.
 */
public class JoinListener implements Listener {

    private final StrengthSMP plugin;

    public JoinListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getEffectManager().reapply(event.getPlayer());

        if (plugin.getBanManager().isBanned(event.getPlayer().getUniqueId())) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // Run next tick so the player is fully respawned before effects are reapplied.
        Bukkit.getScheduler().runTask(plugin, () ->
                plugin.getEffectManager().reapply(event.getPlayer()));
    }
}
