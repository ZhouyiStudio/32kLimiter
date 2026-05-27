package com.zhouyi.mc3d3k.limiter32k.events;

import com.zhouyi.mc3d3k.limiter32k.LimiterMain;
import com.zhouyi.mc3d3k.limiter32k.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class EventListener implements Listener {
    private final Utils utils = new Utils();
    private final ItemStack AIR = new ItemStack(Material.AIR);

    /**
     * 向玩家发送物品清理提示弹窗
     */
    private void sendCleanNotification(Player player, ItemStack item, String reason) {
        if (player == null) return;
        player.sendTitle(
                ChatColor.RED + "§l物品被清理",
                ChatColor.YELLOW + item.getType().name() + " §7(" + reason + ")",
                10, 40, 10
        );
        player.sendMessage(ChatColor.RED + "⚠ " + ChatColor.WHITE + item.getType().name()
                + ChatColor.GRAY + " 已被服务器清理，原因: " + ChatColor.YELLOW + reason);
    }

    /**
     * 获取当前启用的检测模块标志数组
     * 顺序必须与 Utils.checkItem(ItemStack, boolean...) 中 varargs 的顺序一致
     */
    private boolean[] getDetectionFlags() {
        return new boolean[]{
                LimiterMain.detectAbnormalNBT,
                LimiterMain.detectAbnormalEnchantment,
                LimiterMain.detectAbnormalAmount,
                LimiterMain.detectUnbreakable,
                LimiterMain.detectIllegalEnchantments,
                LimiterMain.detectExtremeEnchantment,
                LimiterMain.detectHideFlags,
                LimiterMain.detectAbnormalNameLore,
                LimiterMain.detectAbnormalFoodEffects,
                LimiterMain.detectInvalidPotionType,
                LimiterMain.detectInvalidItemModel,
                LimiterMain.detectCustomMapID,
                LimiterMain.detectExtremePotionEffects,
                LimiterMain.detectCustomModelData
        };
    }

    /**
     * 判断非 OP 玩家是否持有生成蛋（移除非 OP 生成蛋检测）
     */
    private boolean isNonOpWithSpawnEgg(Player player, ItemStack item) {
        return !player.isOp() && LimiterMain.detectNonOpSpawnEgg && utils.isSpawnEgg(item);
    }

    /**
     * 判断是否需要清除物品
     * 先检查玩家白名单、物品空值/AIR、物品白名单，再检查物品黑名单（命中则直接记录并清理），
     * 最后委托给 utils.checkItem 和 isNonOpWithSpawnEgg 做进一步检测
     */
    private boolean shouldClean(Player player, ItemStack item) {
        if (player != null && LimiterMain.getBanManager().isPlayerWhitelisted(player.getName())) {
            return false;
        }
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        if (LimiterMain.getBanManager().isItemWhitelisted(item)) {
            return false;
        }
        // 跳过名字包含 "3d3k" 或 "3D3K" 的物品（不区分大小写）
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            // 去除颜色代码后检查
            String stripped = displayName.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
            if (stripped.toLowerCase().contains("3d3k")) {
                return false;
            }
        }
        String reason;
        if (LimiterMain.getBanManager().isItemBlacklisted(item)) {
            reason = "黑名单物品";
            LimiterMain.getBanManager().logClean(player != null ? player.getName() : "unknown", item, reason);
            sendCleanNotification(player, item, reason);
            return true;
        }
        boolean result;
        if (player == null) {
            result = utils.checkItem(item, getDetectionFlags());
        } else {
            result = utils.checkItem(item, getDetectionFlags()) || isNonOpWithSpawnEgg(player, item);
        }
        if (result) {
            reason = "异常物品";
            LimiterMain.getBanManager().logClean(player != null ? player.getName() : "unknown", item, reason);
            sendCleanNotification(player, item, reason);
        }
        return result;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (LimiterMain.isEnabled) {
            if (event.getDamage() > 30D) {
                if (event.getDamager() instanceof Player) {
                    Player player = (Player) event.getDamager();
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if (shouldClean(player, mainHand)) {
                        event.setDamage(40D);
                        player.getInventory().setItemInMainHand(AIR);
                    }
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    if (shouldClean(player, offHand)) {
                        event.setDamage(40D);
                        player.getInventory().setItemInOffHand(AIR);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (LimiterMain.isEnabled) {
            Player player = event.getPlayer();
            boolean mainHandResult = shouldClean(player, event.getMainHandItem());
            boolean offHandResult = shouldClean(player, event.getOffHandItem());
            if (mainHandResult) {
                event.setMainHandItem(AIR);
            }
            if (offHandResult) {
                event.setOffHandItem(AIR);
            }
        }
    }

    @EventHandler
    public void EntityPickupItem(EntityPickupItemEvent event) {
        if (LimiterMain.isEnabled) {
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player) event.getEntity();
            ItemStack item = event.getItem().getItemStack();
            if (shouldClean(player, item)) {
                event.setCancelled(true);
                event.getItem().remove();
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (LimiterMain.isEnabled) {
            Player player = null;
            if (event.getWhoClicked() instanceof Player) {
                player = (Player) event.getWhoClicked();
            }
            boolean abnormal = shouldClean(player, event.getCurrentItem());
            if (abnormal) {
                event.setCurrentItem(AIR);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (LimiterMain.isEnabled) {
            Player player = (event.getPlayer() instanceof Player) ? (Player) event.getPlayer() : null;
            ItemStack[] items = event.getInventory().getStorageContents();
            if (items.length > 0) {
                ArrayList<ItemStack> abnormalItems = new ArrayList<>();
                for (ItemStack item : items) {
                    if (shouldClean(player, item)) {
                        if (!abnormalItems.contains(item)) {
                            abnormalItems.add(item);
                        }
                    }
                }
                if (abnormalItems.size() > 0) {
                    for (ItemStack item : abnormalItems) {
                        event.getInventory().remove(item);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (LimiterMain.isEnabled) {
            Player player = (event.getPlayer() instanceof Player) ? (Player) event.getPlayer() : null;
            // 如果关闭的是玩家自己的背包（按E键），event.getInventory() 就是 player.getInventory()
            // 此时只需扫描一次，避免重复判定
            boolean isOwnInventory = event.getInventory().equals(event.getPlayer().getInventory());
            // Player
            ItemStack[] items = event.getPlayer().getInventory().getStorageContents();
            if (items.length > 0) {
                ArrayList<ItemStack> abnormalItems = new ArrayList<>();
                for (ItemStack item : items) {
                    if (shouldClean(player, item)) {
                        if (!abnormalItems.contains(item)) {
                            abnormalItems.add(item);
                        }
                    }
                }
                if (abnormalItems.size() > 0) {
                    for (ItemStack item : abnormalItems) {
                        event.getPlayer().getInventory().remove(item);
                    }
                }
            }
            // Inventory（如果不是玩家自己的背包，再扫描事件窗口）
            if (!isOwnInventory) {
                ItemStack[] inventoryContents = event.getInventory().getStorageContents();
                if (inventoryContents.length > 0) {
                    ArrayList<ItemStack> abnormalItems = new ArrayList<>();
                    for (ItemStack item : inventoryContents) {
                        if (shouldClean(player, item)) {
                            if (!abnormalItems.contains(item)) {
                                abnormalItems.add(item);
                            }
                        }
                    }
                    if (abnormalItems.size() > 0) {
                        for (ItemStack item : abnormalItems) {
                            event.getInventory().remove(item);
                        }
                    }
                }
            }
        }
    }
}
