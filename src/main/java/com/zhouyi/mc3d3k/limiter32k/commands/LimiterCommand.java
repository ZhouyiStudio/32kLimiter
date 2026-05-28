package com.zhouyi.mc3d3k.limiter32k.commands;

import com.zhouyi.mc3d3k.limiter32k.LimiterMain;
import com.zhouyi.mc3d3k.limiter32k.utils.BanManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class LimiterCommand implements TabExecutor {

    private static final List<String> COMMANDS = Arrays.asList(
            "reload", "config", "enable", "disable", "status",
            "banitem", "banlist", "unbanitem",
            "whitelist",
            "exeadd", "exedel"
    );

    private static final List<String> WHITELIST_SUBCOMMANDS = Arrays.asList("add", "remove", "list");

    private String bold(String text) {
        return ChatColor.BOLD + text;
    }

    private String green(boolean value) {
        return value ? ChatColor.GREEN + "true" : ChatColor.RED + "false";
    }

    private String statusMark(boolean enabled) {
        return enabled
                ? ChatColor.GREEN + "\u2714"   // ✔
                : ChatColor.RED + "\u2718";     // ✘
    }

    private void noPerm(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "此命令仅限OP使用。");
    }

    private void noAdvancedMode(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "高级执行模式未启用。请先由控制台执行 /32klimiter exeadd 启用。");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // ========== /limiter — 显示插件状态与帮助 ==========
            String version = LimiterMain.getInstance().getDescription().getVersion();
            sender.sendMessage(ChatColor.GOLD + "===== 32kLimiter " + bold("v" + version) + " =====");

            // 插件启用状态
            String status = LimiterMain.isEnabled
                    ? ChatColor.GREEN + bold("\u2714 \u5df2\u542f\u7528")    // ✔ 已启用
                    : ChatColor.RED + bold("\u2718 \u5df2\u7981\u7528");      // ✘ 已禁用
            sender.sendMessage(ChatColor.GOLD + "\u63d2\u4ef6\u72b6\u6001: " + status); // 插件状态

            sender.sendMessage("");

            // 检测模块状态列表
            sender.sendMessage(ChatColor.YELLOW + bold("\u68c0\u6d4b\u6a21\u5757:")); // 检测模块
            sender.sendMessage("  " + statusMark(LimiterMain.detectAbnormalNBT) + " \u5f02\u5e38NBT\u4fee\u6539         " + ChatColor.GRAY + "(abnormal-nbt)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectAbnormalEnchantment) + " \u5f02\u5e38\u9644\u9b54\u68c0\u6d4b       " + ChatColor.GRAY + "(abnormal-enchantment)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectAbnormalAmount) + " \u5f02\u5e38\u5806\u53e0\u6570\u91cf       " + ChatColor.GRAY + "(abnormal-amount)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectUnbreakable) + " \u65e0\u6cd5\u7834\u574f\u68c0\u6d4b       " + ChatColor.GRAY + "(unbreakable)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectIllegalEnchantments) + " \u975e\u6cd5\u9644\u9b54\u5171\u5b58     " + ChatColor.GRAY + "(illegal-enchantments)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectExtremeEnchantment) + " \u6781\u7aef\u9644\u9b54\u7b49\u7ea7     " + ChatColor.GRAY + "(extreme-enchantment)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectHideFlags) + " \u9690\u85cf\u6807\u5fd7\u68c0\u6d4b       " + ChatColor.GRAY + "(hide-flags)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectAbnormalNameLore) + " \u5f02\u5e38\u540d\u79f0Lore       " + ChatColor.GRAY + "(abnormal-name-lore)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectAbnormalFoodEffects) + " \u5f02\u5e38\u98df\u7269\u6548\u679c     " + ChatColor.GRAY + "(abnormal-food-effects)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectNonOpSpawnEgg) + " \u975eOP\u5237\u602a\u86cb\u79fb\u9664     " + ChatColor.GRAY + "(remove-spawn-egg-for-non-op)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectInvalidPotionType) + " \u65e0\u6548\u836f\u6c34\u7c7b\u578b     " + ChatColor.GRAY + "(invalid-potion-type)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectInvalidItemModel) + " \u65e0\u6548\u7269\u54c1\u6a21\u578b     " + ChatColor.GRAY + "(invalid-item-model)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectCustomMapID) + " \u5f02\u5e38\u5730\u56feID         " + ChatColor.GRAY + "(custom-map-id)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectExtremePotionEffects) + " \u6781\u7aef\u836f\u6c34\u6548\u679c     " + ChatColor.GRAY + "(extreme-potion-effects)");
            sender.sendMessage("  " + statusMark(LimiterMain.detectCustomModelData) + " \u5f02\u5e38\u6a21\u578b\u6570\u636e     " + ChatColor.GRAY + "(custom-model-data)");

            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + bold("\u53ef\u7528\u547d\u4ee4:")); // 可用命令
            sender.sendMessage(ChatColor.GOLD + "/" + label + " reload " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u91cd\u8f7d\u914d\u7f6e\u6587\u4ef6");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " config " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u67e5\u770b\u5f53\u524d\u914d\u7f6e");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " enable " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u542f\u7528\u63d2\u4ef6");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " disable " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u7981\u7528\u63d2\u4ef6");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " status " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u67e5\u770b\u63d2\u4ef6\u72b6\u6001");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " banitem " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u5c06\u624b\u4e2d\u7269\u54c1\u52a0\u5165\u9ed1\u540d\u5355");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " banlist " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u67e5\u770b\u9ed1\u540d\u5355\u7269\u54c1\u5217\u8868");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " whitelist add [\u73a9\u5bb6] " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u6dfb\u52a0\u73a9\u5bb6\u5230\u767d\u540d\u5355(\u4ec5\u63a7\u5236\u53f0)");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " whitelist remove [\u73a9\u5bb6] " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u79fb\u9664\u73a9\u5bb6\u6216\u624b\u4e2d\u7269\u54c1\u4ece\u767d\u540d\u5355");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " whitelist list " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u67e5\u770b\u767d\u540d\u5355\u73a9\u5bb6\u548c\u7269\u54c1\u5217\u8868");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " unbanitem " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u5c06\u624b\u4e2d\u7269\u54c1\u4ece\u9ed1\u540d\u5355\u79fb\u9664");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " exeadd " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u542f\u7528\u9ad8\u7ea7\u6267\u884c\u6a21\u5f0f(\u4ec5\u63a7\u5236\u53f0)");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " exedel " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u7981\u7528\u9ad8\u7ea7\u6267\u884c\u6a21\u5f0f(\u4ec5\u63a7\u5236\u53f0)");
            return true;
        }

        switch (args[0]) {
            case "reload":
                if (!sender.isOp()) { noPerm(sender); return true; }
                if (!LimiterMain.advancedExeMode) { noAdvancedMode(sender); return true; }
                sender.sendMessage(ChatColor.GREEN + "\u91cd\u8f7d\u4e2d..."); // 重载中...
                LimiterMain.getInstance().reload();
                sender.sendMessage(ChatColor.GREEN + "\u91cd\u8f7d\u5b8c\u6210\u3002\u5f53\u524d\u72b6\u6001: " // 重载完成。当前状态:
                        + (LimiterMain.isEnabled ? ChatColor.GREEN + "\u5df2\u542f\u7528" : ChatColor.RED + "\u5df2\u7981\u7528"));
                break;
            case "config":
                if (!sender.isOp()) { noPerm(sender); return true; }
                if (!LimiterMain.advancedExeMode) { noAdvancedMode(sender); return true; }
                sender.sendMessage(ChatColor.GOLD + "===== 32kLimiter \u914d\u7f6e ====="); // ===== 32kLimiter 配置 =====
                sender.sendMessage(ChatColor.GOLD + "enabled: " + green(LimiterMain.isEnabled));
                sender.sendMessage(ChatColor.GOLD + "detection-intensity: " + LimiterMain.detectionIntensity + "/10");
                sender.sendMessage(ChatColor.GOLD + "detections:");
                sender.sendMessage("  abnormal-nbt: " + green(LimiterMain.detectAbnormalNBT));
                sender.sendMessage("  abnormal-enchantment: " + green(LimiterMain.detectAbnormalEnchantment));
                sender.sendMessage("  abnormal-amount: " + green(LimiterMain.detectAbnormalAmount));
                sender.sendMessage("  unbreakable: " + green(LimiterMain.detectUnbreakable));
                sender.sendMessage("  illegal-enchantments: " + green(LimiterMain.detectIllegalEnchantments));
                sender.sendMessage("  extreme-enchantment: " + green(LimiterMain.detectExtremeEnchantment));
                sender.sendMessage("  hide-flags: " + green(LimiterMain.detectHideFlags));
                sender.sendMessage("  abnormal-name-lore: " + green(LimiterMain.detectAbnormalNameLore));
                sender.sendMessage("  abnormal-food-effects: " + green(LimiterMain.detectAbnormalFoodEffects));
                sender.sendMessage("  remove-spawn-egg-for-non-op: " + green(LimiterMain.detectNonOpSpawnEgg));
                sender.sendMessage("  invalid-potion-type: " + green(LimiterMain.detectInvalidPotionType));
                sender.sendMessage("  invalid-item-model: " + green(LimiterMain.detectInvalidItemModel));
                sender.sendMessage("  custom-map-id: " + green(LimiterMain.detectCustomMapID));
                sender.sendMessage("  extreme-potion-effects: " + green(LimiterMain.detectExtremePotionEffects));
                sender.sendMessage("  custom-model-data: " + green(LimiterMain.detectCustomModelData));
                break;
            case "enable":
                if (!sender.isOp()) { noPerm(sender); return true; }
                if (!LimiterMain.advancedExeMode) { noAdvancedMode(sender); return true; }
                LimiterMain.isEnabled = true;
                sender.sendMessage(ChatColor.GREEN + "\u63d2\u4ef6\u5df2\u542f\u7528\u3002"); // 插件已启用。
                break;
            case "disable":
                if (!sender.isOp()) { noPerm(sender); return true; }
                if (!LimiterMain.advancedExeMode) { noAdvancedMode(sender); return true; }
                LimiterMain.isEnabled = false;
                sender.sendMessage(ChatColor.GREEN + "\u63d2\u4ef6\u5df2\u7981\u7528\u3002"); // 插件已禁用。
                break;
            case "status":
                String status = LimiterMain.isEnabled
                        ? ChatColor.GREEN + "\u5df2\u542f\u7528"    // 已启用
                        : ChatColor.RED + "\u5df2\u7981\u7528";      // 已禁用
                sender.sendMessage(ChatColor.GOLD + "\u63d2\u4ef6\u5f53\u524d\u72b6\u6001: " + status); // 插件当前状态:
                sender.sendMessage(ChatColor.GOLD + "高级执行模式: " + (LimiterMain.advancedExeMode ? ChatColor.GREEN + "\u2714 \u5df2\u542f\u7528" : ChatColor.RED + "\u2718 \u672a\u542f\u7528"));
                break;
            case "exeadd":
                if (!(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(ChatColor.RED + "该命令仅限控制台使用。");
                    return true;
                }
                LimiterMain.advancedExeMode = true;
sender.sendMessage(ChatColor.GREEN + "高级执行模式已启用。现在可执行高级命令 (reload/config/enable/disable/banitem/unbanitem/banlist/whitelist)。");
                break;
            case "exedel":
                if (!(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(ChatColor.RED + "该命令仅限控制台使用。");
                    return true;
                }
                LimiterMain.advancedExeMode = false;
                sender.sendMessage(ChatColor.GREEN + "高级执行模式已禁用。");
                break;
            case "banitem":
                if (!sender.isOp()) { noPerm(sender); return true; }
                if (!LimiterMain.advancedExeMode) { noAdvancedMode(sender); return true; }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "\u8be5\u547d\u4ee4\u53ea\u80fd\u7531\u73a9\u5bb6\u4f7f\u7528\u3002");
                    return true;
                }
                {
                    Player p = (Player) sender;
                    ItemStack hand = p.getInventory().getItemInMainHand();
                    if (hand == null || hand.getType() == Material.AIR) {
                        sender.sendMessage(ChatColor.RED + "\u4f60\u624b\u4e2d\u6ca1\u6709\u7269\u54c1\u3002");
                        return true;
                    }
                    // 加入黑名单，不清理执行者的物品
                    LimiterMain.getBanManager().addItemBlacklist(hand);
                    sender.sendMessage(ChatColor.GREEN + "\u5df2\u5c06 " + ChatColor.WHITE + hand.getType().name()
                            + ChatColor.GREEN + " \u52a0\u5165\u9ed1\u540d\u5355\u3002\u4f60\u624b\u4e2d\u7684\u7269\u54c1\u672a\u88ab\u6e05\u9664\u3002");
                }
                break;
            case "unbanitem":
                if (!sender.isOp()) { noPerm(sender); return true; }
                if (!LimiterMain.advancedExeMode) { noAdvancedMode(sender); return true; }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "\u8be5\u547d\u4ee4\u53ea\u80fd\u7531\u73a9\u5bb6\u4f7f\u7528\u3002");
                    return true;
                }
                {
                    Player p = (Player) sender;
                    ItemStack hand = p.getInventory().getItemInMainHand();
                    if (hand == null || hand.getType() == Material.AIR) {
                        sender.sendMessage(ChatColor.RED + "\u4f60\u624b\u4e2d\u6ca1\u6709\u7269\u54c1\u3002");
                        return true;
                    }
                    if (LimiterMain.getBanManager().removeItemBlacklist(hand)) {
                        sender.sendMessage(ChatColor.GREEN + "\u5df2\u5c06 " + ChatColor.WHITE + hand.getType().name()
                                + ChatColor.GREEN + " \u4ece\u9ed1\u540d\u5355\u79fb\u9664\u3002");
                    } else {
                        sender.sendMessage(ChatColor.RED + "该物品不在黑名单中。");
                    }
                }
                break;
            case "banlist":
                if (!sender.isOp()) { noPerm(sender); return true; }
                if (!LimiterMain.advancedExeMode) { noAdvancedMode(sender); return true; }
                {
                    Set<String> blacklist = LimiterMain.getBanManager().getItemBlacklist();
                    if (blacklist.isEmpty()) {
                        sender.sendMessage(ChatColor.GRAY + "\u9ed1\u540d\u5355\u4e2d\u6ca1\u6709\u7269\u54c1\u3002");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "===== \u9ed1\u540d\u5355\u7269\u54c1 (" + blacklist.size() + ") =====");
                        int i = 0;
                        for (String sig : blacklist) {
                            if (i >= 20) {
                                sender.sendMessage(ChatColor.GRAY + "  ... \u8fd8\u6709 " + (blacklist.size() - 20) + " \u4e2a\u7269\u54c1\u672a\u663e\u793a");
                                break;
                            }
                            // 只显示类型部分（竖线前）
                            String display = sig.contains("|") ? sig.split("\\|", 2)[0] : sig;
                            sender.sendMessage(ChatColor.GRAY + "  - " + ChatColor.WHITE + display);
                            i++;
                        }
                    }
                }
                break;
            case "whitelist":
                if (!sender.isOp()) { noPerm(sender); return true; }
                if (!LimiterMain.advancedExeMode) { noAdvancedMode(sender); return true; }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "\u7528\u6cd5: /" + label + " whitelist add|remove|list [\u73a9\u5bb6]");
                    return true;
                }
                String sub = args[1].toLowerCase();
                switch (sub) {
                    case "add":
                        if (args.length >= 3) {
                            // whitelist add <player>
                            if (!(sender instanceof ConsoleCommandSender)) {
                                sender.sendMessage(ChatColor.RED + "该命令仅限控制台使用。");
                                return true;
                            }
                            String name = args[2];
                            LimiterMain.getBanManager().addPlayerWhitelist(name);
                            sender.sendMessage(ChatColor.GREEN + "\u5df2\u5c06\u73a9\u5bb6 " + ChatColor.WHITE + name
                                    + ChatColor.GREEN + " \u52a0\u5165\u767d\u540d\u5355\u3002");
                        } else {
                            // whitelist add — 手中物品加入物品白名单
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "\u8be5\u547d\u4ee4\u53ea\u80fd\u7531\u73a9\u5bb6\u4f7f\u7528\u3002");
                                return true;
                            }
                            Player p = (Player) sender;
                            ItemStack hand = p.getInventory().getItemInMainHand();
                            if (hand == null || hand.getType() == Material.AIR) {
                                sender.sendMessage(ChatColor.RED + "\u4f60\u624b\u4e2d\u6ca1\u6709\u7269\u54c1\u3002");
                                return true;
                            }
                            // 如果是潜影盒，展开内部所有物品加入白名单
                            if (hand.getType().name().endsWith("SHULKER_BOX") && hand.getItemMeta() instanceof BlockStateMeta) {
                                BlockStateMeta bsm = (BlockStateMeta) hand.getItemMeta();
                                if (bsm.getBlockState() instanceof ShulkerBox) {
                                    ShulkerBox shulker = (ShulkerBox) bsm.getBlockState();
                                    ItemStack[] contents = shulker.getInventory().getContents();
                                    int count = 0;
                                    for (ItemStack content : contents) {
                                        if (content != null && content.getType() != Material.AIR) {
                                            LimiterMain.getBanManager().addItemWhitelist(content);
                                            count++;
                                        }
                                    }
                                    sender.sendMessage(ChatColor.GREEN + "\u5df2\u5c06\u6f5c\u5f71\u76d2\u4e2d\u7684 " + count + " \u4e2a\u7269\u54c1\u52a0\u5165\u7269\u54c1\u767d\u540d\u5355\u3002");
                                } else {
                                    LimiterMain.getBanManager().addItemWhitelist(hand);
                                    sender.sendMessage(ChatColor.GREEN + "\u5df2\u5c06 " + ChatColor.WHITE + hand.getType().name()
                                            + ChatColor.GREEN + " \u52a0\u5165\u7269\u54c1\u767d\u540d\u5355\u3002");
                                }
                            } else {
                                LimiterMain.getBanManager().addItemWhitelist(hand);
                                sender.sendMessage(ChatColor.GREEN + "\u5df2\u5c06 " + ChatColor.WHITE + hand.getType().name()
                                        + ChatColor.GREEN + " \u52a0\u5165\u7269\u54c1\u767d\u540d\u5355\u3002");
                            }
                        }
                        break;
                    case "remove":
                        if (args.length >= 3) {
                            // whitelist remove <player>
                            String name = args[2];
                            LimiterMain.getBanManager().removePlayerWhitelist(name);
                            sender.sendMessage(ChatColor.GREEN + "\u5df2\u5c06\u73a9\u5bb6 " + ChatColor.WHITE + name
                                    + ChatColor.GREEN + " \u4ece\u767d\u540d\u5355\u79fb\u9664\u3002");
                        } else {
                            // whitelist remove — 手中物品从物品白名单移除
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "\u8be5\u547d\u4ee4\u53ea\u80fd\u7531\u73a9\u5bb6\u4f7f\u7528\u3002");
                                return true;
                            }
                            Player p = (Player) sender;
                            ItemStack hand = p.getInventory().getItemInMainHand();
                            if (hand == null || hand.getType() == Material.AIR) {
                                sender.sendMessage(ChatColor.RED + "\u4f60\u624b\u4e2d\u6ca1\u6709\u7269\u54c1\u3002");
                                return true;
                            }
                            LimiterMain.getBanManager().removeItemWhitelist(hand);
                            sender.sendMessage(ChatColor.GREEN + "\u5df2\u5c06 " + ChatColor.WHITE + hand.getType().name()
                                    + ChatColor.GREEN + " \u4ece\u7269\u54c1\u767d\u540d\u5355\u79fb\u9664\u3002");
                        }
                        break;
                    case "list":
                        Set<String> wPlayers = LimiterMain.getBanManager().getWhitelistedPlayers();
                        Set<String> wItems = LimiterMain.getBanManager().getItemWhitelist();
                        sender.sendMessage(ChatColor.GOLD + "===== \u767d\u540d\u5355 =====");
                        sender.sendMessage(ChatColor.YELLOW + "\u73a9\u5bb6 (" + wPlayers.size() + "):");
                        if (wPlayers.isEmpty()) {
                            sender.sendMessage(ChatColor.GRAY + "  (\u65e0)");
                        } else {
                            for (String wp : wPlayers) {
                                sender.sendMessage(ChatColor.GRAY + "  - " + ChatColor.WHITE + wp);
                            }
                        }
                        sender.sendMessage(ChatColor.YELLOW + "\u7269\u54c1 (" + wItems.size() + "):");
                        if (wItems.isEmpty()) {
                            sender.sendMessage(ChatColor.GRAY + "  (\u65e0)");
                        } else {
                            for (String wi : wItems) {
                                String display = wi.contains("|") ? wi.split("\\|", 2)[0] : wi;
                                sender.sendMessage(ChatColor.GRAY + "  - " + ChatColor.WHITE + display);
                            }
                        }
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "\u672a\u77e5\u5b50\u547d\u4ee4\u3002\u7528\u6cd5: /" + label + " whitelist add|remove|list [\u73a9\u5bb6]");
                        return true;
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "\u672a\u77e5\u547d\u4ee4\u3002\u8f93\u5165 /" + label + " \u67e5\u770b\u5e2e\u52a9\u3002"); // 未知命令。输入 /... 查看帮助。
                return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> matchedCommands = new ArrayList<>();
            for (String cmd : COMMANDS) {
                String pattern = "^" + Pattern.quote(args[0]) + ".*";
                if (Pattern.matches(pattern, cmd)) {
                    matchedCommands.add(cmd);
                }
            }
            if (matchedCommands.isEmpty()) {
                matchedCommands = new ArrayList<>(COMMANDS);
            }
            return matchedCommands;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("whitelist")) {
            List<String> matchedSub = new ArrayList<>();
            for (String sub : WHITELIST_SUBCOMMANDS) {
                String pattern = "^" + Pattern.quote(args[1]) + ".*";
                if (Pattern.matches(pattern, sub)) {
                    matchedSub.add(sub);
                }
            }
            if (matchedSub.isEmpty()) {
                matchedSub = new ArrayList<>(WHITELIST_SUBCOMMANDS);
            }
            return matchedSub;
        }
        return null;
    }
}
