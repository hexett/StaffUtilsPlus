package me.hexett.staffUtilsPlus.commands;

import me.hexett.staffUtilsPlus.utils.ColorUtils;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Base command class that provides common functionality for all commands.
 * 
 * @author Hexett
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final String permission;
    protected final String usage;
    protected final String description;
    protected final boolean playerOnly;
    protected final int minArgs;

    /**
     * Create a new base command.
     * 
     * @param permission The permission required to use this command
     * @param usage The usage message for this command
     * @param description The description of this command
     * @param playerOnly Whether this command can only be used by players
     * @param minArgs The minimum number of arguments required
     */
    protected BaseCommand(String permission, String usage, String description, boolean playerOnly, int minArgs) {
        this.permission = permission;
        this.usage = usage;
        this.description = description;
        this.playerOnly = playerOnly;
        this.minArgs = minArgs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!hasPermission(sender)) {
            sendMessage(sender, MessagesConfig.get("errors.no-permission"));
            return true;
        }

        // Check if player-only command is used by console
        if (playerOnly && !(sender instanceof Player)) {
            sendMessage(sender, MessagesConfig.get("errors.player-only"));
            return true;
        }

        // Check minimum arguments
        if (args.length < minArgs) {
            sendUsage(sender);
            return true;
        }

        // Execute the command
        try {
            return execute(sender, args);
        } catch (Exception e) {
            sendMessage(sender, MessagesConfig.get("errors.command-error"));
            if (sender.hasPermission("staffutils.debug")) {
                e.printStackTrace();
            }
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!hasPermission(sender)) {
            return new ArrayList<>();
        }

        try {
            return tabComplete(sender, args);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Execute the command logic.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return true if the command was handled successfully
     */
    protected abstract boolean execute(CommandSender sender, String[] args);

    /**
     * Provide tab completion for the command.
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return List of tab completion options
     */
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    /**
     * Check if the sender has permission to use this command.
     * 
     * @param sender The command sender
     * @return true if the sender has permission
     */
    protected boolean hasPermission(CommandSender sender) {
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }

    /**
     * Send a message to the sender.
     * 
     * @param sender The command sender
     * @param message The message to send
     */
    protected void sendMessage(CommandSender sender, String message) {
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(ColorUtils.translateColorCodes(message));
        }
    }

    /**
     * Send the usage message to the sender.
     * 
     * @param sender The command sender
     */
    protected void sendUsage(CommandSender sender) {
        sendMessage(sender, MessagesConfig.get("errors.usage").replace("%usage%", usage));
    }

    /**
     * Get the permission node for this command.
     * 
     * @return The permission node
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Get the usage message for this command.
     * 
     * @return The usage message
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Get the description of this command.
     * 
     * @return The command description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if this command is player-only.
     * 
     * @return true if player-only
     */
    public boolean isPlayerOnly() {
        return playerOnly;
    }

    /**
     * Get the minimum number of arguments required.
     * 
     * @return The minimum argument count
     */
    public int getMinArgs() {
        return minArgs;
    }
}
