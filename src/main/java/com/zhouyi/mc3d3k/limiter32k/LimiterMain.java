package com.zhouyi.mc3d3k.limiter32k;

import com.zhouyi.mc3d3k.limiter32k.commands.LimiterCommand;
import com.zhouyi.mc3d3k.limiter32k.events.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LimiterMain extends JavaPlugin {
    private static LimiterMain INSTANCE;
    public static boolean isEnabled;

    // 各检测模块开关
    public static boolean detectAbnormalNBT;
    public static boolean detectAbnormalEnchantment;
    public static boolean detectAbnormalAmount;
    public static boolean detectUnbreakable;
    public static boolean detectIllegalEnchantments;
    public static boolean detectExtremeEnchantment;
    public static boolean detectHideFlags;
    public static boolean detectAbnormalNameLore;
    public static boolean detectAbnormalFoodEffects;
    public static boolean detectNonOpSpawnEgg;

    public static LimiterMain getInstance() {
        return INSTANCE;
    }

    private void loadDetectionsConfig() {
        detectAbnormalNBT = getConfig().getBoolean("detections.abnormal-nbt", true);
        detectAbnormalEnchantment = getConfig().getBoolean("detections.abnormal-enchantment", true);
        detectAbnormalAmount = getConfig().getBoolean("detections.abnormal-amount", true);
        detectUnbreakable = getConfig().getBoolean("detections.unbreakable", true);
        detectIllegalEnchantments = getConfig().getBoolean("detections.illegal-enchantments", true);
        detectExtremeEnchantment = getConfig().getBoolean("detections.extreme-enchantment", true);
        detectHideFlags = getConfig().getBoolean("detections.hide-flags", true);
        detectAbnormalNameLore = getConfig().getBoolean("detections.abnormal-name-lore", true);
        detectAbnormalFoodEffects = getConfig().getBoolean("detections.abnormal-food-effects", true);
        detectNonOpSpawnEgg = getConfig().getBoolean("detections.remove-spawn-egg-for-non-op", true);
    }

    public void reload() {
        reloadConfig();
        isEnabled = getConfig().getBoolean("enabled");
        loadDetectionsConfig();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        isEnabled = getConfig().getBoolean("enabled");
        loadDetectionsConfig();
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        if (Bukkit.getPluginCommand("limiter") != null) {
            Bukkit.getPluginCommand("limiter").setExecutor(new LimiterCommand());
            Bukkit.getPluginCommand("limiter").setTabCompleter(new LimiterCommand());
        }
    }
}
