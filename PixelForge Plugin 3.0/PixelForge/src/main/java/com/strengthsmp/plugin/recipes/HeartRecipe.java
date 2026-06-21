package com.strengthsmp.plugin.recipes;

import com.strengthsmp.plugin.StrengthSMP;
import com.strengthsmp.plugin.util.HeartItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

/**
 * The Heart Crystal - a deliberately expensive way to gain an extra heart
 * without needing a PvP kill/death: 4 Golden Apples, 4 Diamonds, and a
 * Totem of Undying.
 *
 * Recipe shape:
 *   G D G
 *   D T D
 *   G D G
 */
public class HeartRecipe {

    public static void register(StrengthSMP plugin) {
        ItemStack result = HeartItemFactory.createCraftable(plugin);
        NamespacedKey key = new NamespacedKey(plugin, "heart_crystal");
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape("GDG", "DTD", "GDG");
        recipe.setIngredient('G', Material.GOLDEN_APPLE);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);

        Bukkit.addRecipe(recipe);
    }
}
