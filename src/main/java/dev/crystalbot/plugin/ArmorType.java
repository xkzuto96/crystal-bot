package dev.crystalbot.plugin;

import org.bukkit.Material;

public enum ArmorType {
    NONE("No Armor", Material.AIR, Material.AIR, Material.AIR, Material.AIR),
    LEATHER("Leather", Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS),
    CHAINMAIL("Chainmail", Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS),
    IRON("Iron", Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS),
    GOLD("Gold", Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS),
    DIAMOND("Diamond", Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS),
    NETHERITE("Netherite", Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);

    private final String display;
    private final Material helmet;
    private final Material chest;
    private final Material legs;
    private final Material boots;

    ArmorType(String display, Material helmet, Material chest, Material legs, Material boots) {
        this.display = display;
        this.helmet = helmet;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
    }

    public ArmorType toggle() {
        ArmorType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public String getDisplay() {
        return display;
    }

    public Material getHelmet() {
        return helmet;
    }

    public Material getChest() {
        return chest;
    }

    public Material getLegs() {
        return legs;
    }

    public Material getBoots() {
        return boots;
    }
}
