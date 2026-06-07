32kLimiter
---
![Version](https://img.shields.io/github/v/release/ZhouyiStudio/32kLimiter)
![Download](https://img.shields.io/github/downloads/ZhouyiStudio/32kLimiter/total)
![License](https://img.shields.io/github/license/ZhouyiStudio/32kLimiter)
![Stars](https://img.shields.io/github/stars/ZhouyiStudio/32kLimiter)

[简体中文](https://github.com/ZhouyiStudio/32kLimiter/blob/master/README_cn.md) | **English**

A Spigot plugin for limiting 32k weapons

Supported versions of Minecraft: 1.21

> Note: In version 2.0.0 and later, this plugin will rely on the [**`NBT API`**](https://www.spigotmc.org/resources/nbt-api.7939/)
>
> You can download it [Here](https://www.spigotmc.org/resources/nbt-api.7939/)

---

## Features

- **16 Detection Modules** — Covering abnormal NBT, enchantments, item amounts, unbreakable tags, illegal enchantment combinations, extreme enchantment levels, hide flags, abnormal names/lore, food effects, spawn eggs, potion types, item models, map IDs, potion effects, custom model data, and more
- **Adjustable Detection Intensity** — Configurable sensitivity from 1 (loose) to 10 (strict)
- **Whitelist System** — Player whitelist, item whitelist, and item blacklist with persistent storage
- **Shulker Box Auto-Expand** — Adding a shulker box to whitelist automatically expands all contents inside
- **Cleanup Notification** — Players receive a screen title + chat message when their items are cleaned
- **3d3k Immunity** — Items with "3d3k" in their display name are automatically skipped
- **In-Memory Toggle** — Enable/disable the plugin at runtime without restart

## Commands

| Command                          | Permission       | Description                                          |
|----------------------------------|------------------|------------------------------------------------------|
| /32klimiter                      | 32klimiter.admin | Get help                                             |
| /32klimiter reload               | ~                | Reload configuration files                           |
| /32klimiter config               | ~                | View current configuration                           |
| /32klimiter enable               | ~                | Temporarily enable the plugin                        |
| /32klimiter disable              | ~                | Temporarily disable the plugin                       |
| /32klimiter status               | ~                | Get the current status of the plugin                 |
| /32klimiter banitem              | ~                | Add held item to blacklist (player only)             |
| /32klimiter banlist              | ~                | View blacklisted items                               |
| /32klimiter whitelist add [player] | ~              | Add player to whitelist (console only)               |
| /32klimiter whitelist add        | ~                | Add held item to item whitelist (player only, supports shulker box auto-expand) |
| /32klimiter whitelist remove [player] | ~           | Remove player from whitelist                         |
| /32klimiter whitelist remove     | ~                | Remove held item from item whitelist (player only)   |
| /32klimiter whitelist list       | ~                | List all whitelisted players and items               |

## Config

```yaml
# Plugin master switch
enabled: true

# Detection intensity (1-10). Higher = stricter detection.
# 1-3: Loose (only extreme violations), 4-7: Normal, 8-10: Strict (borderline items also caught)
# Default: 5
detection-intensity: 5

# Detection modules — set false to disable any check
detections:
  # Detects abnormal NBT attribute modifiers (e.g. excessive attack damage / health)
  abnormal-nbt: true

  # Detects abnormal enchantment levels (exceeding vanilla max, e.g. Sharpness 6+)
  abnormal-enchantment: true

  # Detects abnormal item stack amounts (e.g. more than 64 in a stack)
  abnormal-amount: true

  # Detects unbreakable tag (Unbreakable:1, common on cheated items)
  unbreakable: true

  # Detects illegal enchantment combinations (mutually exclusive enchants co-existing)
  illegal-enchantments: true

  # Detects extreme enchantment levels (over 32767, the true "32k" level)
  extreme-enchantment: true

  # Detects hidden attribute flags (HideFlags used to conceal item info)
  hide-flags: true

  # Detects abnormal item names (too long, excessive color codes)
  abnormal-name-lore: true

  # Detects abnormal potion effects on food items (> 3 effects or amplifier > 5)
  abnormal-food-effects: true

  # Removes spawn eggs from non-OP players
  remove-spawn-egg-for-non-op: true

  # Detects invalid potion types (Potion tag pointing to non-existent potion)
  invalid-potion-type: true

  # Detects invalid item models (ItemModel pointing to non-existent item)
  invalid-item-model: true

  # Detects custom map IDs (map ID abnormally large or negative)
  custom-map-id: true

  # Detects extreme potion effects (duration > 600000 ticks or amplifier > 20)
  extreme-potion-effects: true

  # Detects abnormal CustomModelData values (outside ±9999)
  custom-model-data: true
```

## About

This plugin does not completely block 32k weapons, players can use 32k in the **hopper** as normal (similar to 2b2t servers)

If you like this project, please give us a Star or [donate to us](https://3d3k.org/sponsor.html), it will make a great contribution to our development work!

If you are using this plugin on your own server, I'd love it if you could put a link to the plugin's repository somewhere

**This plugin is permanently free and open source, if you have purchased this plugin somewhere, we recommend contacting for a refund**
