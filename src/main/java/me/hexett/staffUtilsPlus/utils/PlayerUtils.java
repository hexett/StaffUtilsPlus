package me.hexett.staffUtilsPlus.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Utility class for player-related operations.
 * 
 * @author Hexett
 */
public class PlayerUtils {

    /**
     * Get a player's UUID by name.
     * 
     * @param name The player's name
     * @return The player's UUID, or null if not found
     */
    public static UUID getPlayerUUID(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        // Check online players first
        Player onlinePlayer = Bukkit.getPlayer(name);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        // Check offline players
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    /**
     * Get a player's name by UUID.
     * 
     * @param uuid The player's UUID
     * @return The player's name, or "Unknown" if not found
     */
    public static String getPlayerName(UUID uuid) {
        if (uuid == null) {
            return "Console";
        }

        // Check online players first
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        // Check offline players
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "Unknown";
    }

    /**
     * Check if a player is online.
     * 
     * @param uuid The player's UUID
     * @return true if the player is online, false otherwise
     */
    public static boolean isPlayerOnline(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return Bukkit.getPlayer(uuid) != null;
    }

    /**
     * Check if a player is online.
     * 
     * @param name The player's name
     * @return true if the player is online, false otherwise
     */
    public static boolean isPlayerOnline(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return Bukkit.getPlayer(name) != null;
    }

    /**
     * Get all online player names.
     * 
     * @return Array of online player names
     */
    public static String[] getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toArray(String[]::new);
    }

    /**
     * Check if a string is a valid player name.
     * 
     * @param name The name to check
     * @return true if the name is valid, false otherwise
     */
    public static boolean isValidPlayerName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Check length (Minecraft username limits)
        if (name.length() < 3 || name.length() > 16) {
            return false;
        }
        
        // Check if it contains only valid characters
        return name.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Get the display name for a UUID (with fallback).
     * 
     * @param uuid The player's UUID
     * @param fallback The fallback name if player not found
     * @return The player's display name or fallback
     */
    public static String getDisplayName(UUID uuid, String fallback) {
        String name = getPlayerName(uuid);
        return "Unknown".equals(name) ? fallback : name;
    }
}
