package com.zhouyi.mc3d3k.limiter32k;

import com.zhouyi.mc3d3k.limiter32k.utils.BanManager;
import com.zhouyi.mc3d3k.limiter32k.commands.LimiterCommand;
import com.zhouyi.mc3d3k.limiter32k.events.EventListener;
import com.zhouyi.mc3d3k.limiter32k.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LimiterMain extends JavaPlugin {
    private static LimiterMain INSTANCE;
    private static BanManager banManager;
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

    // 新增检测模块
    public static boolean detectInvalidPotionType;
    public static boolean detectInvalidItemModel;
    public static boolean detectCustomMapID;
    public static boolean detectExtremePotionEffects;
    public static boolean detectCustomModelData;
    public static boolean detectCreativeOnlyItem;

    // 高级执行模式开关 — 仅控制台通过 exeadd/exedel 控制
    public static boolean advancedExeMode;
    public static String advancedExePlayer; // 被授权的玩家名

    // 检测强度 (1-10)
    public static int detectionIntensity;

    public static LimiterMain getInstance() {
        return INSTANCE;
    }

    public static BanManager getBanManager() {
        return banManager;
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
        detectInvalidPotionType = getConfig().getBoolean("detections.invalid-potion-type", true);
        detectInvalidItemModel = getConfig().getBoolean("detections.invalid-item-model", true);
        detectCustomMapID = getConfig().getBoolean("detections.custom-map-id", true);
        detectExtremePotionEffects = getConfig().getBoolean("detections.extreme-potion-effects", true);
        detectCustomModelData = getConfig().getBoolean("detections.custom-model-data", true);
        detectCreativeOnlyItem = getConfig().getBoolean("detections.creative-only-item", true);
        detectionIntensity = getConfig().getInt("detection-intensity", 5);
        if (detectionIntensity < 1) detectionIntensity = 1;
        if (detectionIntensity > 10) detectionIntensity = 10;
        Utils.detectionIntensity = detectionIntensity;
    }
    @Override
    public void onEnable() {
        INSTANCE = this;

        getLogger().info(ChatColor.GOLD + "=========================================");
        getLogger().info(ChatColor.GOLD + "  " + ChatColor.AQUA + "32kLimiter" + ChatColor.GOLD + " by Zhouyi");
        getLogger().info(ChatColor.GOLD + "  " + ChatColor.GOLD + "github.com/ZhouyiStudio/32kLimiter");
        getLogger().info(ChatColor.GOLD + "  " + ChatColor.GOLD + "QQ: 823672854");
        getLogger().info(ChatColor.GOLD + "=========================================");

        saveDefaultConfig();
        banManager = new BanManager(this);
        getLogger().info(ChatColor.GREEN + "[1/7] 加载配置文件... 完成");

        isEnabled = getConfig().getBoolean("enabled");
        loadDetectionsConfig();
        getLogger().info(ChatColor.GREEN + "[2/7] 加载 " + getTotalDetections() + " 个检测模块 (已启用 " + countEnabledDetections() + "), 检测强度: " + detectionIntensity + "/10");

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        getLogger().info(ChatColor.GREEN + "[3/7] 注册事件监听器... 完成");

        // 启动定时任务：每 30 秒（600 tick）扫描在线玩家附近的屏障/光源方块并清除
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!isEnabled || !detectCreativeOnlyItem) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                int scanRadius = 8; // 每玩家半径 8 格
                int removed = EventListener.scanPlayerSurroundings(player, scanRadius);
                if (removed > 0) {
                    player.sendMessage(ChatColor.RED + "⚠ 已清除你附近的 " + removed + " 个屏障/光源方块");
                }
            }
        }, 200L, 600L); // 启动延迟 10s（200tick），每 30s（600tick）执行一次
        getLogger().info(ChatColor.GREEN + "[3.5/7] 启动屏障/光源方块扫描任务... 完成");

        if (Bukkit.getPluginCommand("limiter") != null) {
            Bukkit.getPluginCommand("limiter").setExecutor(new LimiterCommand());
            Bukkit.getPluginCommand("limiter").setTabCompleter(new LimiterCommand());
            getLogger().info(ChatColor.GREEN + "[4/7] 注册命令 /limiter /32klimiter... 完成");
        } else {
            getLogger().info(ChatColor.RED + "[4/7] 警告: limiter 命令未找到!");
        }

        getLogger().info(ChatColor.YELLOW + "[5/7] 插件主开关: " + (isEnabled ? ChatColor.GREEN + "启用" : ChatColor.RED + "禁用"));
        getLogger().info(ChatColor.YELLOW + "[6/7] 检测模块状态:");
        printDetectionStatus();
        getLogger().info(ChatColor.AQUA + "[7/7] 32kLimiter 启动完成!");
        getLogger().info(ChatColor.GOLD + "=========================================");
    }

    @Override
    public void onDisable() {
        if (banManager != null) {
            banManager.save();
        }
    }

    private void printDetectionStatus() {
        printOne(detectAbnormalNBT, "abnormal-nbt (异常NBT属性)");
        printOne(detectAbnormalEnchantment, "abnormal-enchantment (异常附魔等级)");
        printOne(detectAbnormalAmount, "abnormal-amount (异常堆叠数量)");
        printOne(detectUnbreakable, "unbreakable (不可破坏标签)");
        printOne(detectIllegalEnchantments, "illegal-enchantments (互斥附魔共存)");
        printOne(detectExtremeEnchantment, "extreme-enchantment (极端附魔等级)");
        printOne(detectHideFlags, "hide-flags (隐藏属性标志)");
        printOne(detectAbnormalNameLore, "abnormal-name-lore (异常名称Lore)");
        printOne(detectAbnormalFoodEffects, "abnormal-food-effects (异常食物效果)");
        printOne(detectNonOpSpawnEgg, "remove-spawn-egg-for-non-op (移除非OP刷怪蛋)");
        printOne(detectInvalidPotionType, "invalid-potion-type (无效药水类型)");
        printOne(detectInvalidItemModel, "invalid-item-model (无效物品模型)");
        printOne(detectCustomMapID, "custom-map-id (异常地图ID)");
        printOne(detectExtremePotionEffects, "extreme-potion-effects (极端药水效果)");
        printOne(detectCustomModelData, "custom-model-data (异常模型数据)");
        printOne(detectCreativeOnlyItem, "creative-only-item (创造专属物品)");
    }

    private void printOne(boolean on, String desc) {
        getLogger().info((on ? ChatColor.GREEN : ChatColor.RED) + "  " + (on ? "✔" : "✘") + " " + ChatColor.GRAY + desc);
    }

    private int countEnabledDetections() {
        int count = 0;
        if (detectAbnormalNBT) count++;
        if (detectAbnormalEnchantment) count++;
        if (detectAbnormalAmount) count++;
        if (detectUnbreakable) count++;
        if (detectIllegalEnchantments) count++;
        if (detectExtremeEnchantment) count++;
        if (detectHideFlags) count++;
        if (detectAbnormalNameLore) count++;
        if (detectAbnormalFoodEffects) count++;
        if (detectNonOpSpawnEgg) count++;
        if (detectInvalidPotionType) count++;
        if (detectInvalidItemModel) count++;
        if (detectCustomMapID) count++;
        if (detectCustomModelData) count++;
        if (detectCreativeOnlyItem) count++;
        return count;
    }

    private int getTotalDetections() {
        return 16;
    }

    // NOTE: reload() intentionally NOT annotated with @Override
    // JavaPlugin.reloadConfig() is available; custom reload() handles config sync.
    public void reload() {
        reloadConfig();
        isEnabled = getConfig().getBoolean("enabled");
        loadDetectionsConfig();
    }
}
