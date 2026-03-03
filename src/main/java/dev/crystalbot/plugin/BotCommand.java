package dev.crystalbot.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BotCommand implements CommandExecutor {
    private final BotMenuListener menuListener;

    public BotCommand(BotMenuListener menuListener) {
        this.menuListener = menuListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        menuListener.openMenu(player);
        return true;
    }
}
