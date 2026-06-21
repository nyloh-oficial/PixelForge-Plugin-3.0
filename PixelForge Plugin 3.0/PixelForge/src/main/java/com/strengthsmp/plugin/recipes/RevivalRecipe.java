package com.strengthsmp.plugin.recipes;

import com.strengthsmp.plugin.StrengthSMP;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * The Resurrection Totem - deliberately expensive: 4 Wither Skeleton Skulls,
 * 2 Totems of Undying, 4 Diamonds, and a Nether Star.
 *
 * Recipe shape:
 *   S T S
 *   D N D
 *   S T S
 */
public class RevivalRecipe {

    public static ItemStack createResultItem(StrengthSMP plugin) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("\u00a7d\u00a7l\u2726 Resurrection Totem \u2726"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("\u00a77Right-click to open the season's"));
        lore.add(Component.text("\u00a77banned players and bring one back."));
        lore.add(Component.text("\u00a78Consumed on use."));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(plugin.getRevivalItemKey(), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static void register(StrengthSMP plugin) {
        ItemStack result = createResultItem(plugin);
        NamespacedKey key = new NamespacedKey(plugin, "resurrection_totem");
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape("STS", "DND", "STS");
        recipe.setIngredient('S', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('N', Material.NETHER_STAR);

        Bukkit.addRecipe(recipe);
    }
}
