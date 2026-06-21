package com.strengthsmp.plugin.listeners;

import com.strengthsmp.plugin.StrengthSMP;
import com.strengthsmp.plugin.util.HeartItemFactory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/** Right-clicking a dropped Heart item grants +1 max heart, capped at 20 hearts (40 HP). */
public class HeartItemListener implements Listener {

    private static final double HEART = 2.0;
    private static final double MAX_HEALTH = 40.0; // 20 hearts

    private final StrengthSMP plugin;

    public HeartItemListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUseHeart(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!HeartItemFactory.isHeartItem(plugin, item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        double current = maxHealthAttr.getBaseValue();

        if (current >= MAX_HEALTH) {
            player.sendMessage("\u00a7c\u00a7lYou're already at the max of 20 hearts!");
            return;
        }

        double newMax = Math.min(current + HEART, MAX_HEALTH);
        maxHealthAttr.setBaseValue(newMax);
        player.setHealth(Math.min(player.getHealth() + HEART, newMax));

        item.setAmount(item.getAmount() - 1);
        player.sendMessage("\u00a7a\u00a7l+1 Heart! \u00a77You now have " + (int) (newMax / HEART) + " hearts.");
    }
}
