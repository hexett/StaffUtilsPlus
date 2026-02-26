package me.hexett.staffUtilsPlus.utils;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for managing player IP addresses.
 * Stores IP addresses for online players to enable IP bans.
 * 
 * @author Hexett
 */
public class IPAddressManager {
    
    private static final Map<UUID, String> playerIPs = new ConcurrentHashMap<>();
    
    /**
     * Store a player's IP address.
     * 
     * @param player The player
     */
    public static void storePlayerIP(Player player) {
        if (player.getAddress() != null) {
            String ipAddress = player.getAddress().getAddress().getHostAddress();
            playerIPs.put(player.getUniqueId(), ipAddress);
        }
    }
    
    /**
     * Remove a player's IP address when they disconnect.
     * 
     * @param playerUUID The player's UUID
     */
    public static void removePlayerIP(UUID playerUUID) {
        playerIPs.remove(playerUUID);
    }
    
    /**
     * Get a player's IP address.
     * 
     * @param playerUUID The player's UUID
     * @return The player's IP address, or null if not found
     */
    public static String getPlayerIP(UUID playerUUID) {
        return playerIPs.get(playerUUID);
    }
    
    /**
     * Check if an IP address is stored for any player.
     * 
     * @param ipAddress The IP address to check
     * @return true if the IP address is stored, false otherwise
     */
    public static boolean hasIPAddress(String ipAddress) {
        return playerIPs.containsValue(ipAddress);
    }
    
    /**
     * Get all stored IP addresses.
     * 
     * @return Map of player UUIDs to IP addresses
     */
    public static Map<UUID, String> getAllPlayerIPs() {
        return new ConcurrentHashMap<>(playerIPs);
    }
    
    /**
     * Clear all stored IP addresses.
     */
    public static void clearAllIPs() {
        playerIPs.clear();
    }
}
