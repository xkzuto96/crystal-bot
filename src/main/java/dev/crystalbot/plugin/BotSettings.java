package dev.crystalbot.plugin;

public final class BotSettings {
    private ArmorType armorType = ArmorType.NETHERITE;
    private boolean doubleBlastProtection;
    private boolean shieldEnabled;
    private boolean slowFallingEnabled;
    private boolean totemLimitEnabled;
    private int totemLimit = 10;
    private TurnPreset turnPreset = TurnPreset.CHEAT;
    private boolean customModeEnabled;
    private ArmorType customHelmetType = ArmorType.NETHERITE;
    private ArmorType customChestType = ArmorType.NETHERITE;
    private ArmorType customLegsType = ArmorType.NETHERITE;
    private ArmorType customBootsType = ArmorType.NETHERITE;
    private CustomProtectionType customSetProtectionType = CustomProtectionType.PROTECTION;
    private CustomProtectionType customHelmetProtectionType = CustomProtectionType.PROTECTION;
    private CustomProtectionType customChestProtectionType = CustomProtectionType.PROTECTION;
    private CustomProtectionType customLegsProtectionType = CustomProtectionType.PROTECTION;
    private CustomProtectionType customBootsProtectionType = CustomProtectionType.PROTECTION;
    private boolean customThornsEnabled;
    private String customBotName = "PracticeBot";
    private String customBotSkin = "xkzuto";
    private boolean linkNameAndSkin = false;
    private boolean followPlayerEnabled = false;

    public ArmorType getArmorType() {
        return armorType;
    }

    public void setArmorType(ArmorType armorType) {
        this.armorType = armorType;
    }

    public void toggleArmorType() {
        this.armorType = this.armorType.toggle();
    }

    public boolean isDoubleBlastProtection() {
        return doubleBlastProtection;
    }

    public void setDoubleBlastProtection(boolean doubleBlastProtection) {
        this.doubleBlastProtection = doubleBlastProtection;
    }

    public void toggleDoubleBlastProtection() {
        this.doubleBlastProtection = !this.doubleBlastProtection;
    }

    public boolean isShieldEnabled() {
        return shieldEnabled;
    }

    public void setShieldEnabled(boolean shieldEnabled) {
        this.shieldEnabled = shieldEnabled;
    }

    public void toggleShieldEnabled() {
        this.shieldEnabled = !this.shieldEnabled;
    }

    public boolean isSlowFallingEnabled() {
        return slowFallingEnabled;
    }

    public void setSlowFallingEnabled(boolean slowFallingEnabled) {
        this.slowFallingEnabled = slowFallingEnabled;
    }

    public void toggleSlowFallingEnabled() {
        this.slowFallingEnabled = !this.slowFallingEnabled;
    }

    public boolean isTotemLimitEnabled() {
        return totemLimitEnabled;
    }

    public void setTotemLimitEnabled(boolean totemLimitEnabled) {
        this.totemLimitEnabled = totemLimitEnabled;
    }

    public void toggleTotemLimitEnabled() {
        this.totemLimitEnabled = !this.totemLimitEnabled;
    }

    public int getTotemLimit() {
        return totemLimit;
    }

    public void setTotemLimit(int totemLimit) {
        this.totemLimit = Math.max(1, totemLimit);
    }

    public TurnPreset getTurnPreset() {
        return turnPreset;
    }

    public void setTurnPreset(TurnPreset turnPreset) {
        this.turnPreset = turnPreset;
    }

    public void cycleTurnPreset() {
        this.turnPreset = this.turnPreset.nextCycle();
    }

    public boolean isCustomModeEnabled() {
        return customModeEnabled;
    }

    public void setCustomModeEnabled(boolean customModeEnabled) {
        this.customModeEnabled = customModeEnabled;
    }

    public void toggleCustomModeEnabled() {
        this.customModeEnabled = !this.customModeEnabled;
    }

    public ArmorType getCustomHelmetType() {
        return customHelmetType;
    }

    public void setCustomHelmetType(ArmorType customHelmetType) {
        this.customHelmetType = customHelmetType;
    }

    public ArmorType getCustomChestType() {
        return customChestType;
    }

    public void setCustomChestType(ArmorType customChestType) {
        this.customChestType = customChestType;
    }

    public ArmorType getCustomLegsType() {
        return customLegsType;
    }

    public void setCustomLegsType(ArmorType customLegsType) {
        this.customLegsType = customLegsType;
    }

    public ArmorType getCustomBootsType() {
        return customBootsType;
    }

    public void setCustomBootsType(ArmorType customBootsType) {
        this.customBootsType = customBootsType;
    }

    public CustomProtectionType getCustomSetProtectionType() {
        return customSetProtectionType;
    }

    public void setCustomSetProtectionType(CustomProtectionType customSetProtectionType) {
        this.customSetProtectionType = customSetProtectionType;
    }

    public void cycleCustomSetProtectionType() {
        CustomProtectionType next = this.customSetProtectionType.nextCycle();
        this.customSetProtectionType = next;
        this.customHelmetProtectionType = next;
        this.customChestProtectionType = next;
        this.customLegsProtectionType = next;
        this.customBootsProtectionType = next;
    }

    public CustomProtectionType getCustomHelmetProtectionType() {
        return customHelmetProtectionType;
    }

    public void setCustomHelmetProtectionType(CustomProtectionType customHelmetProtectionType) {
        this.customHelmetProtectionType = customHelmetProtectionType;
    }

    public void cycleCustomHelmetProtectionType() {
        this.customHelmetProtectionType = this.customHelmetProtectionType.nextCycle();
    }

