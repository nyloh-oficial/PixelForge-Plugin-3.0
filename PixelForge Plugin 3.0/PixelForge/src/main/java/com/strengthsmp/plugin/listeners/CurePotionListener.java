package com.strengthsmp.plugin.listeners;

import com.strengthsmp.plugin.StrengthSMP;
import com.strengthsmp.plugin.util.CurePotionFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/** Right-clicking a Vial of Cleansing lifts one random curse. */
public class CurePotionListener implements Listener {

    private final StrengthSMP plugin;

    public CurePotionListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUseCurePotion(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!CurePotionFactory.isCurePotion(plugin, item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        boolean cured = plugin.getEffectManager().cureRandomCurse(player);
        if (!cured) {
            player.sendMessage("\u00a77You don't have any curses to cure right now.");
            return;
        }

        item.setAmount(item.getAmount() - 1);
    }
}
