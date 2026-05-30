package com.zhouyi.mc3d3k.limiter32k.utils;

import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {

    /** 检测强度 (1-10), 从 LimiterMain.detectionIntensity 同步 */
    public static int detectionIntensity = 5;

    /**
     * 根据检测强度从 10 级阈值数组中取值 (索引 0 = 强度 1, 索引 9 = 强度 10)
     */
    private int intensityThreshold(int[] levelMap) {
        int idx = Math.min(Math.max(detectionIntensity, 1), 10) - 1;
        return levelMap[idx];
    }

    // ========== 单模块检测方法，每种返回 boolean ==========

    public boolean checkAbnormalNBT(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            NBTCompoundList attrs = nbtItem.getCompoundList("AttributeModifiers");
            int threshold = intensityThreshold(new int[]{200, 150, 100, 50, 20, 10, 5, 3, 1, 0});
            if (attrs != null && attrs.size() > 0) {
                for (ReadWriteNBT attr : attrs) {
                    int amount = attr.getInteger("Amount");
                    if (threshold == 0 ? amount != 0 : Math.abs(amount) > threshold) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    public boolean checkAbnormalEnchantment(ItemStack item) {
        if (item != null) {
            Map<Enchantment, Integer> enchantments = item.getEnchantments();
            int threshold = intensityThreshold(new int[]{50, 40, 30, 20, 15, 12, 10, 8, 6, 5});
            for (Integer level : enchantments.values()) {
                if (level > threshold) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 检测物品堆叠数量是否超过该物品的正常最大堆叠值
     */
    public boolean checkAbnormalAmount(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            int maxStack = item.getMaxStackSize();
            int currentAmount = item.getAmount();
            int multiplier = intensityThreshold(new int[]{64, 32, 16, 8, 4, 3, 2, 1, 1, 1});
            return currentAmount > maxStack * multiplier;
        }
        return false;
    }

    /**
     * 检测物品是否带有 Unbreakable 标签（通常用于作弊物品）
     */
    public boolean checkUnbreakable(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("Unbreakable")) {
                return nbtItem.getBoolean("Unbreakable");
            }
            // 也检查 ItemMeta 中的 isUnbreakable
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.isUnbreakable()) {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * 检测互斥附魔组合（非法附魔共存）
     */
    public boolean checkIllegalEnchantments(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            Map<Enchantment, Integer> enchantments = item.getEnchantments();
            if (enchantments.size() <= 1) {
                return false;
            }
            Set<Enchantment> enchSet = enchantments.keySet();

            // 定义互斥附魔组（Paper 1.21+ Registry 方式）
            // 保护类互斥
            if (containsConflictGroup(enchSet,
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("protection")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("fire_protection")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("projectile_protection")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("blast_protection")))) {
                return true;
            }
            // 锋利/亡灵杀手/节肢杀手互斥
            if (containsConflictGroup(enchSet,
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sharpness")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("smite")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("bane_of_arthropods")))) {
                return true;
            }
            // 精准采集与时运互斥
            if (containsConflictGroup(enchSet,
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("silk_touch")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("fortune")))) {
                return true;
            }
            // 无限与经验修补互斥
            if (containsConflictGroup(enchSet,
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("infinity")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("mending")))) {
                return true;
            }
            // 冰霜行者与深海探索者互斥
            if (containsConflictGroup(enchSet,
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("frost_walker")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("depth_strider")))) {
                return true;
            }
            // 多重射击与穿透互斥
            if (containsConflictGroup(enchSet,
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("multishot")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("piercing")))) {
                return true;
            }
            // 激流与忠诚/引雷互斥
            if (containsConflictGroup(enchSet,
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("riptide")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("loyalty")))) {
                return true;
            }
            if (containsConflictGroup(enchSet,
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("riptide")),
                    Registry.ENCHANTMENT.get(NamespacedKey.minecraft("channeling")))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测附魔等级是否超过了 32767（真正的"32k"等级）
     */
    public boolean checkExtremeEnchantment(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            Map<Enchantment, Integer> enchantments = item.getEnchantments();
            for (Integer level : enchantments.values()) {
                if (level > 32767) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测物品是否使用 HideFlags 隐藏了属性信息
     */
    public boolean checkHideFlags(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // ItemMeta 的 getItemFlags() 返回隐藏的标志位
                return !meta.getItemFlags().isEmpty();
            }
        }
        return false;
    }

    /**
     * 检测物品的自定义名称是否异常（过长或包含过多颜色代码）
     */
    public boolean checkAbnormalItemName(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                int lengthThreshold = intensityThreshold(new int[]{256, 200, 128, 96, 80, 72, 64, 56, 48, 40});
                int colorThreshold = intensityThreshold(new int[]{16, 12, 10, 8, 6, 5, 4, 3, 2, 1});
                // 名称长度超过阈值
                if (displayName.length() > lengthThreshold) {
                    return true;
                }
                // 检测是否包含过多颜色代码（§ 符号数量超过阈值）
                int colorCodeCount = displayName.length() - displayName.replace("§", "").length();
                if (colorCodeCount > colorThreshold) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测食品物品是否含有异常数量的药水效果或过高等级的效果
     */
    public boolean checkAbnormalFoodEffects(ItemStack item) {
        if (item != null && item.getType() != Material.AIR && item.getType().isEdible()) {
            NBTItem nbtItem = new NBTItem(item);
            NBTCompoundList effects = nbtItem.getCompoundList("CustomPotionEffects");
            if (effects != null && effects.size() > 0) {
                int effectCount = intensityThreshold(new int[]{20, 15, 10, 6, 4, 3, 2, 2, 1, 1});
                // 效果数量超过阈值
                if (effects.size() > effectCount) {
                    return true;
                }
                // 药水效果等级超过5（Amplifier > 4）即视为异常
                for (ReadWriteNBT effect : effects) {
                    if (effect.getInteger("Amplifier") > 4) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ========== 新增检测模块 (11-15) ==========

    /**
     * 检测无效药水类型 - Potion 标签指向不存在的药水类型
     */
    public boolean checkInvalidPotionType(ItemStack item) {
        if (item != null && item.getType() != Material.AIR
                && (item.getType() == Material.POTION
                || item.getType() == Material.SPLASH_POTION
                || item.getType() == Material.LINGERING_POTION)) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("Potion")) {
                String potionId = nbtItem.getString("Potion");
                // 检查是否是已知的药水类型 ID
                NamespacedKey key = NamespacedKey.minecraft(potionId);
                PotionType potionType = Registry.POTION.get(key);
                if (potionType == null) {
                    return true; // 无效的药水类型
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 检测无效物品模型 - item_model 指向不存在的物品
     */
    public boolean checkInvalidItemModel(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("ItemModel")) {
                String modelId = nbtItem.getString("ItemModel");
                // 检查模型 ID 是否指向有效物品
                try {
                    NamespacedKey key = NamespacedKey.minecraft(modelId);
                    Material material = Registry.MATERIAL.get(key);
                    if (material == null || material == Material.AIR) {
                        return true; // 无效的模型
                    }
                } catch (Exception e) {
                    return true; // 解析失败视为异常
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 检测自定义地图 ID - 检测异常地图 ID（过大或负数）
     */
    public boolean checkCustomMapID(ItemStack item) {
        if (item != null && item.getType() != Material.AIR && Material.MAP.isItem()) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("map")) {
                int mapId = nbtItem.getInteger("map");
                int threshold = intensityThreshold(new int[]{100000000, 50000000, 10000000, 5000000, 1000000, 500000, 100000, 50000, 10000, 1000});
                if (mapId < 0 || mapId > threshold) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 检测极端药水效果 - CustomPotionEffects 中的效果持续时间或等级异常
     */
    public boolean checkExtremePotionEffects(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            NBTCompoundList effects = nbtItem.getCompoundList("CustomPotionEffects");
            if (effects != null && effects.size() > 0) {
                int durThreshold = intensityThreshold(new int[]{5000000, 3000000, 2000000, 1200000, 600000, 300000, 200000, 100000, 50000, 20000});
                // 药水效果等级超过5（Amplifier > 4）即视为异常
                for (ReadWriteNBT effect : effects) {
                    int duration = effect.getInteger("Duration");
                    int amplifier = effect.getInteger("Amplifier");
                    if (duration > durThreshold || amplifier > 4) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 检测异常 CustomModelData - CustomModelData 值过大（>9999）
     */
    public boolean checkCustomModelData(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasCustomModelData()) {
                int modelData = meta.getCustomModelData();
                int threshold = intensityThreshold(new int[]{1000000, 500000, 100000, 50000, 9999, 5000, 1000, 500, 100, 50});
                if (modelData > threshold || modelData < -threshold) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 检测创造模式专属物品 - 生存模式不应该出现的物品（基岩、命令方块、屏障等）
     */
    public boolean checkCreativeOnlyItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        Material type = item.getType();
        switch (type) {
            // 方块类创造专属
            case BEDROCK:
            case BARRIER:
            case COMMAND_BLOCK:
            case CHAIN_COMMAND_BLOCK:
            case REPEATING_COMMAND_BLOCK:
            case STRUCTURE_BLOCK:
            case STRUCTURE_VOID:
            case JIGSAW:
            case LIGHT:
            case REINFORCED_DEEPSLATE:
            // 传送门框架类
            case END_PORTAL_FRAME:
            case END_GATEWAY:
            case END_PORTAL:
            case NETHER_PORTAL:
            // 物品类创造专属
            case DEBUG_STICK:
            case KNOWLEDGE_BOOK:
            case COMMAND_BLOCK_MINECART:
            // 其他不可生存获取
            case FARMLAND:
            case SPAWNER:
                return true;
            default:
                return false;
        }
    }

    /**
     * 检测物品是否为鞘翅（ELYTRA）
     * 鞘翅只能在创造模式下获取，生存模式不允许持有
     */
    public String checkElytra(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (item.getType() == Material.ELYTRA) {
            return "生存模式不允许持有鞘翅(ELYTRA)";
        }
        return null;
    }

    // ========== 刷怪蛋检测工具方法 ==========

    /**
     * 判断物品是否为刷怪蛋（Spawn Egg）
     * 兼容 1.12 的 MONSTER_EGG 和 1.13+ 的 _SPAWN_EGG 名称格式
     */
    public boolean isSpawnEgg(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        String materialName = item.getType().name();
        // 1.12: MONSTER_EGG; 1.13+: *_SPAWN_EGG
        return materialName.equals("MONSTER_EGG") || materialName.endsWith("_SPAWN_EGG");
    }

    // ========== 辅助方法 ==========

    /**
     * 判断两个或以上的互斥附魔是否同时出现在物品上
     */
    private boolean containsConflictGroup(Set<Enchantment> enchants, Enchantment... group) {
        int count = 0;
        for (Enchantment ench : group) {
            if (enchants.contains(ench)) {
                count++;
            }
        }
        return count >= 2;
    }

    // ========== 综合检测入口 ==========

    public String checkItem(ItemStack itemStack, boolean... detectionFlags) {
        if (itemStack == null) return null;

        String reason;
        if (detectionFlags.length > 0) {
            int index = 0;
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkAbnormalNBT(itemStack)) return "异常NBT属性";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkAbnormalEnchantment(itemStack)) return "异常附魔等级";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkAbnormalAmount(itemStack)) return "异常堆叠数量";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkUnbreakable(itemStack)) return "不可破坏标签";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkIllegalEnchantments(itemStack)) return "互斥附魔共存";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkExtremeEnchantment(itemStack)) return "极端附魔等级";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkHideFlags(itemStack)) return "隐藏物品属性";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkAbnormalItemName(itemStack)) return "异常名称描述";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkAbnormalFoodEffects(itemStack)) return "异常食物效果";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkInvalidPotionType(itemStack)) return "无效药水类型";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkInvalidItemModel(itemStack)) return "无效物品模型";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkCustomMapID(itemStack)) return "异常地图ID";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkExtremePotionEffects(itemStack)) return "极端药水效果";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkCustomModelData(itemStack)) return "异常模型数据";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if (checkCreativeOnlyItem(itemStack)) return "创造专属物品";
            }
            if (index < detectionFlags.length && detectionFlags[index++]) {
                if ((reason = checkElytra(itemStack)) != null) return reason;
            }
            return null;
        }

        // 默认行为：全部检查
        if (checkAbnormalNBT(itemStack)) return "异常NBT属性";
        if (checkAbnormalEnchantment(itemStack)) return "异常附魔等级";
        if (checkAbnormalAmount(itemStack)) return "异常堆叠数量";
        if (checkUnbreakable(itemStack)) return "不可破坏标签";
        if (checkIllegalEnchantments(itemStack)) return "互斥附魔共存";
        if (checkExtremeEnchantment(itemStack)) return "极端附魔等级";
        if (checkHideFlags(itemStack)) return "隐藏物品属性";
        if (checkAbnormalItemName(itemStack)) return "异常名称描述";
        if (checkAbnormalFoodEffects(itemStack)) return "异常食物效果";
        if (checkInvalidPotionType(itemStack)) return "无效药水类型";
        if (checkInvalidItemModel(itemStack)) return "无效物品模型";
        if (checkCustomMapID(itemStack)) return "异常地图ID";
        if (checkExtremePotionEffects(itemStack)) return "极端药水效果";
        if (checkCustomModelData(itemStack)) return "异常模型数据";
        if (checkCreativeOnlyItem(itemStack)) return "创造专属物品";
        if ((reason = checkElytra(itemStack)) != null) return reason;
        return null;
    }
}
