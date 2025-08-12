package me.hexett.staffUtilsPlus.service.punishments;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing player punishments.
 * Provides methods for banning, muting, and kicking players.
 * 
 * @author Hexett
 */
public interface PunishmentService {

    /**
     * Ban a player permanently or temporarily.
     * 
     * @param issuer The UUID of the player issuing the ban, or null for console
     * @param target The UUID of the player to ban
     * @param reason The reason for the ban
     * @param expiresAt When the ban expires (timestamp), or -1 for permanent
     */
    void ban(UUID issuer, UUID target, String reason, long expiresAt);
    
    /**
     * Unban a previously banned player.
     * 
     * @param issuer The UUID of the player issuing the unban, or null for console
     * @param target The UUID of the player to unban
     */
    void unban(UUID issuer, UUID target);
    
    /**
     * Check if a player is currently banned.
     * 
     * @param target The UUID of the player to check
     * @return true if the player is banned, false otherwise
     */
    boolean isBanned(UUID target);
    
    /**
     * Get the active punishment of a specific type for a player.
     * 
     * @param target The UUID of the player
     * @param type The type of punishment to check
     * @return Optional containing the active punishment, or empty if none found
     */
    Optional<Punishment> getActivePunishment(UUID target, Punishment.Type type);
    
    /**
     * Mute a player permanently or temporarily.
     * 
     * @param issuer The UUID of the player issuing the mute, or null for console
     * @param target The UUID of the player to mute
     * @param reason The reason for the mute
     * @param expiresAt When the mute expires (timestamp), or -1 for permanent
     */
    void mute(UUID issuer, UUID target, String reason, long expiresAt);
    
    /**
     * Unmute a previously muted player.
     * 
     * @param issuer The UUID of the player issuing the unmute, or null for console
     * @param target The UUID of the player to unmute
     */
    void unmute(UUID issuer, UUID target);
    
    /**
     * Check if a player is currently muted.
     * 
     * @param target The UUID of the player to check
     * @return true if the player is muted, false otherwise
     */
    boolean isMuted(UUID target);

    /**
     * IP ban a player permanently or temporarily.
     * 
     * @param issuer The UUID of the player issuing the IP ban, or null for console
     * @param target The UUID of the player to IP ban
     * @param ipAddress The IP address to ban
     * @param reason The reason for the IP ban
     * @param expiresAt When the IP ban expires (timestamp), or -1 for permanent
     */
    void ipBan(UUID issuer, UUID target, String ipAddress, String reason, long expiresAt);
    
    /**
     * Unban an IP address.
     * 
     * @param issuer The UUID of the player issuing the unban, or null for console
     * @param ipAddress The IP address to unban
     */
    void unbanIP(UUID issuer, String ipAddress);
    
    /**
     * Check if an IP address is currently banned.
     * 
     * @param ipAddress The IP address to check
     * @return true if the IP address is banned, false otherwise
     */
    boolean isIPBanned(String ipAddress);
    
    /**
     * Get the active IP ban for an IP address.
     * 
     * @param ipAddress The IP address to check
     * @return Optional containing the active IP ban, or empty if not banned
     */
    Optional<Punishment> getActiveIPBan(String ipAddress);

    List<Punishment> getActiveTempBans();
}
