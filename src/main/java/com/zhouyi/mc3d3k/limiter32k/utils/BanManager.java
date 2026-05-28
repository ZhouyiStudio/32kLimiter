package com.zhouyi.mc3d3k.limiter32k.utils;

import com.zhouyi.mc3d3k.limiter32k.LimiterMain;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BanManager {

    private final LimiterMain plugin;
    private final File dataFile;
    private YamlConfiguration data;

    // 玩家白名单
    private final Set<String> whitelistedPlayers = new HashSet<>();
    // 物品黑名单
    private final Set<String> itemBlacklist = new HashSet<>();
    // 物品白名单
    private final Set<String> itemWhitelist = new HashSet<>();

    public BanManager(LimiterMain plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    /**
     * 生成物品唯一签名 — 类型名 + 完整 NBT JSON
     */
    public String getItemSignature(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "";
        NBTItem nbtItem = new NBTItem(item);
        return item.getType().name() + "|" + nbtItem.toString();
    }

    // ========== 玩家白名单 ==========

    public boolean isPlayerWhitelisted(String playerName) {
        return whitelistedPlayers.contains(playerName.toLowerCase());
    }

    public boolean addPlayerWhitelist(String playerName) {
        return whitelistedPlayers.add(playerName.toLowerCase());
    }

    public boolean removePlayerWhitelist(String playerName) {
        return whitelistedPlayers.remove(playerName.toLowerCase());
    }

    public Set<String> getWhitelistedPlayers() {
        return Collections.unmodifiableSet(whitelistedPlayers);
    }

    // ========== 物品黑名单 ==========

    public boolean isItemBlacklisted(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return itemBlacklist.contains(getItemSignature(item));
    }

    public boolean addItemBlacklist(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return itemBlacklist.add(getItemSignature(item));
    }

    /**
     * 从黑名单移除物品
     */
    public boolean removeItemBlacklist(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return itemBlacklist.remove(getItemSignature(item));
    }

    public Set<String> getItemBlacklist() {
        return Collections.unmodifiableSet(itemBlacklist);
    }

    // ========== 物品白名单 ==========

    public boolean isItemWhitelisted(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return itemWhitelist.contains(getItemSignature(item));
    }

    public boolean addItemWhitelist(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return itemWhitelist.add(getItemSignature(item));
    }

    public boolean removeItemWhitelist(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return itemWhitelist.remove(getItemSignature(item));
    }

    public Set<String> getItemWhitelist() {
        return Collections.unmodifiableSet(itemWhitelist);
    }

    // ========== 日志 ==========

    public void logClean(String playerName, ItemStack item, String reason) {
        plugin.getLogger().info("[清理] " + playerName
                + " -> " + item.getType().name()
                + " (" + reason + ")");
    }

    // ========== 持久化 ==========

    private void load() {
        if (!dataFile.exists()) {
            data = new YamlConfiguration();
            return;
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        whitelistedPlayers.clear();
        whitelistedPlayers.addAll(data.getStringList("player-whitelist"));

        itemBlacklist.clear();
        itemBlacklist.addAll(data.getStringList("item-blacklist"));

        itemWhitelist.clear();
        itemWhitelist.addAll(data.getStringList("item-whitelist"));
    }

    public void save() {
        if (data == null) data = new YamlConfiguration();
        data.set("player-whitelist", new ArrayList<>(whitelistedPlayers));
        data.set("item-blacklist", new ArrayList<>(itemBlacklist));
        data.set("item-whitelist", new ArrayList<>(itemWhitelist));
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("无法保存 data.yml: " + e.getMessage());
        }
    }
}
