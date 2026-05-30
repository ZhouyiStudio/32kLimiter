package com.zhouyi.mc3d3k.limiter32k.events;

import com.zhouyi.mc3d3k.limiter32k.LimiterMain;
import com.zhouyi.mc3d3k.limiter32k.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
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
                LimiterMain.detectCustomModelData,
                LimiterMain.detectCreativeOnlyItem,
                LimiterMain.detectElytra
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
    private String shouldClean(Player player, ItemStack item) {
        if (player != null && LimiterMain.getBanManager().isPlayerWhitelisted(player.getName())) {
            return null;
        }
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        if (LimiterMain.getBanManager().isItemWhitelisted(item)) {
            return null;
        }
        // 跳过名字包含 "3d3k" 或 "3D3K" 的物品（不区分大小写）
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            // 去除颜色代码后检查
            String stripped = displayName.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
            if (stripped.toLowerCase().contains("3d3k")) {
                return null;
            }
        }
        if (LimiterMain.getBanManager().isItemBlacklisted(item)) {
            String reason = "黑名单物品";
            LimiterMain.getBanManager().logClean(player != null ? player.getName() : "unknown", item, reason);
            sendCleanNotification(player, item, reason);
            return reason;
        }
        String result;
        if (player == null) {
            result = utils.checkItem(item, getDetectionFlags());
        } else {
            result = utils.checkItem(item, getDetectionFlags());
            if (result == null && isNonOpWithSpawnEgg(player, item)) {
                result = "非OP持有刷怪蛋";
            }
        }
        if (result != null) {
            LimiterMain.getBanManager().logClean(player != null ? player.getName() : "unknown", item, result);
            sendCleanNotification(player, item, result);
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
                    String result = shouldClean(player, mainHand);
                    if (result != null) {
                        event.setDamage(40D);
                        player.getInventory().setItemInMainHand(AIR);
                    }
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    result = shouldClean(player, offHand);
                    if (result != null) {
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
            String mainHandResult = shouldClean(player, event.getMainHandItem());
            String offHandResult = shouldClean(player, event.getOffHandItem());
            if (mainHandResult != null) {
                event.setMainHandItem(AIR);
            }
            if (offHandResult != null) {
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
            if (shouldClean(player, item) != null) {
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
            String result = shouldClean(player, event.getCurrentItem());
            if (result != null) {
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
                    if (shouldClean(player, item) != null) {
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
            // 扫描结束的窗口中的异常物品
            scanItemsInInventory(event.getInventory(), event.getPlayer());
        }
    }

    /**
     * 扫描指定容器中的物品，清除异常物品
     */
    private void scanItemsInInventory(Inventory inv, HumanEntity human) {
        Player player = (human instanceof Player) ? (Player) human : null;
        ItemStack[] items = inv.getStorageContents();
        if (items.length == 0) return;
        int count = 0;
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null && !item.getType().isAir()) {
                String reason = shouldClean(player, item);
                if (reason != null) {
                    inv.setItem(i, AIR);
                    count++;
                    sendCleanNotification(player, item, reason);
                }
            }
        }
        if (count > 0 && player != null) {
            player.sendMessage("§c[32kLimiter] 已从 " + getInvName(inv) + " 中清理 " + count + " 个异常物品");
        }
    }

    private String getInvName(Inventory inv) {
        if (inv.getType() == InventoryType.ENDER_CHEST) return "末影箱";
        if (inv.getType() == InventoryType.CHEST) return "箱子";
        if (inv.getType() == InventoryType.PLAYER) return "背包";
        if (inv.getType() == InventoryType.SHULKER_BOX) return "潜影盒";
        return inv.getType().name();
    }

    /**
     * 玩家登录时扫描末影箱，清除异常物品（防止保存时序列化卡死）
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (LimiterMain.isEnabled && LimiterMain.detectEnderChestOnJoin) {
            Player player = event.getPlayer();
            Inventory enderChest = player.getEnderChest();
            if (enderChest == null) return;
            ItemStack[] items = enderChest.getStorageContents();
            if (items.length == 0) return;
            int count = 0;
            for (int i = 0; i < items.length; i++) {
                ItemStack item = items[i];
                if (item != null && !item.getType().isAir()) {
                    String reason = shouldClean(player, item);
                    if (reason != null) {
                        enderChest.setItem(i, AIR);
                        count++;
                        if (!player.hasMetadata("32kLimiter_cleared_enderchest")) {
                            sendCleanNotification(player, item, reason);
                        }
                    }
                }
            }
            if (count > 0) {
                player.sendMessage("§c[32kLimiter] 已从你的末影箱中清理 " + count + " 个异常物品");
            }
        }
    }

    /**
     * 关闭容器时扫描末影箱（防止关闭末影箱后漏扫）
     */
    private void scanEnderChestOnClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();
        if (inv.getType() != InventoryType.ENDER_CHEST) return;
        ItemStack[] items = inv.getStorageContents();
        if (items.length == 0) return;
        int count = 0;
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null && !item.getType().isAir()) {
                String reason = shouldClean(player, item);
                if (reason != null) {
                    inv.setItem(i, AIR);
                    count++;
                    sendCleanNotification(player, item, reason);
                }
            }
        }
        if (count > 0) {
            player.sendMessage("§c[32kLimiter] 已从你的末影箱中清理 " + count + " 个异常物品");
        }
    }

    /**
     * 阻止非创造模式玩家放置屏障/光源方块
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!LimiterMain.isEnabled) return;
        if (!LimiterMain.detectCreativeOnlyItem) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        Material type = event.getBlock().getType();
        if (type == Material.BARRIER || type == Material.LIGHT) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "⚠ 你不能在生存模式下放置 " + ChatColor.WHITE + type.name());
        }
    }

    /**
     * 扫描玩家附近已放置的屏障/光源方块并清除
     * 在玩家周围 radius 格范围内搜索
     */
    public static int scanPlayerSurroundings(Player player, int radius) {
        if (!LimiterMain.isEnabled || !LimiterMain.detectCreativeOnlyItem) return 0;

        World world = player.getWorld();
        int px = player.getLocation().getBlockX();
        int py = player.getLocation().getBlockY();
        int pz = player.getLocation().getBlockZ();
        int removed = 0;

        for (int x = px - radius; x <= px + radius; x++) {
            for (int z = pz - radius; z <= pz + radius; z++) {
                // 只扫描附近高度范围（y=0..world max）
                int maxY = world.getMaxHeight();
                for (int y = Math.max(0, py - radius); y <= Math.min(maxY, py + radius); y++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material type = block.getType();
                    if (type == Material.BARRIER || type == Material.LIGHT) {
                        block.setType(Material.AIR);
                        removed++;
                    }
                }
            }
        }
        return removed;
    }
}
