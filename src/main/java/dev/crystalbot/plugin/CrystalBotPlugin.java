package dev.crystalbot.plugin;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrystalBotPlugin extends JavaPlugin {
    private BotManager botManager;
    private BotMenuListener botMenuListener;

    @Override
    public void onEnable() {
        this.botManager = new BotManager(this);
        this.botMenuListener = new BotMenuListener(this, botManager);

        Bukkit.getPluginManager().registerEvents(botMenuListener, this);
        Bukkit.getPluginManager().registerEvents(botManager, this);

        PluginCommand botCommand = getCommand("bot");
        if (botCommand != null) {
            botCommand.setExecutor(new BotCommand(botMenuListener));
        } else {
            getLogger().severe("Command /bot is not defined in plugin.yml");
        }
    }

    @Override
    public void onDisable() {
        if (botManager != null) {
            botManager.shutdown();
        }
    }

    public NamespacedKey key(String path) {
        return new NamespacedKey(this, path);
    }
}
