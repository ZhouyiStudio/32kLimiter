package com.zhouyi.mc3d3k.limiter32k.commands;

import com.zhouyi.mc3d3k.limiter32k.LimiterMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class LimiterCommand implements TabExecutor {

    private static final List<String> COMMANDS = Arrays.asList("reload", "config", "enable", "disable", "status");

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

            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + bold("\u53ef\u7528\u547d\u4ee4:")); // 可用命令
            sender.sendMessage(ChatColor.GOLD + "/" + label + " reload " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u91cd\u8f7d\u914d\u7f6e\u6587\u4ef6");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " config " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u67e5\u770b\u5f53\u524d\u914d\u7f6e");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " enable " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u542f\u7528\u63d2\u4ef6");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " disable " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u7981\u7528\u63d2\u4ef6");
            sender.sendMessage(ChatColor.GOLD + "/" + label + " status " + ChatColor.WHITE + "- " + ChatColor.GRAY + "\u67e5\u770b\u63d2\u4ef6\u72b6\u6001");
            return true;
        }

        switch (args[0]) {
            case "reload":
                sender.sendMessage(ChatColor.GREEN + "\u91cd\u8f7d\u4e2d..."); // 重载中...
                LimiterMain.getInstance().reload();
                sender.sendMessage(ChatColor.GREEN + "\u91cd\u8f7d\u5b8c\u6210\u3002\u5f53\u524d\u72b6\u6001: " // 重载完成。当前状态:
                        + (LimiterMain.isEnabled ? ChatColor.GREEN + "\u5df2\u542f\u7528" : ChatColor.RED + "\u5df2\u7981\u7528"));
                break;
            case "config":
                sender.sendMessage(ChatColor.GOLD + "===== 32kLimiter \u914d\u7f6e ====="); // ===== 32kLimiter 配置 =====
                sender.sendMessage(ChatColor.GOLD + "enabled: " + green(LimiterMain.isEnabled));
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
                break;
            case "enable":
                LimiterMain.isEnabled = true;
                sender.sendMessage(ChatColor.GREEN + "\u63d2\u4ef6\u5df2\u542f\u7528\u3002"); // 插件已启用。
                break;
            case "disable":
                LimiterMain.isEnabled = false;
                sender.sendMessage(ChatColor.GREEN + "\u63d2\u4ef6\u5df2\u7981\u7528\u3002"); // 插件已禁用。
                break;
            case "status":
                String status = LimiterMain.isEnabled
                        ? ChatColor.GREEN + "\u5df2\u542f\u7528"    // 已启用
                        : ChatColor.RED + "\u5df2\u7981\u7528";      // 已禁用
                sender.sendMessage(ChatColor.GOLD + "\u63d2\u4ef6\u5f53\u524d\u72b6\u6001: " + status); // 插件当前状态:
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
        return null;
    }
}