    public CustomProtectionType getCustomChestProtectionType() {
        return customChestProtectionType;
    }

    public void setCustomChestProtectionType(CustomProtectionType customChestProtectionType) {
        this.customChestProtectionType = customChestProtectionType;
    }

    public void cycleCustomChestProtectionType() {
        this.customChestProtectionType = this.customChestProtectionType.nextCycle();
    }

    public CustomProtectionType getCustomLegsProtectionType() {
        return customLegsProtectionType;
    }

    public void setCustomLegsProtectionType(CustomProtectionType customLegsProtectionType) {
        this.customLegsProtectionType = customLegsProtectionType;
    }

    public void cycleCustomLegsProtectionType() {
        this.customLegsProtectionType = this.customLegsProtectionType.nextCycle();
    }

    public CustomProtectionType getCustomBootsProtectionType() {
        return customBootsProtectionType;
    }

    public void setCustomBootsProtectionType(CustomProtectionType customBootsProtectionType) {
        this.customBootsProtectionType = customBootsProtectionType;
    }

    public void cycleCustomBootsProtectionType() {
        this.customBootsProtectionType = this.customBootsProtectionType.nextCycle();
    }

    public boolean isCustomThornsEnabled() {
        return customThornsEnabled;
    }

    public void setCustomThornsEnabled(boolean customThornsEnabled) {
        this.customThornsEnabled = customThornsEnabled;
    }

    public void toggleCustomThornsEnabled() {
        this.customThornsEnabled = !this.customThornsEnabled;
    }

    public String getCustomBotName() {
        return customBotName;
    }

    public void setCustomBotName(String customBotName) {
        this.customBotName = customBotName != null && !customBotName.trim().isEmpty() ? customBotName : "PracticeBot";
        if (linkNameAndSkin) {
            this.customBotSkin = this.customBotName;
        }
    }

    public String getCustomBotSkin() {
        return customBotSkin;
    }

    public void setCustomBotSkin(String customBotSkin) {
        this.customBotSkin = customBotSkin != null && !customBotSkin.trim().isEmpty() ? customBotSkin : "xkzuto";
        if (linkNameAndSkin) {
            this.customBotName = this.customBotSkin;
        }
    }

    public boolean isLinkNameAndSkin() {
        return linkNameAndSkin;
    }

    public void setLinkNameAndSkin(boolean linkNameAndSkin) {
        this.linkNameAndSkin = linkNameAndSkin;
        if (linkNameAndSkin && !customBotName.equals(customBotSkin)) {
            this.customBotSkin = this.customBotName;
        }
    }

    public void toggleLinkNameAndSkin() {
        setLinkNameAndSkin(!this.linkNameAndSkin);
    }

    public boolean isFollowPlayerEnabled() {
        return followPlayerEnabled;
    }

    public void setFollowPlayerEnabled(boolean followPlayerEnabled) {
        this.followPlayerEnabled = followPlayerEnabled;
    }

    public void toggleFollowPlayerEnabled() {
        this.followPlayerEnabled = !this.followPlayerEnabled;
    }

    public void cycleCustomHelmet() {
        this.customHelmetType = this.customHelmetType.toggle();
    }

    public void cycleCustomChest() {
        this.customChestType = this.customChestType.toggle();
    }

    public void cycleCustomLegs() {
        this.customLegsType = this.customLegsType.toggle();
    }

    public void cycleCustomBoots() {
        this.customBootsType = this.customBootsType.toggle();
    }

    public void cycleWholeCustomSet() {
        ArmorType next = this.customHelmetType.toggle();
        this.customHelmetType = next;
        this.customChestType = next;
        this.customLegsType = next;
        this.customBootsType = next;
    }

    public void setWholeCustomSet(ArmorType type) {
        this.customHelmetType = type;
        this.customChestType = type;
        this.customLegsType = type;
        this.customBootsType = type;
    }

    public void setWholeCustomProtectionType(CustomProtectionType type) {
        this.customSetProtectionType = type;
        this.customHelmetProtectionType = type;
        this.customChestProtectionType = type;
        this.customLegsProtectionType = type;
        this.customBootsProtectionType = type;
    }

    public BotSettings copy() {
        BotSettings clone = new BotSettings();
        clone.armorType = this.armorType;
        clone.doubleBlastProtection = this.doubleBlastProtection;
        clone.shieldEnabled = this.shieldEnabled;
        clone.slowFallingEnabled = this.slowFallingEnabled;
        clone.totemLimitEnabled = this.totemLimitEnabled;
        clone.totemLimit = this.totemLimit;
        clone.turnPreset = this.turnPreset;
        clone.customModeEnabled = this.customModeEnabled;
        clone.customHelmetType = this.customHelmetType;
        clone.customChestType = this.customChestType;
        clone.customLegsType = this.customLegsType;
        clone.customBootsType = this.customBootsType;
        clone.customSetProtectionType = this.customSetProtectionType;
        clone.customHelmetProtectionType = this.customHelmetProtectionType;
        clone.customChestProtectionType = this.customChestProtectionType;
        clone.customLegsProtectionType = this.customLegsProtectionType;
        clone.customBootsProtectionType = this.customBootsProtectionType;
        clone.customThornsEnabled = this.customThornsEnabled;
        clone.customBotName = this.customBotName;
        clone.customBotSkin = this.customBotSkin;
        clone.linkNameAndSkin = this.linkNameAndSkin;
        clone.followPlayerEnabled = this.followPlayerEnabled;
        return clone;
    }
}
