package com.strengthsmp.plugin.listeners;

import com.strengthsmp.plugin.StrengthSMP;
import com.strengthsmp.plugin.util.HeartItemFactory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Core death handling:
 *  - Victim gets cursed.
 *  - Killer (if PvP) gets cured of a curse, or blessed if they're already clean.
 *  - Victim loses 1 heart (2 max HP) and drops it on the ground.
 *  - If that was their last heart, they're eliminated for the season.
 */
public class DeathListener implements Listener {

    private static final double HEART = 2.0; // 1 heart = 2 max HP

    private final StrengthSMP plugin;

    public DeathListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        plugin.getEffectManager().applyCurseOnDeath(victim);

        if (killer != null) {
            plugin.getEffectManager().rewardKiller(killer);
        }

        AttributeInstance maxHealthAttr = victim.getAttribute(Attribute.MAX_HEALTH);
        double currentMax = maxHealthAttr.getBaseValue();
        double newMax = currentMax - HEART;

        if (newMax <= 0) {
            event.deathMessage(Component.text("\u00a7c" + victim.getName()
                    + " lost their final heart and is eliminated for the season!"));

            plugin.getBanManager().banForSeason(victim, "Eliminated from StrengthLifesteal - out of hearts");

            // Reset their max health now so it's correct whenever they're revived later.
            maxHealthAttr.setBaseValue(20.0);

            Bukkit.getScheduler().runTask(plugin, () ->
                    victim.kickPlayer("\u00a7c\u00a7lYou ran out of hearts!\n\u00a77A Resurrection Totem can bring you back."));
            return;
        }

        maxHealthAttr.setBaseValue(newMax);
        event.getDrops().add(HeartItemFactory.create(plugin, victim));
        victim.sendMessage("\u00a7c\u00a7lYou lost a heart! \u00a77It dropped where you died - someone can claim it.");
    }
}
