package me.hexett.staffUtilsPlus.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Configuration manager for plugin messages.
 * Handles loading, caching, and retrieving messages from the messages.yml file.
 * 
 * @author Hexett
 */
public class MessagesConfig {

    private static FileConfiguration messages;
    private static File file;
    private static String prefix = "";
    private static boolean initialized = false;

    /**
     * Load the messages configuration file.
     * 
     * @param plugin The plugin instance
     */
    public static void load(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(file);

        // Merge defaults from JAR if new keys are missing
        try (InputStream defStream = plugin.getResource("messages.yml")) {
            if (defStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
                messages.setDefaults(defConfig);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load default messages: " + e.getMessage());
        }

        prefix = color(messages.getString("prefix", "&7"));
        initialized = true;
    }

    /**
     * Get a message by path, with prefix replacement.
     * 
     * @param path The message path
     * @return The formatted message
     */
    public static String get(String path) {
        if (!initialized) {
            return path != null ? path : "";
        }
        
        if (path == null) {
            return "";
        }
        
        String message = messages.getString(path, path);
        return color(message.replace("%prefix%", prefix));
    }

    /**
     * Get a list of messages by path, with prefix replacement.
     * 
     * @param path The message path
     * @return The formatted message list
     */
    public static List<String> getList(String path) {
        if (!initialized) {
            return List.of();
        }
        
        if (path == null) {
            return List.of();
        }
        
        List<String> list = messages.getStringList(path);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, color(list.get(i).replace("%prefix%", prefix)));
        }
        return list;
    }

    /**
     * Reload the messages configuration.
     * 
     * @param plugin The plugin instance
     */
    public static void reload(Plugin plugin) {
        load(plugin);
    }

    /**
     * Check if the messages configuration has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Apply color codes to a message.
     * 
     * @param msg The message to colorize
     * @return The colorized message
     */
    private static String color(String msg) {
        if (msg == null) {
            return "";
        }
        return ColorUtils.translateColorCodes(msg);
    }
}
