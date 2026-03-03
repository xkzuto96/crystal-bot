package dev.crystalbot.plugin;

public enum TurnPreset {
    OFF("Off", 0.0f, 0.0f, false, false),
    CHEAT("Cheat", 999.0f, 999.0f, true),
    AGGRESSIVE("Aggressive", 20.0f, 12.0f),
    BALANCED("Balanced", 12.0f, 8.0f),
    SLOW("Slow", 7.0f, 5.0f);

    private final String display;
    private final float maxYawStep;
    private final float maxPitchStep;
    private final boolean instantSnap;
    private final boolean trackingEnabled;

    TurnPreset(String display, float maxYawStep, float maxPitchStep) {
        this(display, maxYawStep, maxPitchStep, false, true);
    }

    TurnPreset(String display, float maxYawStep, float maxPitchStep, boolean instantSnap) {
        this(display, maxYawStep, maxPitchStep, instantSnap, true);
    }

    TurnPreset(String display, float maxYawStep, float maxPitchStep, boolean instantSnap, boolean trackingEnabled) {
        this.display = display;
        this.maxYawStep = maxYawStep;
        this.maxPitchStep = maxPitchStep;
        this.instantSnap = instantSnap;
        this.trackingEnabled = trackingEnabled;
    }

    public String getDisplay() {
        return display;
    }

    public float getMaxYawStep() {
        return maxYawStep;
    }

    public float getMaxPitchStep() {
        return maxPitchStep;
    }

    public boolean isInstantSnap() {
        return instantSnap;
    }

    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }

    public TurnPreset next() {
        TurnPreset[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public TurnPreset nextCycle() {
        return switch (this) {
            case CHEAT -> AGGRESSIVE;
            case AGGRESSIVE -> BALANCED;
            case BALANCED -> SLOW;
            case SLOW, OFF -> CHEAT;
        };
    }
}