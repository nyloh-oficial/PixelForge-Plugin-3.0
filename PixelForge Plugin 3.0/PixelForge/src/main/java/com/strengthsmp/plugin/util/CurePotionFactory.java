package com.strengthsmp.plugin.util;

import com.strengthsmp.plugin.StrengthSMP;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/** Builds the "Vial of Cleansing" - a craftable potion that lifts one random curse on right-click. */
public class CurePotionFactory {

    public static ItemStack create(StrengthSMP plugin) {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.displayName(Component.text("\u00a7b\u2726 Vial of Cleansing \u2726"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("\u00a77Right-click to lift one random curse."));
        meta.lore(lore);
        meta.setColor(Color.AQUA);

        meta.getPersistentDataContainer().set(plugin.getCurePotionKey(), PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isCurePotion(StrengthSMP plugin, ItemStack item) {
        if (item == null || item.getType() != Material.POTION || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(plugin.getCurePotionKey(), PersistentDataType.BYTE);
    }
}
