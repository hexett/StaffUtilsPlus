package me.hexett.staffUtilsPlus.service.alts;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for tracking and managing alternative accounts.
 *
 * @author Hexett
 */
public interface AltAccountService {

    /**
     * Get the IP address associated with a player.
     *
     * @param uuid The player's UUID
     * @return The IP address, or null if not found
     */
    String getPlayerIP(UUID uuid);

    /**
     * Get all alternative accounts for a player (including the player themselves).
     *
     * @param uuid The player's UUID
     * @return List of UUIDs that share the same IP address
     */
    List<UUID> getAltAccounts(UUID uuid);

    /**
     * Get all players who have joined from a specific IP address.
     *
     * @param ipAddress The IP address
     * @return List of UUIDs
     */
    List<UUID> getPlayersByIP(String ipAddress);
}