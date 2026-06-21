package com.strengthsmp.plugin.recipes;

import com.strengthsmp.plugin.StrengthSMP;
import com.strengthsmp.plugin.util.CurePotionFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

/**
 * The Vial of Cleansing - a deliberately expensive way to lift one curse
 * without needing a PvP kill: 4 Phantom Membranes, 4 Ghast Tears, and a
 * Diamond.
 *
 * Recipe shape:
 *   P G P
 *   G D G
 *   P G P
 */
public class CurePotionRecipe {

    public static void register(StrengthSMP plugin) {
        ItemStack result = CurePotionFactory.create(plugin);
        NamespacedKey key = new NamespacedKey(plugin, "vial_of_cleansing");
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        recipe.shape("PGP", "GDG", "PGP");
        recipe.setIngredient('P', Material.PHANTOM_MEMBRANE);
        recipe.setIngredient('G', Material.GHAST_TEAR);
        recipe.setIngredient('D', Material.DIAMOND);

        Bukkit.addRecipe(recipe);
    }
}
