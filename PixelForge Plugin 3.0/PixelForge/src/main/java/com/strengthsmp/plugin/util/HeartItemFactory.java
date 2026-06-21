package com.strengthsmp.plugin.util;

import com.strengthsmp.plugin.StrengthSMP;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/** Builds the "Heart" item that drops when a player dies - a head wearing the victim's own face. */
public class HeartItemFactory {

    public static ItemStack create(StrengthSMP plugin, OfflinePlayer victim) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(victim);
        meta.displayName(Component.text("\u00a7c\u2764 " + victim.getName() + "'s Heart"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("\u00a77Right-click to claim an extra heart"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(plugin.getHeartItemKey(), PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(plugin.getHeartOwnerKey(), PersistentDataType.STRING, victim.getUniqueId().toString());

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHeartItem(StrengthSMP plugin, ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(plugin.getHeartItemKey(), PersistentDataType.BYTE);
    }

    /** Builds the craftable "Heart Crystal" - works exactly like a death-drop heart when right-clicked. */
    public static ItemStack createCraftable(StrengthSMP plugin) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.displayName(Component.text("\u00a7c\u2764 Heart Crystal"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("\u00a77Right-click to claim an extra heart"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(plugin.getHeartItemKey(), PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }
}
