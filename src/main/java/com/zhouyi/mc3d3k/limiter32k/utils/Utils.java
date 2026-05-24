package com.zhouyi.mc3d3k.limiter32k.utils;

import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Set;

public class Utils {

    // ========== 单模块检测方法，每种返回 boolean ==========

    public boolean checkAbnormalNBT(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            NBTCompoundList attrs = nbtItem.getCompoundList("AttributeModifiers");
            if (attrs.size() > 0) {
                for (ReadWriteNBT attr : attrs) {
                    if (attr.getInteger("Amount") != 0) {
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
            for (Integer level : enchantments.values()) {
                if (level > 5) {
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
            return currentAmount > maxStack;
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

            // 定义互斥附魔组（Paper 1.20.5+ / 1.21 API 常量名）
            // 保护类互斥
            if (containsConflictGroup(enchSet,
                    Enchantment.PROTECTION,
                    Enchantment.FIRE_PROTECTION,
                    Enchantment.PROJECTILE_PROTECTION,
                    Enchantment.BLAST_PROTECTION)) {
                return true;
            }
            // 锋利/亡灵杀手/节肢杀手互斥
            if (containsConflictGroup(enchSet,
                    Enchantment.SHARPNESS,
                    Enchantment.SMITE,
                    Enchantment.BANE_OF_ARTHROPODS)) {
                return true;
            }
            // 精准采集与时运互斥
            if (containsConflictGroup(enchSet,
                    Enchantment.SILK_TOUCH,
                    Enchantment.FORTUNE)) {
                return true;
            }
            // 无限与经验修补互斥
            if (containsConflictGroup(enchSet,
                    Enchantment.INFINITY,
                    Enchantment.MENDING)) {
                return true;
            }
            // 冰霜行者与深海探索者互斥
            if (containsConflictGroup(enchSet,
                    Enchantment.FROST_WALKER,
                    Enchantment.DEPTH_STRIDER)) {
                return true;
            }
            // 多重射击与穿透互斥
            if (containsConflictGroup(enchSet,
                    Enchantment.MULTISHOT,
                    Enchantment.PIERCING)) {
                return true;
            }
            // 激流与忠诚/引雷互斥
            if (containsConflictGroup(enchSet,
                    Enchantment.RIPTIDE,
                    Enchantment.LOYALTY)) {
                return true;
            }
            if (containsConflictGroup(enchSet,
                    Enchantment.RIPTIDE,
                    Enchantment.CHANNELING)) {
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
                // 名称长度超过 64 个字符
                if (displayName.length() > 64) {
                    return true;
                }
                // 检测是否包含大量颜色代码（4个以上 § 符号）
                int colorCodeCount = displayName.length() - displayName.replace("§", "").length();
                if (colorCodeCount > 4) {
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
            if (effects.size() > 0) {
                // 效果数量超过 3 个为异常
                if (effects.size() > 3) {
                    return true;
                }
                // 检查是否有效果等级过高（Amplifier > 5）
                for (ReadWriteNBT effect : effects) {
                    if (effect.getInteger("Amplifier") > 5) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    public boolean checkItem(ItemStack itemStack, boolean... detectionFlags) {
        if (itemStack == null) return false;

        // 如果传入了检测标志，依次检查
        if (detectionFlags.length > 0) {
            int index = 0;
            if (index < detectionFlags.length && detectionFlags[index++] && checkAbnormalNBT(itemStack)) return true;
            if (index < detectionFlags.length && detectionFlags[index++] && checkAbnormalEnchantment(itemStack)) return true;
            if (index < detectionFlags.length && detectionFlags[index++] && checkAbnormalAmount(itemStack)) return true;
            if (index < detectionFlags.length && detectionFlags[index++] && checkUnbreakable(itemStack)) return true;
            if (index < detectionFlags.length && detectionFlags[index++] && checkIllegalEnchantments(itemStack)) return true;
            if (index < detectionFlags.length && detectionFlags[index++] && checkExtremeEnchantment(itemStack)) return true;
            if (index < detectionFlags.length && detectionFlags[index++] && checkHideFlags(itemStack)) return true;
            if (index < detectionFlags.length && detectionFlags[index++] && checkAbnormalItemName(itemStack)) return true;
            if (index < detectionFlags.length && detectionFlags[index++] && checkAbnormalFoodEffects(itemStack)) return true;
            return false;
        }

        // 默认行为：全部检查
        return checkAbnormalNBT(itemStack)
                || checkAbnormalEnchantment(itemStack)
                || checkAbnormalAmount(itemStack)
                || checkUnbreakable(itemStack)
                || checkIllegalEnchantments(itemStack)
                || checkExtremeEnchantment(itemStack)
                || checkHideFlags(itemStack)
                || checkAbnormalItemName(itemStack)
                || checkAbnormalFoodEffects(itemStack);
    }
}
