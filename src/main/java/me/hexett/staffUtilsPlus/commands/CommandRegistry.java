package me.hexett.staffUtilsPlus.commands;

import me.hexett.staffUtilsPlus.StaffUtilsPlus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Command registry system for managing all plugin commands.
 * 
 * @author Hexett
 */
public class CommandRegistry {

    private static final Logger log = StaffUtilsPlus.getInstance().getLogger();
    private static final Map<String, BaseCommand> commands = new HashMap<>();
    private static final Map<String, String> aliases = new HashMap<>();

    /**
     * Register a command with the registry.
     * 
     * @param name The command name
     * @param command The command instance
     * @return true if registration was successful
     */
    public static boolean registerCommand(String name, BaseCommand command) {
        if (name == null || name.isEmpty() || command == null) {
            log.warning("Failed to register command: Invalid parameters");
            return false;
        }

        StaffUtilsPlus plugin = StaffUtilsPlus.getInstance();
        PluginCommand pluginCommand = plugin.getCommand(name);
        
        if (pluginCommand == null) {
            log.warning("Failed to register command '" + name + "': Command not found in plugin.yml");
            return false;
        }

        // Set the executor and tab completer
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

        // Register in our registry
        commands.put(name.toLowerCase(), command);
        
        log.info("Registered command: " + name);
        return true;
    }

    /**
     * Register a command with aliases.
     * 
     * @param name The primary command name
     * @param command The command instance
     * @param commandAliases Array of aliases for the command
     * @return true if registration was successful
     */
    public static boolean registerCommand(String name, BaseCommand command, String... commandAliases) {
        boolean success = registerCommand(name, command);
        
        if (success && commandAliases != null) {
            for (String alias : commandAliases) {
                if (alias != null && !alias.isEmpty()) {
                    aliases.put(alias.toLowerCase(), name.toLowerCase());
                    log.info("Registered alias: " + alias + " -> " + name);
                }
            }
        }
        
        return success;
    }

    /**
     * Unregister a command from the registry.
     * 
     * @param name The command name to unregister
     * @return true if unregistration was successful
     */
    public static boolean unregisterCommand(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        String lowerName = name.toLowerCase();
        BaseCommand command = commands.remove(lowerName);
        
        if (command != null) {
            // Remove aliases
            aliases.entrySet().removeIf(entry -> entry.getValue().equals(lowerName));
            
            // Unregister from Bukkit
            StaffUtilsPlus plugin = StaffUtilsPlus.getInstance();
            PluginCommand pluginCommand = plugin.getCommand(name);
            if (pluginCommand != null) {
                pluginCommand.setExecutor(null);
                pluginCommand.setTabCompleter(null);
            }
            
            log.info("Unregistered command: " + name);
            return true;
        }
        
        return false;
    }

    /**
     * Get a command by name or alias.
     * 
     * @param name The command name or alias
     * @return The command instance, or null if not found
     */
    public static BaseCommand getCommand(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        String lowerName = name.toLowerCase();
        
        // Check direct command
        BaseCommand command = commands.get(lowerName);
        if (command != null) {
            return command;
        }
        
        // Check aliases
        String aliasedName = aliases.get(lowerName);
        if (aliasedName != null) {
            return commands.get(aliasedName);
        }
        
        return null;
    }

    /**
     * Check if a command is registered.
     * 
     * @param name The command name or alias
     * @return true if the command is registered
     */
    public static boolean isCommandRegistered(String name) {
        return getCommand(name) != null;
    }

    /**
     * Get all registered commands.
     * 
     * @return Map of command names to command instances
     */
    public static Map<String, BaseCommand> getAllCommands() {
        return new HashMap<>(commands);
    }

    /**
     * Get all registered aliases.
     * 
     * @return Map of aliases to primary command names
     */
    public static Map<String, String> getAllAliases() {
        return new HashMap<>(aliases);
    }

    /**
     * Reload all commands from the registry.
     * This is useful after configuration changes.
     */
    public static void reloadCommands() {
        log.info("Reloading all commands...");
        
        // Clear current registry
        commands.clear();
        aliases.clear();
        
        // Re-register all commands
        // This will be called by individual command classes during reload
        log.info("Commands reloaded successfully");
    }

    /**
     * Get the number of registered commands.
     * 
     * @return The number of commands
     */
    public static int getCommandCount() {
        return commands.size();
    }

    /**
     * Get the number of registered aliases.
     * 
     * @return The number of aliases
     */
    public static int getAliasCount() {
        return aliases.size();
    }
}
