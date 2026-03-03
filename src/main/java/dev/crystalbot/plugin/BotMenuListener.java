package dev.crystalbot.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BotMenuListener implements Listener {
    private final CrystalBotPlugin plugin;
    private final BotManager botManager;
    private final Map<UUID, BotSettings> settingsByPlayer = new HashMap<>();
    private final Map<UUID, BotSettings> advancedDraftByPlayer = new HashMap<>();
    private final Map<UUID, BotSettings> appearanceDraftByPlayer = new HashMap<>();
    private final Map<UUID, Location> pendingTotemSign = new HashMap<>();
    private final Map<UUID, Location> pendingBotNameSign = new HashMap<>();
    private final Map<UUID, Location> pendingBotSkinSign = new HashMap<>();

    public BotMenuListener(CrystalBotPlugin plugin, BotManager botManager) {
        this.plugin = plugin;
        this.botManager = botManager;
    }

    public void openMenu(Player player) {
        BotSettings settings = getSettings(player.getUniqueId());
        boolean hasBotSpawned = botManager.hasBotForPlayer(player.getUniqueId());
        player.openInventory(BotMenu.create(settings, hasBotSpawned));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(BotMenu.ADVANCED_TITLE)) {
            handleAdvancedClick(event);
            return;
        }

        if (event.getView().getTitle().equals(BotMenu.APPEARANCE_TITLE)) {
            handleAppearanceClick(event);
            return;
        }

        Inventory top = event.getView().getTopInventory();
        if (top == null || !event.getView().getTitle().equals(BotMenu.TITLE)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        BotSettings settings = getSettings(player.getUniqueId());
        int slot = event.getRawSlot();

        if (slot >= top.getSize()) {
            return;
        }

        if (slot == BotMenu.SLOT_CREDIT) {
            player.sendActionBar(Component.text("☄Plugin by xkzuto", NamedTextColor.LIGHT_PURPLE));
            return;
        }

        if (slot != BotMenu.SLOT_SPAWN
            && slot != BotMenu.SLOT_DESPAWN
            && slot != BotMenu.SLOT_ARMOR
            && slot != BotMenu.SLOT_DOUBLE_BLAST
            && slot != BotMenu.SLOT_SHIELD
            && slot != BotMenu.SLOT_SLOW_FALLING
            && slot != BotMenu.SLOT_TURN_PRESET
            && slot != BotMenu.SLOT_TOTEM_LIMIT
            && slot != BotMenu.SLOT_CUSTOM_MODE
            && slot != BotMenu.SLOT_APPEARANCE
            && slot != BotMenu.SLOT_FOLLOW_PLAYER
            && slot != BotMenu.SLOT_STATUS_DOUBLE_BLAST
            && slot != BotMenu.SLOT_STATUS_SLOW_FALLING
            && slot != BotMenu.SLOT_STATUS_SHIELD
            && slot != BotMenu.SLOT_STATUS_TURN
            && slot != BotMenu.SLOT_STATUS_TOTEM_LIMIT) {
            return;
        }

        if (slot == BotMenu.SLOT_ARMOR) {
            settings.toggleArmorType();
            saveSettings(player.getUniqueId(), settings);
            botManager.updateBotSettings(player, settings);
            player.sendActionBar(Component.text("Armor: " + settings.getArmorType().getDisplay(), NamedTextColor.YELLOW));
            player.openInventory(BotMenu.create(settings));
            return;
        }

        if (slot == BotMenu.SLOT_DOUBLE_BLAST || slot == BotMenu.SLOT_STATUS_DOUBLE_BLAST) {
            settings.toggleDoubleBlastProtection();
            saveSettings(player.getUniqueId(), settings);
            botManager.updateBotSettings(player, settings);
            player.sendActionBar(Component.text("Double Blast: " + (settings.isDoubleBlastProtection() ? "Enabled" : "Disabled"), settings.isDoubleBlastProtection() ? NamedTextColor.GREEN : NamedTextColor.RED));
            player.openInventory(BotMenu.create(settings));
            return;
        }

        if (slot == BotMenu.SLOT_SLOW_FALLING || slot == BotMenu.SLOT_STATUS_SLOW_FALLING) {
            settings.toggleSlowFallingEnabled();
            saveSettings(player.getUniqueId(), settings);
            botManager.updateBotSettings(player, settings);
            player.sendActionBar(Component.text("Slow Falling: " + (settings.isSlowFallingEnabled() ? "Enabled" : "Disabled"), settings.isSlowFallingEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
            player.openInventory(BotMenu.create(settings));
            return;
        }

        if (slot == BotMenu.SLOT_SHIELD || slot == BotMenu.SLOT_STATUS_SHIELD) {
            settings.toggleShieldEnabled();
            saveSettings(player.getUniqueId(), settings);
            botManager.updateBotSettings(player, settings);
            player.sendActionBar(Component.text("Shield: " + (settings.isShieldEnabled() ? "Enabled" : "Disabled"), settings.isShieldEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
            player.openInventory(BotMenu.create(settings));
            return;
        }

        if (slot == BotMenu.SLOT_TURN_PRESET) {
            settings.cycleTurnPreset();
            saveSettings(player.getUniqueId(), settings);
            botManager.updateBotSettings(player, settings);
            player.sendActionBar(Component.text("Turn Preset: " + settings.getTurnPreset().getDisplay(), NamedTextColor.AQUA));
            player.openInventory(BotMenu.create(settings));
            return;
        }

        if (slot == BotMenu.SLOT_STATUS_TURN) {
            settings.setTurnPreset(settings.getTurnPreset().isTrackingEnabled() ? TurnPreset.OFF : TurnPreset.CHEAT);
            saveSettings(player.getUniqueId(), settings);
            botManager.updateBotSettings(player, settings);
            player.sendActionBar(Component.text("Aim Tracking: " + (settings.getTurnPreset().isTrackingEnabled() ? "On" : "Off"), settings.getTurnPreset().isTrackingEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
            player.openInventory(BotMenu.create(settings));
            return;
        }

        if (slot == BotMenu.SLOT_TOTEM_LIMIT) {
            player.closeInventory();
            openTotemLimitSign(player);
            return;
        }

        if (slot == BotMenu.SLOT_STATUS_TOTEM_LIMIT) {
            settings.toggleTotemLimitEnabled();
            saveSettings(player.getUniqueId(), settings);
            botManager.updateBotSettings(player, settings);
            player.sendActionBar(Component.text("Totem Limit: " + (settings.isTotemLimitEnabled() ? "Enabled" : "Infinite"), settings.isTotemLimitEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
            player.openInventory(BotMenu.create(settings));
            return;
        }

        if (slot == BotMenu.SLOT_APPEARANCE) {
            BotSettings draft = settings.copy();
            appearanceDraftByPlayer.put(player.getUniqueId(), draft);
            player.openInventory(BotMenu.createAppearance(draft));
            return;
        }

        if (slot == BotMenu.SLOT_FOLLOW_PLAYER) {
            settings.toggleFollowPlayerEnabled();
            saveSettings(player.getUniqueId(), settings);
            botManager.updateBotSettings(player, settings);
            player.sendActionBar(Component.text("Follow Player: " + (settings.isFollowPlayerEnabled() ? "Enabled" : "Disabled"), settings.isFollowPlayerEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
            player.openInventory(BotMenu.create(settings, botManager.hasBotForPlayer(player.getUniqueId())));
            return;
        }

        if (slot == BotMenu.SLOT_CUSTOM_MODE) {
            if (event.isRightClick()) {
                BotSettings draft = settings.copy();
                advancedDraftByPlayer.put(player.getUniqueId(), draft);
                player.openInventory(BotMenu.createAdvanced(draft));
            } else {
                settings.toggleCustomModeEnabled();
                saveSettings(player.getUniqueId(), settings);
                botManager.updateBotSettings(player, settings);
                player.sendActionBar(Component.text("Custom Mode: " + (settings.isCustomModeEnabled() ? "Enabled" : "Disabled"), settings.isCustomModeEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
                player.openInventory(BotMenu.create(settings));
            }
            return;
        }

        if (slot == BotMenu.SLOT_SPAWN) {
            player.closeInventory();
            botManager.spawnOrReplaceBot(player, settings.copy());
            return;
        }

        if (slot == BotMenu.SLOT_DESPAWN) {
            player.closeInventory();
            botManager.despawnForPlayer(player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(BotMenu.TITLE) 
            || event.getView().getTitle().equals(BotMenu.ADVANCED_TITLE)
            || event.getView().getTitle().equals(BotMenu.APPEARANCE_TITLE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        // Handle totem limit sign
        Location totemExpected = pendingTotemSign.remove(event.getPlayer().getUniqueId());
        if (totemExpected != null && event.getBlock().getLocation().equals(totemExpected)) {
            handleTotemSignInput(event, totemExpected);
            return;
        }

        // Handle bot name sign
        Location nameExpected = pendingBotNameSign.remove(event.getPlayer().getUniqueId());
        if (nameExpected != null && event.getBlock().getLocation().equals(nameExpected)) {
            handleBotNameSignInput(event, nameExpected);
            return;
        }

        // Handle bot skin sign
        Location skinExpected = pendingBotSkinSign.remove(event.getPlayer().getUniqueId());
        if (skinExpected != null && event.getBlock().getLocation().equals(skinExpected)) {
            handleBotSkinSignInput(event, skinExpected);
        }
    }

    private void handleTotemSignInput(SignChangeEvent event, Location signLoc) {
        restoreTempSign(signLoc);

        String input = "";
        for (String line : event.getLines()) {
            String digits = line == null ? "" : line.replaceAll("[^0-9]", "").trim();
            if (!digits.isEmpty()) {
                input = digits;
                break;
            }
        }

        if (input.isEmpty()) {
            event.getPlayer().sendActionBar(Component.text("Numbers only", NamedTextColor.RED));
            BotSettings settings = getSettings(event.getPlayer().getUniqueId());
            event.getPlayer().openInventory(BotMenu.create(settings));
            return;
        }

        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            event.getPlayer().sendActionBar(Component.text("Invalid number", NamedTextColor.RED));
            return;
        }

        if (value <= 0 || value > 100000) {
            event.getPlayer().sendActionBar(Component.text("Use 1 - 100000", NamedTextColor.RED));
            return;
        }

        BotSettings settings = getSettings(event.getPlayer().getUniqueId());
        settings.setTotemLimit(value);
        settings.setTotemLimitEnabled(true);
        saveSettings(event.getPlayer().getUniqueId(), settings);
        botManager.updateBotSettings(event.getPlayer(), settings);

        event.getPlayer().sendActionBar(Component.text("Totem Limit: " + value, NamedTextColor.GREEN));
        event.getPlayer().openInventory(BotMenu.create(settings));
    }

    private void handleBotNameSignInput(SignChangeEvent event, Location signLoc) {
        restoreTempSign(signLoc);

        StringBuilder input = new StringBuilder();
        for (String line : event.getLines()) {
            if (line != null && !line.trim().isEmpty()) {
                if (input.length() > 0) {
                    input.append(" ");
                }
                input.append(line.trim());
            }
        }

        String name = input.toString().trim();
        if (name.isEmpty()) {
            event.getPlayer().sendActionBar(Component.text("Name cannot be empty", NamedTextColor.RED));
            BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(event.getPlayer().getUniqueId(), id -> getSettings(id).copy());
            event.getPlayer().openInventory(BotMenu.createAppearance(draft));
            return;
        }

        if (name.length() > 32) {
            name = name.substring(0, 32);
        }

        BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(event.getPlayer().getUniqueId(), id -> getSettings(id).copy());
        draft.setCustomBotName(name);
        
        // Instantly apply changes
        UUID playerId = event.getPlayer().getUniqueId();
        settingsByPlayer.put(playerId, draft.copy());
        saveSettings(playerId, draft);
        botManager.updateBotSettings(event.getPlayer(), draft);
        
        event.getPlayer().sendActionBar(Component.text("Bot Name: " + name, NamedTextColor.GREEN));
        event.getPlayer().openInventory(BotMenu.createAppearance(draft));
    }

    private void handleBotSkinSignInput(SignChangeEvent event, Location signLoc) {
        restoreTempSign(signLoc);

        StringBuilder input = new StringBuilder();
        for (String line : event.getLines()) {
            if (line != null && !line.trim().isEmpty()) {
                input.append(line.trim());
            }
        }

        String skin = input.toString().trim();
        if (skin.isEmpty()) {
            event.getPlayer().sendActionBar(Component.text("Skin name cannot be empty", NamedTextColor.RED));
            BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(event.getPlayer().getUniqueId(), id -> getSettings(id).copy());
            event.getPlayer().openInventory(BotMenu.createAppearance(draft));
            return;
        }

        if (skin.length() > 16) {
            skin = skin.substring(0, 16);
        }

        BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(event.getPlayer().getUniqueId(), id -> getSettings(id).copy());
        draft.setCustomBotSkin(skin);
        
        // Instantly apply changes
        UUID playerId = event.getPlayer().getUniqueId();
        settingsByPlayer.put(playerId, draft.copy());
        saveSettings(playerId, draft);
        botManager.updateBotSettings(event.getPlayer(), draft);
        
        event.getPlayer().sendActionBar(Component.text("Bot Skin: " + skin, NamedTextColor.GREEN));
        event.getPlayer().openInventory(BotMenu.createAppearance(draft));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BotSettings settings = settingsByPlayer.get(event.getPlayer().getUniqueId());
        if (settings != null) {
            saveSettings(event.getPlayer().getUniqueId(), settings);
        }
        advancedDraftByPlayer.remove(event.getPlayer().getUniqueId());
        appearanceDraftByPlayer.remove(event.getPlayer().getUniqueId());
        cleanupPendingSign(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        BotSettings settings = settingsByPlayer.get(event.getPlayer().getUniqueId());
        if (settings != null) {
            saveSettings(event.getPlayer().getUniqueId(), settings);
        }
        advancedDraftByPlayer.remove(event.getPlayer().getUniqueId());
        appearanceDraftByPlayer.remove(event.getPlayer().getUniqueId());
        cleanupPendingSign(event.getPlayer().getUniqueId());
    }

    private void openTotemLimitSign(Player player) {
        Location base = player.getLocation().getBlock().getLocation();
        Location signLoc = null;
        for (int y = 2; y <= 6; y++) {
            Location probe = base.clone().add(0, y, 0);
            if (probe.getBlock().getType().isAir()) {
                signLoc = probe;
                break;
            }
        }

        if (signLoc == null) {
            player.sendActionBar(Component.text("Could not open sign input", NamedTextColor.RED));
            BotSettings settings = getSettings(player.getUniqueId());
            player.openInventory(BotMenu.create(settings));
            return;
        }

        Block block = signLoc.getBlock();
        block.setType(Material.OAK_SIGN, false);
        if (!(block.getState() instanceof Sign sign)) {
            block.setType(Material.AIR, false);
            player.sendActionBar(Component.text("Could not open sign input", NamedTextColor.RED));
            BotSettings settings = getSettings(player.getUniqueId());
            player.openInventory(BotMenu.create(settings));
            return;
        }

        sign.setLine(0, "Totem Limit");
        sign.setLine(1, "");
        sign.setLine(2, "1-100000");
        sign.setLine(3, "numbers only");
        sign.update(true, false);

        pendingTotemSign.put(player.getUniqueId(), signLoc);

        boolean opened = false;
        try {
            java.lang.reflect.Method openSign = player.getClass().getMethod("openSign", Sign.class);
            openSign.invoke(player, sign);
            opened = true;
        } catch (Throwable ignored) {
        }

        if (!opened) {
            pendingTotemSign.remove(player.getUniqueId());
            restoreTempSign(signLoc);
            player.sendActionBar(Component.text("Sign input unsupported on this build", NamedTextColor.RED));
            BotSettings settings = getSettings(player.getUniqueId());
            player.openInventory(BotMenu.create(settings));
        }
    }

    private void restoreTempSign(Location location) {
        if (location == null) {
            return;
        }
        if (location.getBlock().getType() == Material.OAK_SIGN) {
            location.getBlock().setType(Material.AIR, false);
        }
    }

    private void cleanupPendingSign(UUID playerId) {
        Location location = pendingTotemSign.remove(playerId);
        restoreTempSign(location);
        location = pendingBotNameSign.remove(playerId);
        restoreTempSign(location);
        location = pendingBotSkinSign.remove(playerId);
        restoreTempSign(location);
    }

    private void handleAdvancedClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= top.getSize()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        BotSettings draft = advancedDraftByPlayer.computeIfAbsent(playerId, id -> getSettings(id).copy());

        if (slot == BotMenu.ADV_SLOT_CANCEL) {
            advancedDraftByPlayer.remove(playerId);
            player.openInventory(BotMenu.create(getSettings(playerId)));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_CONFIRM) {
            draft.setCustomModeEnabled(true);
            settingsByPlayer.put(playerId, draft.copy());
            saveSettings(playerId, draft);
            botManager.updateBotSettings(player, draft);
            advancedDraftByPlayer.remove(playerId);
            player.sendActionBar(Component.text("Custom armor applied", NamedTextColor.GREEN));
            player.openInventory(BotMenu.create(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_SET) {
            draft.cycleWholeCustomSet();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_THORNS) {
            draft.toggleCustomThornsEnabled();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_SET_PROTECTION) {
            draft.cycleCustomSetProtectionType();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_HELMET_PROTECTION) {
            draft.cycleCustomHelmetProtectionType();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_CHEST_PROTECTION) {
            draft.cycleCustomChestProtectionType();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_LEGS_PROTECTION) {
            draft.cycleCustomLegsProtectionType();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_BOOTS_PROTECTION) {
            draft.cycleCustomBootsProtectionType();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_HELMET) {
            draft.cycleCustomHelmet();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_CHEST) {
            draft.cycleCustomChest();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_LEGS) {
            draft.cycleCustomLegs();
            player.openInventory(BotMenu.createAdvanced(draft));
            return;
        }

        if (slot == BotMenu.ADV_SLOT_BOOTS) {
            draft.cycleCustomBoots();
            player.openInventory(BotMenu.createAdvanced(draft));
        }
    }

    private void handleAppearanceClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= top.getSize()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(playerId, id -> getSettings(id).copy());

        if (slot == BotMenu.APP_SLOT_CANCEL) {
            appearanceDraftByPlayer.remove(playerId);
            player.openInventory(BotMenu.create(getSettings(playerId), botManager.hasBotForPlayer(playerId)));
            return;
        }

        if (slot == BotMenu.APP_SLOT_NAME) {
            player.closeInventory();
            openBotNameSign(player);
            return;
        }

        if (slot == BotMenu.APP_SLOT_SKIN) {
            player.closeInventory();
            openBotSkinSign(player);
            return;
        }

        if (slot == BotMenu.APP_SLOT_LINK_TOGGLE) {
            draft.toggleLinkNameAndSkin();
            // Apply changes and save
            settingsByPlayer.put(playerId, draft.copy());
            saveSettings(playerId, draft);
            botManager.updateBotSettings(player, draft);
            player.sendActionBar(Component.text("Name & Skin: " + (draft.isLinkNameAndSkin() ? "Linked" : "Separate"), draft.isLinkNameAndSkin() ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
            player.openInventory(BotMenu.createAppearance(draft));
        }
    }

    private void openBotNameSign(Player player) {
        Location base = player.getLocation().getBlock().getLocation();
        Location signLoc = null;
        for (int y = 2; y <= 6; y++) {
            Location probe = base.clone().add(0, y, 0);
            if (probe.getBlock().getType().isAir()) {
                signLoc = probe;
                break;
            }
        }

        if (signLoc == null) {
            player.sendActionBar(Component.text("Could not open sign input", NamedTextColor.RED));
            BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(player.getUniqueId(), id -> getSettings(id).copy());
            player.openInventory(BotMenu.createAppearance(draft));
            return;
        }

        Block block = signLoc.getBlock();
        block.setType(Material.OAK_SIGN, false);
        if (!(block.getState() instanceof Sign sign)) {
            block.setType(Material.AIR, false);
            player.sendActionBar(Component.text("Could not open sign input", NamedTextColor.RED));
            BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(player.getUniqueId(), id -> getSettings(id).copy());
            player.openInventory(BotMenu.createAppearance(draft));
            return;
        }

        sign.setLine(0, "Bot Name");
        sign.setLine(1, "");
        sign.setLine(2, "Enter name");
        sign.setLine(3, "(1-32 chars)");
        sign.update(true, false);

        pendingBotNameSign.put(player.getUniqueId(), signLoc);

        boolean opened = false;
        try {
            java.lang.reflect.Method openSign = player.getClass().getMethod("openSign", Sign.class);
            openSign.invoke(player, sign);
            opened = true;
        } catch (Throwable ignored) {
        }

        if (!opened) {
            pendingBotNameSign.remove(player.getUniqueId());
            restoreTempSign(signLoc);
            player.sendActionBar(Component.text("Sign input unsupported on this build", NamedTextColor.RED));
            BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(player.getUniqueId(), id -> getSettings(id).copy());
            player.openInventory(BotMenu.createAppearance(draft));
        }
    }

    private void openBotSkinSign(Player player) {
        Location base = player.getLocation().getBlock().getLocation();
        Location signLoc = null;
        for (int y = 2; y <= 6; y++) {
            Location probe = base.clone().add(0, y, 0);
            if (probe.getBlock().getType().isAir()) {
                signLoc = probe;
                break;
            }
        }

        if (signLoc == null) {
            player.sendActionBar(Component.text("Could not open sign input", NamedTextColor.RED));
            BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(player.getUniqueId(), id -> getSettings(id).copy());
            player.openInventory(BotMenu.createAppearance(draft));
            return;
        }

        Block block = signLoc.getBlock();
        block.setType(Material.OAK_SIGN, false);
        if (!(block.getState() instanceof Sign sign)) {
            block.setType(Material.AIR, false);
            player.sendActionBar(Component.text("Could not open sign input", NamedTextColor.RED));
            BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(player.getUniqueId(), id -> getSettings(id).copy());
            player.openInventory(BotMenu.createAppearance(draft));
            return;
        }

        sign.setLine(0, "Bot Skin");
        sign.setLine(1, "");
        sign.setLine(2, "Minecraft name");
        sign.setLine(3, "(1-16 chars)");
        sign.update(true, false);

        pendingBotSkinSign.put(player.getUniqueId(), signLoc);

        boolean opened = false;
        try {
            java.lang.reflect.Method openSign = player.getClass().getMethod("openSign", Sign.class);
            openSign.invoke(player, sign);
            opened = true;
        } catch (Throwable ignored) {
        }

        if (!opened) {
            pendingBotSkinSign.remove(player.getUniqueId());
            restoreTempSign(signLoc);
            player.sendActionBar(Component.text("Sign input unsupported on this build", NamedTextColor.RED));
            BotSettings draft = appearanceDraftByPlayer.computeIfAbsent(player.getUniqueId(), id -> getSettings(id).copy());
            player.openInventory(BotMenu.createAppearance(draft));
        }
    }

    private BotSettings getSettings(UUID playerId) {
        return settingsByPlayer.computeIfAbsent(playerId, this::loadSettings);
    }

    private BotSettings loadSettings(UUID playerId) {
        BotSettings settings = new BotSettings();
        String path = "players." + playerId + ".";

        String armorRaw = plugin.getConfig().getString(path + "armorType", settings.getArmorType().name());
        try {
            settings.setArmorType(ArmorType.valueOf(armorRaw));
        } catch (IllegalArgumentException ignored) {
        }

        settings.setDoubleBlastProtection(plugin.getConfig().getBoolean(path + "doubleBlastProtection", settings.isDoubleBlastProtection()));
        settings.setShieldEnabled(plugin.getConfig().getBoolean(path + "shieldEnabled", settings.isShieldEnabled()));
        settings.setSlowFallingEnabled(plugin.getConfig().getBoolean(path + "slowFallingEnabled", settings.isSlowFallingEnabled()));
        settings.setTotemLimitEnabled(plugin.getConfig().getBoolean(path + "totemLimitEnabled", settings.isTotemLimitEnabled()));
        settings.setTotemLimit(plugin.getConfig().getInt(path + "totemLimit", settings.getTotemLimit()));
        settings.setCustomModeEnabled(plugin.getConfig().getBoolean(path + "customModeEnabled", settings.isCustomModeEnabled()));

        String customHelmetRaw = plugin.getConfig().getString(path + "customHelmetType", settings.getCustomHelmetType().name());
        String customChestRaw = plugin.getConfig().getString(path + "customChestType", settings.getCustomChestType().name());
        String customLegsRaw = plugin.getConfig().getString(path + "customLegsType", settings.getCustomLegsType().name());
        String customBootsRaw = plugin.getConfig().getString(path + "customBootsType", settings.getCustomBootsType().name());
        try { settings.setCustomHelmetType(ArmorType.valueOf(customHelmetRaw)); } catch (IllegalArgumentException ignored) {}
        try { settings.setCustomChestType(ArmorType.valueOf(customChestRaw)); } catch (IllegalArgumentException ignored) {}
        try { settings.setCustomLegsType(ArmorType.valueOf(customLegsRaw)); } catch (IllegalArgumentException ignored) {}
        try { settings.setCustomBootsType(ArmorType.valueOf(customBootsRaw)); } catch (IllegalArgumentException ignored) {}

        String customSetProtectionRaw = plugin.getConfig().getString(path + "customSetProtectionType", settings.getCustomSetProtectionType().name());
        String customHelmetProtectionRaw = plugin.getConfig().getString(path + "customHelmetProtectionType", settings.getCustomHelmetProtectionType().name());
        String customChestProtectionRaw = plugin.getConfig().getString(path + "customChestProtectionType", settings.getCustomChestProtectionType().name());
        String customLegsProtectionRaw = plugin.getConfig().getString(path + "customLegsProtectionType", settings.getCustomLegsProtectionType().name());
        String customBootsProtectionRaw = plugin.getConfig().getString(path + "customBootsProtectionType", settings.getCustomBootsProtectionType().name());
        try { settings.setCustomSetProtectionType(CustomProtectionType.valueOf(customSetProtectionRaw)); } catch (IllegalArgumentException ignored) {}
        try { settings.setCustomHelmetProtectionType(CustomProtectionType.valueOf(customHelmetProtectionRaw)); } catch (IllegalArgumentException ignored) {}
        try { settings.setCustomChestProtectionType(CustomProtectionType.valueOf(customChestProtectionRaw)); } catch (IllegalArgumentException ignored) {}
        try { settings.setCustomLegsProtectionType(CustomProtectionType.valueOf(customLegsProtectionRaw)); } catch (IllegalArgumentException ignored) {}
        try { settings.setCustomBootsProtectionType(CustomProtectionType.valueOf(customBootsProtectionRaw)); } catch (IllegalArgumentException ignored) {}

        if (!plugin.getConfig().contains(path + "customSetProtectionType")) {
            int legacyProtectionLevel = plugin.getConfig().getInt(path + "customProtectionLevel", 4);
            if (legacyProtectionLevel > 0) {
                settings.setWholeCustomProtectionType(CustomProtectionType.PROTECTION);
            }
        }

        settings.setCustomThornsEnabled(plugin.getConfig().getBoolean(path + "customThornsEnabled", settings.isCustomThornsEnabled()));

        settings.setCustomBotName(plugin.getConfig().getString(path + "customBotName", settings.getCustomBotName()));
        settings.setCustomBotSkin(plugin.getConfig().getString(path + "customBotSkin", settings.getCustomBotSkin()));
        settings.setLinkNameAndSkin(plugin.getConfig().getBoolean(path + "linkNameAndSkin", settings.isLinkNameAndSkin()));
        settings.setFollowPlayerEnabled(plugin.getConfig().getBoolean(path + "followPlayerEnabled", settings.isFollowPlayerEnabled()));

        String turnRaw = plugin.getConfig().getString(path + "turnPreset", settings.getTurnPreset().name());
        try {
            settings.setTurnPreset(TurnPreset.valueOf(turnRaw));
        } catch (IllegalArgumentException ignored) {
        }

        return settings;
    }

    private void saveSettings(UUID playerId, BotSettings settings) {
        String path = "players." + playerId + ".";
        plugin.getConfig().set(path + "armorType", settings.getArmorType().name());
        plugin.getConfig().set(path + "doubleBlastProtection", settings.isDoubleBlastProtection());
        plugin.getConfig().set(path + "shieldEnabled", settings.isShieldEnabled());
        plugin.getConfig().set(path + "slowFallingEnabled", settings.isSlowFallingEnabled());
        plugin.getConfig().set(path + "totemLimitEnabled", settings.isTotemLimitEnabled());
        plugin.getConfig().set(path + "totemLimit", settings.getTotemLimit());
        plugin.getConfig().set(path + "turnPreset", settings.getTurnPreset().name());
        plugin.getConfig().set(path + "customModeEnabled", settings.isCustomModeEnabled());
        plugin.getConfig().set(path + "customHelmetType", settings.getCustomHelmetType().name());
        plugin.getConfig().set(path + "customChestType", settings.getCustomChestType().name());
        plugin.getConfig().set(path + "customLegsType", settings.getCustomLegsType().name());
        plugin.getConfig().set(path + "customBootsType", settings.getCustomBootsType().name());
        plugin.getConfig().set(path + "customSetProtectionType", settings.getCustomSetProtectionType().name());
        plugin.getConfig().set(path + "customHelmetProtectionType", settings.getCustomHelmetProtectionType().name());
        plugin.getConfig().set(path + "customChestProtectionType", settings.getCustomChestProtectionType().name());
        plugin.getConfig().set(path + "customLegsProtectionType", settings.getCustomLegsProtectionType().name());
        plugin.getConfig().set(path + "customBootsProtectionType", settings.getCustomBootsProtectionType().name());
        plugin.getConfig().set(path + "customProtectionLevel", null);
        plugin.getConfig().set(path + "customThornsEnabled", settings.isCustomThornsEnabled());
        plugin.getConfig().set(path + "customBotName", settings.getCustomBotName());
        plugin.getConfig().set(path + "customBotSkin", settings.getCustomBotSkin());
        plugin.getConfig().set(path + "linkNameAndSkin", settings.isLinkNameAndSkin());
        plugin.getConfig().set(path + "followPlayerEnabled", settings.isFollowPlayerEnabled());
        plugin.saveConfig();
    }
}
