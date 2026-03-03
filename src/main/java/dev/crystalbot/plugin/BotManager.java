package dev.crystalbot.plugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BotManager implements Listener {
    private static final double AUTO_DESPAWN_DISTANCE_SQ = 100.0 * 100.0;
    private static final int SHIELD_VISUAL_WARMUP_TICKS = 3;
    private static final int SHIELD_VISUAL_RETRY_TICKS = 8;
    private static final long SHIELD_AXE_DISABLE_MS = 5000L;
    private static final String DEFAULT_SKIN_NAME = "xkzuto";

    private final CrystalBotPlugin plugin;
    private final Map<UUID, NPC> botByOwner = new HashMap<>();
    private final Map<UUID, BotRuntime> runtimeByEntity = new HashMap<>();
    private final BukkitTask aiTask;

    public BotManager(CrystalBotPlugin plugin) {
        this.plugin = plugin;
        this.aiTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickAi, 1L, 2L);
    }

    public void spawnOrReplaceBot(Player owner, BotSettings settings) {
        if (!CitizensAPI.hasImplementation()) {
            owner.sendMessage(Component.text("Citizens is not loaded yet. Try again in a moment.", NamedTextColor.RED));
            return;
        }

        despawnForOwner(owner.getUniqueId());

        Location spawnLocation = owner.getLocation().clone();
        spawnLocation.add(0, 1.5, 0); // Spawn above ground
        spawnLocation.add(spawnLocation.getDirection().normalize().multiply(2.0)); // 2 blocks forward
        spawnLocation.setPitch(0f);
        spawnLocation.setYaw(owner.getLocation().getYaw());

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(org.bukkit.entity.EntityType.PLAYER, settings.getCustomBotName());
        npc.setProtected(false);
        npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, true);

        if (!npc.spawn(spawnLocation)) {
            npc.destroy();
            owner.sendMessage(Component.text("Failed to spawn player bot.", NamedTextColor.RED));
            return;
        }

        if (!(npc.getEntity() instanceof LivingEntity bot)) {
            npc.destroy();
            owner.sendMessage(Component.text("Spawned NPC is not a living entity.", NamedTextColor.RED));
            return;
        }

        botByOwner.put(owner.getUniqueId(), npc);
        runtimeByEntity.put(bot.getUniqueId(), new BotRuntime(owner.getUniqueId(), settings.copy()));

        var maxHealth = bot.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0);
        }
        bot.setHealth(20.0);
        bot.setCollidable(true);

        // Delay equipment to let entity fully initialize
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!bot.isValid() || bot.isDead()) {
                return;
            }
            applySkin(npc, settings.getCustomBotSkin());
            equipBot(bot, settings);
        });

        // Reapply after skin loads
        Bukkit.getScheduler().runTaskLater(plugin, () -> reapplySpawnState(owner.getUniqueId()), 15L);

        // Silent post-spawn recovery: emulate shield re-toggle after startup race windows.
        if (settings.isShieldEnabled()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> refreshShieldAfterSpawn(owner.getUniqueId()), 100L);
        }

        owner.sendMessage(Component.text("Practice player bot spawned.", NamedTextColor.GREEN));
    }

    public boolean hasBotForPlayer(UUID playerId) {
        return botByOwner.containsKey(playerId);
    }

    public void despawnForPlayer(Player owner) {
        NPC npc = botByOwner.get(owner.getUniqueId());
        if (npc == null) {
            owner.sendMessage(Component.text("No bot currently spawned.", NamedTextColor.RED));
            return;
        }
        despawnForOwner(owner.getUniqueId());
        owner.sendMessage(Component.text("Practice bot despawned.", NamedTextColor.GREEN));
    }

    public void updateBotSettings(Player owner, BotSettings newSettings) {
        NPC npc = botByOwner.get(owner.getUniqueId());
        if (npc == null || !npc.isSpawned() || !(npc.getEntity() instanceof LivingEntity bot)) {
            owner.sendMessage(Component.text("No bot currently spawned.", NamedTextColor.RED));
            return;
        }

        BotRuntime runtime = runtimeByEntity.get(bot.getUniqueId());
        if (runtime == null) {
            return;
        }

        // Update settings
        runtime.settings.setArmorType(newSettings.getArmorType());
        runtime.settings.setDoubleBlastProtection(newSettings.isDoubleBlastProtection());
        runtime.settings.setShieldEnabled(newSettings.isShieldEnabled());
        runtime.settings.setSlowFallingEnabled(newSettings.isSlowFallingEnabled());
        runtime.settings.setTotemLimitEnabled(newSettings.isTotemLimitEnabled());
        runtime.settings.setTotemLimit(newSettings.getTotemLimit());
        runtime.settings.setTurnPreset(newSettings.getTurnPreset());
        runtime.settings.setCustomModeEnabled(newSettings.isCustomModeEnabled());
        runtime.settings.setCustomHelmetType(newSettings.getCustomHelmetType());
        runtime.settings.setCustomChestType(newSettings.getCustomChestType());
        runtime.settings.setCustomLegsType(newSettings.getCustomLegsType());
        runtime.settings.setCustomBootsType(newSettings.getCustomBootsType());
        runtime.settings.setCustomSetProtectionType(newSettings.getCustomSetProtectionType());
        runtime.settings.setCustomHelmetProtectionType(newSettings.getCustomHelmetProtectionType());
        runtime.settings.setCustomChestProtectionType(newSettings.getCustomChestProtectionType());
        runtime.settings.setCustomLegsProtectionType(newSettings.getCustomLegsProtectionType());
        runtime.settings.setCustomBootsProtectionType(newSettings.getCustomBootsProtectionType());
        runtime.settings.setCustomThornsEnabled(newSettings.isCustomThornsEnabled());
        runtime.settings.setFollowPlayerEnabled(newSettings.isFollowPlayerEnabled());
        runtime.remainingTotemPops = runtime.settings.isTotemLimitEnabled() ? runtime.settings.getTotemLimit() : -1;

        // Track if appearance changed to trigger reapply
        boolean nameChanged = false;
        boolean skinChanged = false;

        // Apply name and skin changes if different
        if (!runtime.settings.getCustomBotName().equals(newSettings.getCustomBotName())) {
            runtime.settings.setCustomBotName(newSettings.getCustomBotName());
            npc.setName(newSettings.getCustomBotName());
            nameChanged = true;
        }
        if (!runtime.settings.getCustomBotSkin().equals(newSettings.getCustomBotSkin())) {
            runtime.settings.setCustomBotSkin(newSettings.getCustomBotSkin());
            applySkin(npc, newSettings.getCustomBotSkin());
            skinChanged = true;
        }
        runtime.settings.setLinkNameAndSkin(newSettings.isLinkNameAndSkin());

        // Re-equip with new settings
        equipBot(bot, runtime.settings);
        syncHands(bot, runtime);

        // If appearance changed, schedule delayed reapply to prevent naked bot after skin/name load
        if (nameChanged || skinChanged) {
            // Early 3-second refresh to catch equipment loss from name/skin changes
            Bukkit.getScheduler().runTaskLater(plugin, () -> reapplySpawnState(owner.getUniqueId()), 60L);
            
            // 5-second refresh for additional safety
            Bukkit.getScheduler().runTaskLater(plugin, () -> reapplySpawnState(owner.getUniqueId()), 100L);
            
            // Also schedule shield refresh if shield is enabled
            if (runtime.settings.isShieldEnabled()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> refreshShieldAfterSpawn(owner.getUniqueId()), 120L);
            }
        }

        owner.sendMessage(Component.text("Bot settings updated instantly.", NamedTextColor.GREEN));
    }

    public void shutdown() {
        aiTask.cancel();
        for (UUID ownerId : botByOwner.keySet().toArray(UUID[]::new)) {
            despawnForOwner(ownerId);
        }
        botByOwner.clear();
        runtimeByEntity.clear();
    }

    private void tickAi() {
        for (Map.Entry<UUID, NPC> entry : new HashMap<>(botByOwner).entrySet()) {
            UUID ownerId = entry.getKey();
            NPC npc = entry.getValue();

            Player owner = Bukkit.getPlayer(ownerId);
            if (owner == null || !owner.isOnline()) {
                despawnForOwner(ownerId);
                continue;
            }

            if (!npc.isSpawned() || !(npc.getEntity() instanceof LivingEntity bot)) {
                continue;
            }

            if (bot == null || !bot.isValid() || bot.isDead()) {
                despawnForOwner(ownerId);
                continue;
            }

            BotRuntime runtime = runtimeByEntity.get(bot.getUniqueId());
            if (runtime == null) {
                continue;
            }

            if (!owner.getWorld().equals(bot.getWorld()) || owner.getLocation().distanceSquared(bot.getLocation()) > AUTO_DESPAWN_DISTANCE_SQ) {
                despawnForOwner(ownerId);
                owner.sendActionBar(Component.text("Bot despawned (100+ blocks away)", NamedTextColor.GRAY));
                continue;
            }

            TurnPreset preset = runtime.settings.getTurnPreset();
            if (preset.isTrackingEnabled()) {
                Player closest = null;
                double closestDist = Double.MAX_VALUE;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getWorld().equals(bot.getWorld())) {
                        double dist = p.getEyeLocation().distanceSquared(bot.getEyeLocation());
                        if (dist < closestDist && dist < 400.0) {
                            closest = p;
                            closestDist = dist;
                        }
                    }
                }

                if (closest != null) {
                    Vector toTarget = closest.getEyeLocation().toVector().subtract(bot.getEyeLocation().toVector());
                    double x = toTarget.getX();
                    double y = toTarget.getY();
                    double z = toTarget.getZ();
                    double horizontal = Math.sqrt((x * x) + (z * z));
                    if (horizontal > 0.05) {
                        float targetYaw = (float) Math.toDegrees(Math.atan2(-x, z));
                        float targetPitch = (float) Math.toDegrees(Math.atan2(-y, horizontal));
                        targetPitch = Math.max(-55.0f, Math.min(55.0f, targetPitch));

                        if (!runtime.rotationInitialized) {
                            runtime.lastYaw = bot.getLocation().getYaw();
                            runtime.lastPitch = bot.getLocation().getPitch();
                            runtime.rotationInitialized = true;
                        }

                        float nextYaw;
                        float nextPitch;
                        if (preset.isInstantSnap()) {
                            nextYaw = targetYaw;
                            nextPitch = targetPitch;
                        } else {
                            nextYaw = approachAngle(runtime.lastYaw, targetYaw, preset.getMaxYawStep());
                            nextPitch = approachLinear(runtime.lastPitch, targetPitch, preset.getMaxPitchStep());
                        }
                        bot.setRotation(nextYaw, nextPitch);
                        runtime.lastYaw = nextYaw;
                        runtime.lastPitch = nextPitch;
                    }
                }
            }

            // Follow player movement (only if not recently damaged to preserve knockback)
            if (runtime.settings.isFollowPlayerEnabled()) {
                long timeSinceLastDamage = System.currentTimeMillis() - runtime.lastDamagedTime;
                if (timeSinceLastDamage > 1000L) { // Wait 1 second after damage
                    double distanceToOwner = bot.getLocation().distanceSquared(owner.getLocation());
                    if (distanceToOwner > 9.0 && distanceToOwner < AUTO_DESPAWN_DISTANCE_SQ) { // 3+ blocks away
                        Vector direction = owner.getLocation().toVector().subtract(bot.getLocation().toVector());
                        double horizontalDist = Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
                        if (horizontalDist > 0.1) {
                            direction.normalize().multiply(0.2); // Move speed
                            direction.setY(0); // Don't fly up/down, just walk
                            Location newLoc = bot.getLocation().add(direction);
                            newLoc.setYaw(bot.getLocation().getYaw());
                            newLoc.setPitch(bot.getLocation().getPitch());
                            bot.teleport(newLoc);
                        }
                    }
                }
            }
            // Force full equipment sync during first spawn window to prevent skin-load armor glitches.
            if (runtime.ticksSpawned <= 30) {
                equipBot(bot, runtime.settings);
            } else {
                syncHands(bot, runtime);
            }
            runtime.ticksSpawned++;

            maintainSaturation(bot);
            maintainMovementEffects(bot, runtime);
            updateShieldUseVisual(bot, runtime);
        }
    }

    private void reapplySpawnState(UUID ownerId) {
        NPC npc = botByOwner.get(ownerId);
        if (npc == null || !npc.isSpawned() || !(npc.getEntity() instanceof LivingEntity bot)) {
            return;
        }
        BotRuntime runtime = runtimeByEntity.get(bot.getUniqueId());
        if (runtime == null) {
            return;
        }

        applySkin(npc, runtime.settings.getCustomBotSkin());
        npc.setName(runtime.settings.getCustomBotName());
        equipBot(bot, runtime.settings);
        syncHands(bot, runtime);
        
        // Ensure health is full
        if (bot.getHealth() < bot.getMaxHealth()) {
            bot.setHealth(bot.getMaxHealth());
        }
        
        // Reapply movement effects
        maintainMovementEffects(bot, runtime);

        runtime.shieldWarmupTicks = 0;
        runtime.shieldRetryTicks = 0;
        runtime.shieldVisualActive = false;
        if (bot instanceof Player playerBot) {
            setUsingMainHandVisual(playerBot, false);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!bot.isValid() || bot.isDead()) {
                return;
            }
            updateShieldUseVisual(bot, runtime);
        }, 3L);
    }

    private void refreshShieldAfterSpawn(UUID ownerId) {
        NPC npc = botByOwner.get(ownerId);
        if (npc == null || !npc.isSpawned() || !(npc.getEntity() instanceof LivingEntity bot)) {
            return;
        }
        BotRuntime runtime = runtimeByEntity.get(bot.getUniqueId());
        if (runtime == null || !runtime.settings.isShieldEnabled()) {
            return;
        }

        EntityEquipment equipment = bot.getEquipment();
        if (equipment == null) {
            return;
        }

        equipment.setItemInMainHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
        if (!runtime.settings.isTotemLimitEnabled() || runtime.remainingTotemPops > 0) {
            equipment.setItemInOffHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
        }

        runtime.shieldWarmupTicks = 0;
        runtime.shieldRetryTicks = 0;
        runtime.shieldVisualActive = false;
        if (bot instanceof Player playerBot) {
            setUsingMainHandVisual(playerBot, false);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!bot.isValid() || bot.isDead()) {
                return;
            }
            BotRuntime currentRuntime = runtimeByEntity.get(bot.getUniqueId());
            if (currentRuntime == null || !currentRuntime.settings.isShieldEnabled()) {
                return;
            }

            syncHands(bot, currentRuntime);
            currentRuntime.shieldWarmupTicks = 0;
            currentRuntime.shieldRetryTicks = 0;
            currentRuntime.shieldVisualActive = false;
            updateShieldUseVisual(bot, currentRuntime);
        }, 2L);
    }

    private void syncHands(LivingEntity bot, BotRuntime runtime) {
        EntityEquipment equipment = bot.getEquipment();
        if (equipment == null) {
            return;
        }

        boolean infinite = !runtime.settings.isTotemLimitEnabled();
        int remaining = runtime.remainingTotemPops;

        if (runtime.settings.isShieldEnabled()) {
            equipment.setItemInMainHand(unbreakableShield());
            if (infinite || remaining > 0) {
                equipment.setItemInOffHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
            } else {
                equipment.setItemInOffHand(new ItemStack(Material.AIR));
            }
        } else {
            if (infinite) {
                equipment.setItemInMainHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
                equipment.setItemInOffHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
            } else if (remaining >= 2) {
                equipment.setItemInMainHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
                equipment.setItemInOffHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
            } else if (remaining == 1) {
                equipment.setItemInMainHand(new ItemStack(Material.AIR));
                equipment.setItemInOffHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
            } else {
                equipment.setItemInMainHand(new ItemStack(Material.AIR));
                equipment.setItemInOffHand(new ItemStack(Material.AIR));
            }
        }
    }

    private void maintainMovementEffects(LivingEntity bot, BotRuntime runtime) {
        if (runtime.settings.isSlowFallingEnabled()) {
            PotionEffect current = bot.getPotionEffect(PotionEffectType.SLOW_FALLING);
            if (current == null || current.getDuration() < 80) {
                bot.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 12, 0, true, false, true));
            }
        } else {
            if (bot.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                bot.removePotionEffect(PotionEffectType.SLOW_FALLING);
            }
        }
    }

    private void updateShieldUseVisual(LivingEntity bot, BotRuntime runtime) {
        if (!(bot instanceof Player playerBot)) {
            return;
        }

        EntityEquipment equipment = bot.getEquipment();
        long now = System.currentTimeMillis();
        boolean shieldCooldownActive = now < runtime.shieldDisabledUntil;
        boolean shouldUseShield = runtime.settings.isShieldEnabled()
            && equipment != null
            && equipment.getItemInMainHand().getType() == Material.SHIELD
            && !shieldCooldownActive;

        if (shouldUseShield) {
            if (!runtime.shieldVisualActive) {
                runtime.shieldWarmupTicks++;
                if (runtime.shieldWarmupTicks < SHIELD_VISUAL_WARMUP_TICKS) {
                    return;
                }
            }
        } else {
            runtime.shieldWarmupTicks = 0;
            runtime.shieldRetryTicks = 0;
            if (runtime.shieldVisualActive) {
                setUsingMainHandVisual(playerBot, false);
                runtime.shieldVisualActive = false;
                return;
            }
        }

        if (runtime.shieldVisualActive == shouldUseShield) {
            return;
        }

        boolean applied = setUsingMainHandVisual(playerBot, shouldUseShield);
        if (applied) {
            runtime.shieldVisualActive = shouldUseShield;
            if (!shouldUseShield) {
                runtime.shieldWarmupTicks = 0;
                runtime.shieldRetryTicks = 0;
            }
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity bot)) {
            return;
        }
        BotRuntime runtime = runtimeByEntity.get(bot.getUniqueId());
        if (runtime == null) {
            return;
        }

        // Track damage time for knockback preservation
        runtime.lastDamagedTime = System.currentTimeMillis();

        if (!runtime.settings.isShieldEnabled()) {
            return;
        }

        EntityEquipment equipment = bot.getEquipment();
        if (equipment == null || equipment.getItemInMainHand().getType() != Material.SHIELD) {
            return;
        }

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        // Check if shield is actually not on cooldown
        long now = System.currentTimeMillis();
        boolean shieldCooldownActive = now < runtime.shieldDisabledUntil;
        if (shieldCooldownActive) {
            return; // Shield is disabled, allow damage
        }

        Material attackerMainhand = attacker.getInventory().getItemInMainHand().getType();

        // Block all damage when shield is active and not on cooldown
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!bot.isValid() || bot.isDead()) {
                return;
            }
            bot.setVelocity(new Vector(0.0, 0.0, 0.0));
        });

        // Axe disables shield; other attacks just bounce off
        if (attackerMainhand.name().endsWith("_AXE")) {
            runtime.shieldDisabledUntil = System.currentTimeMillis() + SHIELD_AXE_DISABLE_MS;
            runtime.shieldWarmupTicks = 0;
            runtime.shieldRetryTicks = 0;

            if (bot instanceof Player playerBot) {
                playerBot.setCooldown(Material.SHIELD, (int) (SHIELD_AXE_DISABLE_MS / 50L));
                setUsingMainHandVisual(playerBot, false);
                runtime.shieldVisualActive = false;
            }
            bot.getWorld().playSound(bot.getLocation(), Sound.ITEM_SHIELD_BREAK, 0.8f, 0.5f);
        }
    }

    private float approachLinear(float current, float target, float step) {
        if (current < target) {
            return Math.min(current + step, target);
        }
        return Math.max(current - step, target);
    }

    private float approachAngle(float current, float target, float step) {
        float delta = wrapDegrees(target - current);
        if (delta > step) {
            delta = step;
        } else if (delta < -step) {
            delta = -step;
        }
        return current + delta;
    }

    private float wrapDegrees(float value) {
        float wrapped = value % 360.0f;
        if (wrapped >= 180.0f) {
            wrapped -= 360.0f;
        }
        if (wrapped < -180.0f) {
            wrapped += 360.0f;
        }
        return wrapped;
    }

    private boolean setUsingMainHandVisual(Player player, boolean active) {
        try {
            Object craftPlayer = player;
            Method getHandle = craftPlayer.getClass().getMethod("getHandle");
            Object serverPlayer = getHandle.invoke(craftPlayer);

            Class<?> interactionHandClass = Class.forName("net.minecraft.world.InteractionHand");
            Object mainHand = null;
            for (Object constant : interactionHandClass.getEnumConstants()) {
                if (String.valueOf(constant).equals("MAIN_HAND")) {
                    mainHand = constant;
                    break;
                }
            }
            if (mainHand == null) {
                return false;
            }

            if (active) {
                Method startUsingItem = serverPlayer.getClass().getMethod("startUsingItem", interactionHandClass);
                startUsingItem.invoke(serverPlayer, mainHand);
            } else {
                Method stopUsingItem;
                try {
                    stopUsingItem = serverPlayer.getClass().getMethod("stopUsingItem");
                } catch (NoSuchMethodException ignored) {
                    stopUsingItem = serverPlayer.getClass().getMethod("releaseUsingItem");
                }
                stopUsingItem.invoke(serverPlayer);
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void applySkin(NPC npc, String skinName) {
        try {
            Class skinTraitClass = Class.forName("net.citizensnpcs.trait.SkinTrait");
            Object skinTrait = npc.getOrAddTrait(skinTraitClass);

            try {
                Method setSkinName = skinTraitClass.getMethod("setSkinName", String.class, boolean.class);
                setSkinName.invoke(skinTrait, skinName, true);
                return;
            } catch (NoSuchMethodException ignored) {
            }

            Method setSkinName = skinTraitClass.getMethod("setSkinName", String.class);
            setSkinName.invoke(skinTrait, skinName);
        } catch (Throwable ignored) {
        }
    }

    private void maintainSaturation(LivingEntity bot) {
        if (!(bot instanceof Player playerBot)) {
            return;
        }

        playerBot.setFoodLevel(20);
        playerBot.setSaturation(20.0f);
        playerBot.setExhaustion(0.0f);
    }

    private void equipBot(LivingEntity bot, BotSettings settings) {
        EntityEquipment equipment = bot.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack helmet;
        ItemStack chest;
        ItemStack legs;
        ItemStack boots;

        if (settings.isCustomModeEnabled()) {
            Material helmetMat = settings.getCustomHelmetType().getHelmet();
            Material chestMat = settings.getCustomChestType().getChest();
            Material legsMat = settings.getCustomLegsType().getLegs();
            Material bootsMat = settings.getCustomBootsType().getBoots();

            helmet = (helmetMat == Material.AIR) ? new ItemStack(Material.AIR) : unbreakableArmor(helmetMat, settings.getCustomHelmetProtectionType().getEnchantment(), 4, settings.isCustomThornsEnabled());
            chest = (chestMat == Material.AIR) ? new ItemStack(Material.AIR) : unbreakableArmor(chestMat, settings.getCustomChestProtectionType().getEnchantment(), 4, settings.isCustomThornsEnabled());
            legs = (legsMat == Material.AIR) ? new ItemStack(Material.AIR) : unbreakableArmor(legsMat, settings.getCustomLegsProtectionType().getEnchantment(), 4, settings.isCustomThornsEnabled());
            boots = (bootsMat == Material.AIR) ? new ItemStack(Material.AIR) : unbreakableArmor(bootsMat, settings.getCustomBootsProtectionType().getEnchantment(), 4, settings.isCustomThornsEnabled());
        } else {
            ArmorType armorType = settings.getArmorType();
            Material helmetMat = armorType.getHelmet();
            Material chestMat = armorType.getChest();
            Material legsMat = armorType.getLegs();
            Material bootsMat = armorType.getBoots();

            helmet = (helmetMat == Material.AIR) ? new ItemStack(Material.AIR) : unbreakableArmor(helmetMat, Enchantment.PROTECTION, 4);
            chest = (chestMat == Material.AIR) ? new ItemStack(Material.AIR) : unbreakableArmor(chestMat, Enchantment.PROTECTION, 4);
            legs = (legsMat == Material.AIR) ? new ItemStack(Material.AIR) : unbreakableArmor(legsMat, Enchantment.BLAST_PROTECTION, 4);
            boots = (bootsMat == Material.AIR) ? new ItemStack(Material.AIR) : 
                (settings.isDoubleBlastProtection()
                    ? unbreakableArmor(bootsMat, Enchantment.BLAST_PROTECTION, 4)
                    : unbreakableArmor(bootsMat, Enchantment.PROTECTION, 4));
        }

        equipment.setHelmet(helmet);
        equipment.setChestplate(chest);
        equipment.setLeggings(legs);
        equipment.setBoots(boots);

        if (settings.isShieldEnabled()) {
            equipment.setItemInMainHand(unbreakableShield());
            equipment.setItemInOffHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
        } else {
            equipment.setItemInMainHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
            equipment.setItemInOffHand(unbreakableItem(Material.TOTEM_OF_UNDYING));
        }

        // Only set drop chances for non-Player entities (Citizens NPCs are Players)
        if (!(bot instanceof Player)) {
            equipment.setHelmetDropChance(0.0f);
            equipment.setChestplateDropChance(0.0f);
            equipment.setLeggingsDropChance(0.0f);
            equipment.setBootsDropChance(0.0f);
            equipment.setItemInMainHandDropChance(0.0f);
            equipment.setItemInOffHandDropChance(0.0f);
        }
    }

    private ItemStack unbreakableArmor(Material material, Enchantment enchantment, int level) {
        return unbreakableArmor(material, enchantment, level, false);
    }

    private ItemStack unbreakableArmor(Material material, Enchantment enchantment, int level, boolean thorns) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        if (level > 0) {
            item.addUnsafeEnchantment(enchantment, level);
        }
        if (thorns) {
            item.addUnsafeEnchantment(Enchantment.THORNS, 3);
        }
        return item;
    }

    private ItemStack unbreakableItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack unbreakableShield() {
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta meta = shield.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
            shield.setItemMeta(meta);
        }
        shield.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        shield.addUnsafeEnchantment(Enchantment.MENDING, 1);
        return shield;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity bot)) {
            return;
        }
        BotRuntime runtime = runtimeByEntity.get(bot.getUniqueId());
        if (runtime == null) {
            return;
        }

        // Track damage time for knockback preservation
        runtime.lastDamagedTime = System.currentTimeMillis();

        // Do not cancel normal damage flow; keep vanilla physics/knockback.
        // Fallback anti-death guard only when shield mode is off and no totem is present.
        if (!runtime.settings.isShieldEnabled()) {
            double remaining = bot.getHealth() - event.getFinalDamage();
            if (remaining <= 0.0) {
                EntityEquipment equipment = bot.getEquipment();
                boolean hasTotem = equipment != null && (
                    equipment.getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                        || equipment.getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING
                );
                if (!hasTotem && !runtime.settings.isTotemLimitEnabled()) {
                    event.setCancelled(true);
                    bot.setHealth(1.0);
                    applyTotemPopEffects(bot);
                }
            }
        }
    }

    private void applyTotemPopEffects(LivingEntity bot) {
        bot.playEffect(EntityEffect.TOTEM_RESURRECT);
        bot.getWorld().playSound(bot.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        bot.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 45 * 20, 1, true, true, true));
        bot.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 5 * 20, 1, true, true, true));
        bot.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40 * 20, 0, true, true, true));
    }

    @EventHandler
    public void onResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof LivingEntity bot)) {
            return;
        }
        BotRuntime runtime = runtimeByEntity.get(bot.getUniqueId());
        if (runtime == null) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (runtime.settings.isTotemLimitEnabled() && runtime.remainingTotemPops > 0) {
            runtime.remainingTotemPops--;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!bot.isValid() || bot.isDead()) {
                return;
            }
            syncHands(bot, runtime);
        });
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        UUID botId = event.getEntity().getUniqueId();
        BotRuntime runtime = runtimeByEntity.remove(botId);
        if (runtime == null) {
            return;
        }

        event.getDrops().clear();
        event.setDroppedExp(0);

        NPC npc = botByOwner.remove(runtime.ownerId);
        if (npc != null) {
            npc.destroy();
        }
    }

    @EventHandler
    public void onOwnerQuit(PlayerQuitEvent event) {
        despawnForOwner(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onOwnerKick(PlayerKickEvent event) {
        despawnForOwner(event.getPlayer().getUniqueId());
    }

    private void despawnForOwner(UUID ownerId) {
        NPC npc = botByOwner.remove(ownerId);
        if (npc == null) {
            return;
        }

        if (npc.isSpawned() && npc.getEntity() != null) {
            runtimeByEntity.remove(npc.getEntity().getUniqueId());
        }

        npc.destroy();
    }

    private static final class BotRuntime {
        private final UUID ownerId;
        private final BotSettings settings;
        private int remainingTotemPops;
        private int ticksSpawned = 0;
        private boolean shieldVisualActive = false;
        private int shieldWarmupTicks = 0;
        private int shieldRetryTicks = 0;
        private long shieldDisabledUntil = 0L;
        private long lastDamagedTime = 0L;
        private boolean rotationInitialized = false;
        private float lastYaw = 0.0f;
        private float lastPitch = 0.0f;

        private BotRuntime(UUID ownerId, BotSettings settings) {
            this.ownerId = ownerId;
            this.settings = settings;
            this.remainingTotemPops = settings.isTotemLimitEnabled() ? settings.getTotemLimit() : -1;
        }
    }
}
