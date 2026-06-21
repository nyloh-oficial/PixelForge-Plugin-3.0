package com.strengthsmp.plugin;

import com.strengthsmp.plugin.commands.SSMPCommand;
import com.strengthsmp.plugin.listeners.CurePotionListener;
import com.strengthsmp.plugin.listeners.DeathListener;
import com.strengthsmp.plugin.listeners.HeartItemListener;
import com.strengthsmp.plugin.listeners.JoinListener;
import com.strengthsmp.plugin.listeners.RevivalListener;
import com.strengthsmp.plugin.managers.BanManager;
import com.strengthsmp.plugin.managers.EffectManager;
import com.strengthsmp.plugin.recipes.CurePotionRecipe;
import com.strengthsmp.plugin.recipes.HeartRecipe;
import com.strengthsmp.plugin.recipes.RevivalRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class StrengthSMP extends JavaPlugin {

    private static StrengthSMP instance;

    private BanManager banManager;
    private EffectManager effectManager;

    private NamespacedKey heartItemKey;
    private NamespacedKey heartOwnerKey;
    private NamespacedKey revivalItemKey;
    private NamespacedKey banTargetKey;
    private NamespacedKey curePotionKey;

    @Override
    public void onEnable() {
        instance = this;

        // Namespaced keys used to tag our custom items via PersistentDataContainer
        heartItemKey = new NamespacedKey(this, "heart_item");
        heartOwnerKey = new NamespacedKey(this, "heart_owner");
        revivalItemKey = new NamespacedKey(this, "revival_totem");
        banTargetKey = new NamespacedKey(this, "ban_target");
        curePotionKey = new NamespacedKey(this, "cure_potion");

        // Managers (load saved data)
        banManager = new BanManager(this);
        effectManager = new EffectManager(this);

        // Custom crafting recipes
        RevivalRecipe.register(this);
        HeartRecipe.register(this);
        CurePotionRecipe.register(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new HeartItemListener(this), this);
        getServer().getPluginManager().registerEvents(new RevivalListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CurePotionListener(this), this);

        // Admin command
        getCommand("ssmp").setExecutor(new SSMPCommand(this));

        getLogger().info("StrengthLifesteal enabled! Hearts, curses, and revivals are live.");
    }

    @Override
    public void onDisable() {
        if (banManager != null) banManager.save();
        if (effectManager != null) effectManager.save();
        getLogger().info("StrengthLifesteal disabled, data saved.");
    }

    public static StrengthSMP getInstance() { return instance; }
    public BanManager getBanManager() { return banManager; }
    public EffectManager getEffectManager() { return effectManager; }
    public NamespacedKey getHeartItemKey() { return heartItemKey; }
    public NamespacedKey getHeartOwnerKey() { return heartOwnerKey; }
    public NamespacedKey getRevivalItemKey() { return revivalItemKey; }
    public NamespacedKey getBanTargetKey() { return banTargetKey; }
    public NamespacedKey getCurePotionKey() { return curePotionKey; }
}
