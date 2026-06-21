package com.strengthsmp.plugin.managers;

import com.strengthsmp.plugin.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Tracks who's "out for the season". This is a SOFT elimination, not a real
 * server ban - eliminated players can still log back in, but get dropped
 * into spectator mode until someone trades them a heart (/ssmp giveheart)
 * or revives them with a Resurrection Totem.
 */
public class BanManager {

    private final StrengthSMP plugin;
    private final File file;
    private final FileConfiguration config;

    // UUID -> last known username
    private final Map<UUID, String> banned = new LinkedHashMap<>();

    public BanManager(StrengthSMP plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "banned.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void banForSeason(OfflinePlayer player, String reason) {
        String name = player.getName() == null ? "Unknown" : player.getName();
        banned.put(player.getUniqueId(), name);
        save();

        Player online = player.getPlayer();
        if (online != null) {
            online.setGameMode(GameMode.SPECTATOR);
        }
    }

    public void revive(UUID id) {
        banned.remove(id);
        save();

        // Fresh start: wipe any leftover curses/blessings from before they died.
        plugin.getEffectManager().clearAll(id);

        Player online = Bukkit.getPlayer(id);
        if (online != null) {
            online.setGameMode(GameMode.SURVIVAL);
        }
    }

    public boolean isBanned(UUID id) {
        return banned.containsKey(id);
    }

    /** TESTING ONLY - un-eliminates everyone, no Resurrection Totem or trade needed. */
    public void clearAllBans() {
        for (UUID id : new ArrayList<>(banned.keySet())) {
            Player online = Bukkit.getPlayer(id);
            if (online != null) online.setGameMode(GameMode.SURVIVAL);
        }
        banned.clear();
        save();
    }

    public Map<UUID, String> getBanned() {
        return Collections.unmodifiableMap(banned);
    }

    private void load() {
        if (config.contains("banned")) {
            for (String key : config.getConfigurationSection("banned").getKeys(false)) {
                banned.put(UUID.fromString(key), config.getString("banned." + key));
            }
        }
    }

    public void save() {
        config.set("banned", null);
        for (Map.Entry<UUID, String> e : banned.entrySet()) {
            config.set("banned." + e.getKey(), e.getValue());
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
