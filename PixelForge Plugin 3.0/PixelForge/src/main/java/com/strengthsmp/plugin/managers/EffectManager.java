package com.strengthsmp.plugin.managers;

import com.strengthsmp.plugin.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles the curse/blessing loop:
 *  - Die -> gain a random "bad" effect (curse), permanent until cured.
 *  - Kill someone -> cure one of your own curses; if you have none, gain a
 *    random "good" effect (blessing) instead.
 */
public class EffectManager {

    private final StrengthSMP plugin;
    private final File file;
    private final FileConfiguration config;

    private static final int PERMANENT = Integer.MAX_VALUE;

    private static final List<PotionEffectType> BAD_EFFECTS = Arrays.asList(
            PotionEffectType.WEAKNESS,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.HUNGER,
            PotionEffectType.UNLUCK,
            PotionEffectType.BLINDNESS,
            PotionEffectType.NAUSEA,
            PotionEffectType.DARKNESS,
            PotionEffectType.GLOWING,
            PotionEffectType.LEVITATION,
            PotionEffectType.WEAVING,
            PotionEffectType.INFESTED,
            PotionEffectType.OOZING,
            PotionEffectType.POISON,
            PotionEffectType.WIND_CHARGED
    );

    private static final List<PotionEffectType> GOOD_EFFECTS = Arrays.asList(
            PotionEffectType.STRENGTH,
            PotionEffectType.SPEED,
            PotionEffectType.HASTE,
            PotionEffectType.REGENERATION,
            PotionEffectType.LUCK,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.SATURATION,
            PotionEffectType.RESISTANCE,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.ABSORPTION,
            PotionEffectType.DOLPHINS_GRACE
    );

    // Effects WE applied, tracked separately so we never touch effects from potions/other plugins
    private final Map<UUID, Set<PotionEffectType>> trackedBad = new HashMap<>();
    private final Map<UUID, Set<PotionEffectType>> trackedGood = new HashMap<>();

    private final Random random = new Random();

    public EffectManager(StrengthSMP plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "effects.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void applyCurseOnDeath(Player player) {
        Set<PotionEffectType> active = trackedBad.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        List<PotionEffectType> available = new ArrayList<>(BAD_EFFECTS);
        available.removeAll(active);

        PotionEffectType chosen = available.isEmpty()
                ? BAD_EFFECTS.get(random.nextInt(BAD_EFFECTS.size()))
                : available.get(random.nextInt(available.size()));

        active.add(chosen);
        player.addPotionEffect(new PotionEffect(chosen, PERMANENT, 0, false, true, true));
        player.sendMessage("\u00a7c\u00a7lCURSED! \u00a77You've been afflicted with \u00a7c" + format(chosen) + "\u00a77.");
        save();
    }

    public void rewardKiller(Player killer) {
        Set<PotionEffectType> bad = trackedBad.computeIfAbsent(killer.getUniqueId(), k -> new HashSet<>());

        if (!bad.isEmpty()) {
            PotionEffectType toRemove = bad.iterator().next();
            bad.remove(toRemove);
            killer.removePotionEffect(toRemove);
            killer.sendMessage("\u00a7a\u00a7lCURED! \u00a77Your \u00a7c" + format(toRemove) + " \u00a77curse has been lifted.");
        } else {
            Set<PotionEffectType> good = trackedGood.computeIfAbsent(killer.getUniqueId(), k -> new HashSet<>());
            List<PotionEffectType> available = new ArrayList<>(GOOD_EFFECTS);
            available.removeAll(good);

            PotionEffectType chosen;
            int amplifier = 0;
            if (available.isEmpty()) {
                chosen = GOOD_EFFECTS.get(random.nextInt(GOOD_EFFECTS.size()));
                PotionEffect current = killer.getPotionEffect(chosen);
                amplifier = current != null ? current.getAmplifier() + 1 : 0;
            } else {
                chosen = available.get(random.nextInt(available.size()));
            }

            good.add(chosen);
            killer.addPotionEffect(new PotionEffect(chosen, PERMANENT, amplifier, false, true, true));
            killer.sendMessage("\u00a7b\u00a7lBLESSED! \u00a77You've gained \u00a7b" + format(chosen) + "\u00a77.");
        }
        save();
    }

    /** Re-apply tracked curses/blessings on join since raw potion effects don't survive a restart. */
    public void reapply(Player player) {
        UUID id = player.getUniqueId();
        for (PotionEffectType t : trackedBad.getOrDefault(id, Collections.emptySet())) {
            player.addPotionEffect(new PotionEffect(t, PERMANENT, 0, false, true, true));
        }
        for (PotionEffectType t : trackedGood.getOrDefault(id, Collections.emptySet())) {
            player.addPotionEffect(new PotionEffect(t, PERMANENT, 0, false, true, true));
        }
    }

    /** Called when a player is revived - wipes their curse/blessing slate clean. */
    public void clearAll(UUID id) {
        trackedBad.remove(id);
        trackedGood.remove(id);
        save();
    }

    /** TESTING ONLY - wipes every tracked curse/blessing for every player, online or not. */
    public void clearEveryone() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (PotionEffectType t : BAD_EFFECTS) p.removePotionEffect(t);
            for (PotionEffectType t : GOOD_EFFECTS) p.removePotionEffect(t);
        }
        trackedBad.clear();
        trackedGood.clear();
        save();
    }

    /**
     * Called by the Vial of Cleansing - lifts one random curse from the player.
     * Returns false (and does nothing) if the player has no curses to lift.
     */
    public boolean cureRandomCurse(Player player) {
        Set<PotionEffectType> active = trackedBad.get(player.getUniqueId());
        if (active == null || active.isEmpty()) return false;

        List<PotionEffectType> options = new ArrayList<>(active);
        PotionEffectType toRemove = options.get(random.nextInt(options.size()));

        active.remove(toRemove);
        player.removePotionEffect(toRemove);
        player.sendMessage("\u00a7a\u00a7lCURED! \u00a77Your \u00a7c" + format(toRemove) + " \u00a77curse has been lifted.");
        save();
        return true;
    }

    private String format(PotionEffectType type) {
        String name = type.getKey().getKey().replace('_', ' ');
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private void load() {
        if (config.contains("bad")) {
            for (String key : config.getConfigurationSection("bad").getKeys(false)) {
                UUID id = UUID.fromString(key);
                Set<PotionEffectType> set = new HashSet<>();
                for (String name : config.getStringList("bad." + key)) {
                    PotionEffectType t = PotionEffectType.getByName(name);
                    if (t != null) set.add(t);
                }
                trackedBad.put(id, set);
            }
        }
        if (config.contains("good")) {
            for (String key : config.getConfigurationSection("good").getKeys(false)) {
                UUID id = UUID.fromString(key);
                Set<PotionEffectType> set = new HashSet<>();
                for (String name : config.getStringList("good." + key)) {
                    PotionEffectType t = PotionEffectType.getByName(name);
                    if (t != null) set.add(t);
                }
                trackedGood.put(id, set);
            }
        }
    }

    public void save() {
        for (Map.Entry<UUID, Set<PotionEffectType>> e : trackedBad.entrySet()) {
            List<String> names = new ArrayList<>();
            for (PotionEffectType t : e.getValue()) names.add(t.getName());
            config.set("bad." + e.getKey(), names);
        }
        for (Map.Entry<UUID, Set<PotionEffectType>> e : trackedGood.entrySet()) {
            List<String> names = new ArrayList<>();
            for (PotionEffectType t : e.getValue()) names.add(t.getName());
            config.set("good." + e.getKey(), names);
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
