package dev.crystalbot.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class BotMenu {
    public static final String TITLE = ChatColor.DARK_AQUA + "Practice Bot";
    public static final String ADVANCED_TITLE = ChatColor.DARK_PURPLE + "Advanced Custom Armor";
    public static final String APPEARANCE_TITLE = ChatColor.DARK_GREEN + "Bot Appearance";

    public static final int SLOT_SPAWN = 10; // line 2, slot 2
    public static final int SLOT_DOUBLE_BLAST = 12; // line 2, slot 4
    public static final int SLOT_SLOW_FALLING = 13; // line 2, slot 5
    public static final int SLOT_SHIELD = 14; // line 2, slot 6
    public static final int SLOT_TURN_PRESET = 15; // one slot after shield
    public static final int SLOT_TOTEM_LIMIT = 16; // after turn preset
    public static final int SLOT_ARMOR = 19; // line 3, slot 2 (1 slot back)
    public static final int SLOT_DESPAWN = 28; // one row below armor

    public static final int SLOT_STATUS_DOUBLE_BLAST = 21;
    public static final int SLOT_STATUS_SLOW_FALLING = 22;
    public static final int SLOT_STATUS_SHIELD = 23;
    public static final int SLOT_STATUS_TURN = 24;
    public static final int SLOT_STATUS_TOTEM_LIMIT = 25;
    public static final int SLOT_APPEARANCE = 31; // left of custom mode
    public static final int SLOT_CUSTOM_MODE = 32; // 1 row below shield status
    public static final int SLOT_FOLLOW_PLAYER = 33; // right of custom mode
    public static final int SLOT_CREDIT = 44;

    public static final int ADV_SLOT_SET = 20;
    public static final int ADV_SLOT_CANCEL = 3;
    public static final int ADV_SLOT_THORNS = 4;
    public static final int ADV_SLOT_CONFIRM = 5;
    public static final int ADV_SLOT_SET_PROTECTION = 11;
    public static final int ADV_SLOT_HELMET_PROTECTION = 12;
    public static final int ADV_SLOT_CHEST_PROTECTION = 13;
    public static final int ADV_SLOT_LEGS_PROTECTION = 14;
    public static final int ADV_SLOT_BOOTS_PROTECTION = 15;
    public static final int ADV_SLOT_HELMET = 21;
    public static final int ADV_SLOT_CHEST = 22;
    public static final int ADV_SLOT_LEGS = 23;
    public static final int ADV_SLOT_BOOTS = 24;

    public static final int APP_SLOT_CANCEL = 10;
    public static final int APP_SLOT_NAME = 12;
    public static final int APP_SLOT_SKIN = 14;
    public static final int APP_SLOT_LINK_TOGGLE = 16;

    private BotMenu() {
    }

    public static Inventory create(BotSettings settings) {
        return create(settings, true);
    }

    public static Inventory create(BotSettings settings, boolean hasBotSpawned) {
        Inventory inventory = org.bukkit.Bukkit.createInventory(null, 45, TITLE);

        fillAll(inventory, fillerPane());
        inventory.setItem(SLOT_CREDIT, creditFiller());

        inventory.setItem(SLOT_SPAWN, enchantedItem(Material.LIME_CONCRETE, ChatColor.GREEN + "Spawn / Replace Bot", List.of(
            ChatColor.GRAY + "Spawn a bot near you",
            ChatColor.GRAY + "If one exists, it gets replaced",
            ChatColor.AQUA + "Click to run"
        )));

        inventory.setItem(SLOT_ARMOR, item(
            settings.getArmorType() == ArmorType.NONE ? Material.WHITE_STAINED_GLASS_PANE : settings.getArmorType().getChest(),
            ChatColor.YELLOW + "Armor Type",
            List.of(
                ChatColor.GRAY + "Current: " + ChatColor.WHITE + settings.getArmorType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(SLOT_SHIELD, item(
            Material.SHIELD,
            ChatColor.YELLOW + "Shield",
            List.of(
                ChatColor.GRAY + "State: " + (settings.isShieldEnabled() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"),
                ChatColor.GRAY + "On = shield in main hand",
                ChatColor.GRAY + "Off = totems in hands",
                ChatColor.AQUA + "Click to toggle"
            )
        ));

        inventory.setItem(SLOT_SLOW_FALLING, item(
            Material.FEATHER,
            ChatColor.YELLOW + "Permanent Slow Falling",
            List.of(
                ChatColor.GRAY + "State: " + (settings.isSlowFallingEnabled() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"),
                ChatColor.GRAY + "Reapplies after pops",
                ChatColor.AQUA + "Click to toggle"
            )
        ));

        inventory.setItem(SLOT_TURN_PRESET, item(
            Material.CLOCK,
            ChatColor.YELLOW + "Aim Mode",
            List.of(
                ChatColor.GRAY + "Mode: " + ChatColor.WHITE + settings.getTurnPreset().getDisplay(),
                ChatColor.GRAY + "Cheat = instant snap",
                ChatColor.GRAY + "Aggressive/Balanced/Slow",
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(SLOT_TOTEM_LIMIT, item(
            Material.NAME_TAG,
            ChatColor.YELLOW + "Totem Limit",
            List.of(
                ChatColor.GRAY + "State: " + (settings.isTotemLimitEnabled() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off (Infinite)"),
                ChatColor.GRAY + "Amount: " + ChatColor.WHITE + settings.getTotemLimit(),
                ChatColor.AQUA + "Click to set amount"
            )
        ));

        inventory.setItem(SLOT_DOUBLE_BLAST, item(
            Material.NETHERITE_BOOTS,
            ChatColor.YELLOW + "Blast Prot Boots",
            List.of(
                ChatColor.GRAY + "State: " + (settings.isDoubleBlastProtection() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"),
                ChatColor.GRAY + "Enabled: Boots = Blast Protection IV",
                ChatColor.GRAY + "Disabled: Boots = Protection IV",
                ChatColor.GRAY + "Leggings stay Blast Prot IV",
                ChatColor.AQUA + "Click to toggle"
            )
        ));

        inventory.setItem(SLOT_STATUS_DOUBLE_BLAST, statusDye("Double Blast Boots", settings.isDoubleBlastProtection()));
        inventory.setItem(SLOT_STATUS_SLOW_FALLING, statusDye("Slow Falling", settings.isSlowFallingEnabled()));
        inventory.setItem(SLOT_STATUS_SHIELD, statusDye("Shield", settings.isShieldEnabled()));
        inventory.setItem(SLOT_STATUS_TURN, statusDye("Aim Tracking", settings.getTurnPreset().isTrackingEnabled()));
        inventory.setItem(SLOT_STATUS_TOTEM_LIMIT, statusDye("Limit Totems", settings.isTotemLimitEnabled()));

        inventory.setItem(SLOT_APPEARANCE, item(
            Material.PLAYER_HEAD,
            ChatColor.YELLOW + "Bot Appearance",
            List.of(
                ChatColor.GRAY + "Name: " + ChatColor.WHITE + settings.getCustomBotName(),
                ChatColor.GRAY + "Skin: " + ChatColor.WHITE + settings.getCustomBotSkin(),
                ChatColor.AQUA + "Click to customize"
            )
        ));

        inventory.setItem(SLOT_CUSTOM_MODE, item(
            Material.COMMAND_BLOCK,
            ChatColor.YELLOW + "Custom Mode",
            List.of(
                ChatColor.GRAY + "State: " + (settings.isCustomModeEnabled() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"),
                ChatColor.AQUA + "Left click: toggle",
                ChatColor.AQUA + "Right click: advanced menu"
            )
        ));

        inventory.setItem(SLOT_FOLLOW_PLAYER, item(
            Material.LEAD,
            ChatColor.YELLOW + "Follow Player",
            List.of(
                ChatColor.GRAY + "State: " + (settings.isFollowPlayerEnabled() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"),
                ChatColor.GRAY + "Bot will walk towards you",
                ChatColor.AQUA + "Click to toggle"
            )
        ));

        if (hasBotSpawned) {
            inventory.setItem(SLOT_DESPAWN, enchantedItem(
                Material.RED_CONCRETE,
                ChatColor.RED + "Despawn Bot",
                List.of(
                    ChatColor.GRAY + "Removes your current bot",
                    ChatColor.AQUA + "Click to despawn"
                )
            ));
        }

        return inventory;
    }

    public static Inventory createAdvanced(BotSettings settings) {
        Inventory inventory = org.bukkit.Bukkit.createInventory(null, 27, ADVANCED_TITLE);
        fillAll(inventory, fillerPane());

        inventory.setItem(ADV_SLOT_SET, item(
            settings.getCustomChestType().getChest(),
            ChatColor.YELLOW + "Whole Armor Set",
            List.of(
                ChatColor.GRAY + "Current: " + ChatColor.WHITE + settings.getCustomChestType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(ADV_SLOT_CANCEL, item(
            Material.RED_CONCRETE,
            ChatColor.RED + "Cancel",
            List.of(ChatColor.GRAY + "Discard changes")
        ));

        inventory.setItem(ADV_SLOT_THORNS, item(
            Material.ENCHANTED_BOOK,
            ChatColor.YELLOW + "Thorns",
            List.of(
                ChatColor.GRAY + "State: " + (settings.isCustomThornsEnabled() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"),
                ChatColor.GRAY + "Applies Thorns III to whole set",
                ChatColor.AQUA + "Click to toggle"
            )
        ));

        inventory.setItem(ADV_SLOT_CONFIRM, enchantedItem(
            Material.LIME_CONCRETE,
            ChatColor.GREEN + "Confirm",
            List.of(ChatColor.GRAY + "Apply custom mode settings")
        ));

        inventory.setItem(ADV_SLOT_SET_PROTECTION, item(
            Material.ENCHANTED_BOOK,
            ChatColor.YELLOW + "Set Protection Type",
            List.of(
                ChatColor.GRAY + "Type: " + ChatColor.WHITE + settings.getCustomSetProtectionType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(ADV_SLOT_HELMET_PROTECTION, item(
            Material.ENCHANTED_BOOK,
            ChatColor.YELLOW + "Helmet Protection",
            List.of(
                ChatColor.GRAY + "Type: " + ChatColor.WHITE + settings.getCustomHelmetProtectionType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(ADV_SLOT_CHEST_PROTECTION, item(
            Material.ENCHANTED_BOOK,
            ChatColor.YELLOW + "Chestplate Protection",
            List.of(
                ChatColor.GRAY + "Type: " + ChatColor.WHITE + settings.getCustomChestProtectionType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(ADV_SLOT_LEGS_PROTECTION, item(
            Material.ENCHANTED_BOOK,
            ChatColor.YELLOW + "Leggings Protection",
            List.of(
                ChatColor.GRAY + "Type: " + ChatColor.WHITE + settings.getCustomLegsProtectionType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(ADV_SLOT_BOOTS_PROTECTION, item(
            Material.ENCHANTED_BOOK,
            ChatColor.YELLOW + "Boots Protection",
            List.of(
                ChatColor.GRAY + "Type: " + ChatColor.WHITE + settings.getCustomBootsProtectionType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(ADV_SLOT_HELMET, item(
            settings.getCustomHelmetType().getHelmet(),
            ChatColor.YELLOW + "Helmet",
            List.of(
                ChatColor.GRAY + "Type: " + ChatColor.WHITE + settings.getCustomHelmetType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(ADV_SLOT_CHEST, item(
            settings.getCustomChestType().getChest(),
            ChatColor.YELLOW + "Chestplate",
            List.of(
                ChatColor.GRAY + "Type: " + ChatColor.WHITE + settings.getCustomChestType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(ADV_SLOT_LEGS, item(
            settings.getCustomLegsType().getLegs(),
            ChatColor.YELLOW + "Leggings",
            List.of(
                ChatColor.GRAY + "Type: " + ChatColor.WHITE + settings.getCustomLegsType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        inventory.setItem(ADV_SLOT_BOOTS, item(
            settings.getCustomBootsType().getBoots(),
            ChatColor.YELLOW + "Boots",
            List.of(
                ChatColor.GRAY + "Type: " + ChatColor.WHITE + settings.getCustomBootsType().getDisplay(),
                ChatColor.AQUA + "Click to cycle"
            )
        ));

        return inventory;
    }

    public static Inventory createAppearance(BotSettings settings) {
        Inventory inventory = org.bukkit.Bukkit.createInventory(null, 27, APPEARANCE_TITLE);
        fillAll(inventory, fillerPane());

        inventory.setItem(APP_SLOT_CANCEL, item(
            Material.RED_CONCRETE,
            ChatColor.RED + "Back",
            List.of(ChatColor.GRAY + "Return to main menu")
        ));

        inventory.setItem(APP_SLOT_NAME, item(
            Material.NAME_TAG,
            ChatColor.YELLOW + "Bot Name",
            List.of(
                ChatColor.GRAY + "Current: " + ChatColor.WHITE + settings.getCustomBotName(),
                ChatColor.AQUA + "Click to change"
            )
        ));

        inventory.setItem(APP_SLOT_SKIN, item(
            Material.PLAYER_HEAD,
            ChatColor.YELLOW + "Bot Skin",
            List.of(
                ChatColor.GRAY + "Current: " + ChatColor.WHITE + settings.getCustomBotSkin(),
                ChatColor.AQUA + "Click to change"
            )
        ));

        inventory.setItem(APP_SLOT_LINK_TOGGLE, item(
            settings.isLinkNameAndSkin() ? Material.CHAIN : Material.SHEARS,
            ChatColor.YELLOW + "Link Name & Skin",
            List.of(
                ChatColor.GRAY + "State: " + (settings.isLinkNameAndSkin() ? ChatColor.GREEN + "Linked" : ChatColor.RED + "Separate"),
                ChatColor.GRAY + "When linked, both will match",
                ChatColor.AQUA + "Click to toggle"
            )
        ));

        return inventory;
    }

    private static ItemStack statusDye(String label, boolean enabled) {
        return enchantedItem(
            enabled ? Material.LIME_DYE : Material.RED_DYE,
            (enabled ? ChatColor.GREEN : ChatColor.RED) + label,
            List.of(
                ChatColor.GRAY + "State: " + (enabled ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"),
                ChatColor.AQUA + "Click to toggle"
            )
        );
    }

    private static ItemStack creditFiller() {
        return item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, ChatColor.GRAY + " ", List.of(
            ChatColor.LIGHT_PURPLE + "☄Plugin by xkzuto"
        ));
    }

    private static ItemStack fillerPane() {
        return item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, ChatColor.GRAY + " ", List.of());
    }

    private static void fillAll(Inventory inventory, ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, item);
        }
    }

    private static ItemStack enchantedItem(Material material, String name, List<String> loreLines) {
        ItemStack stack = item(material, name, loreLines);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack item(Material material, String name, List<String> loreLines) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>(loreLines);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
