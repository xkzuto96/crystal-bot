package dev.crystalbot.plugin;

import org.bukkit.enchantments.Enchantment;

public enum CustomProtectionType {
    PROTECTION("Protection", Enchantment.PROTECTION),
    BLAST("Blast Protection", Enchantment.BLAST_PROTECTION),
    PROJECTILE("Projectile Protection", Enchantment.PROJECTILE_PROTECTION),
    FIRE("Fire Protection", Enchantment.FIRE_PROTECTION);

    private final String display;
    private final Enchantment enchantment;

    CustomProtectionType(String display, Enchantment enchantment) {
        this.display = display;
        this.enchantment = enchantment;
    }

    public String getDisplay() {
        return display;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public CustomProtectionType nextCycle() {
        return values()[(ordinal() + 1) % values().length];
    }
}