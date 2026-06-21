package com.strengthsmp.plugin.listeners;

import com.strengthsmp.plugin.StrengthSMP;
import com.strengthsmp.plugin.gui.RevivalGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/** Right-clicking the Resurrection Totem opens the GUI of banned players. */
public class RevivalListener implements Listener {

    private final StrengthSMP plugin;

    public RevivalListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUseRevivalItem(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(plugin.getRevivalItemKey(), PersistentDataType.BYTE)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (plugin.getBanManager().getBanned().isEmpty()) {
            player.sendMessage("\u00a77No one is currently banned this season - nothing to revive.");
            return;
        }

        new RevivalGUI(plugin, player).open();
    }
}
