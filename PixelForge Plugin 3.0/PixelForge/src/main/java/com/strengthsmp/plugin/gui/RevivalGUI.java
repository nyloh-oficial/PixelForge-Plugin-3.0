package com.strengthsmp.plugin.gui;

import com.strengthsmp.plugin.StrengthSMP;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** A GUI of player heads for everyone banned this season - click one to revive them. */
public class RevivalGUI implements Listener {

    private final StrengthSMP plugin;
    private final Player opener;
    private final Inventory inventory;
    private final Map<Integer, UUID> slotToPlayer = new HashMap<>();
    private boolean handled = false;

    public RevivalGUI(StrengthSMP plugin, Player opener) {
        this.plugin = plugin;
        this.opener = opener;

        Map<UUID, String> banned = plugin.getBanManager().getBanned();
        int size = Math.min(54, Math.max(9, (int) (Math.ceil(banned.size() / 9.0) * 9)));
        this.inventory = Bukkit.createInventory(null, size, Component.text("\u00a75\u00a7lChoose a Player to Revive"));

        int slot = 0;
        for (Map.Entry<UUID, String> entry : banned.entrySet()) {
            if (slot >= size) break;
            OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.displayName(Component.text("\u00a7e" + entry.getValue()));
            meta.lore(List.of(Component.text("\u00a77Click to bring back with 10 hearts")));
            meta.getPersistentDataContainer().set(plugin.getBanTargetKey(), PersistentDataType.STRING, entry.getKey().toString());
            head.setItemMeta(meta);

            inventory.setItem(slot, head);
            slotToPlayer.put(slot, entry.getKey());
            slot++;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        opener.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        if (handled) return;

        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        UUID targetId = slotToPlayer.get(event.getRawSlot());
        if (targetId == null) return;

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);

        ItemStack inHand = clicker.getInventory().getItemInMainHand();
        if (inHand.hasItemMeta() && inHand.getItemMeta().getPersistentDataContainer()
                .has(plugin.getRevivalItemKey(), PersistentDataType.BYTE)) {
            inHand.setAmount(inHand.getAmount() - 1);
        } else {
            clicker.sendMessage("\u00a7cYou need a Resurrection Totem in hand to revive someone.");
            return;
        }

        handled = true;
        plugin.getBanManager().revive(targetId);

        clicker.sendMessage("\u00a7a\u00a7lRevived! \u00a77" + target.getName() + " can rejoin with 10 hearts.");
        Bukkit.broadcast(Component.text("\u00a7d" + target.getName()
                + " has been brought back into the season by " + clicker.getName() + "!"));
        clicker.closeInventory();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
