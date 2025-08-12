package me.hexett.staffUtilsPlus.service.warnings;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing player warnings.
 * 
 * @author Hexett
 */
public interface WarningService {
    
    /**
     * Warn a player.
     * 
     * @param target The UUID of the target player
     * @param issuer The UUID of the staff member issuing the warning
     * @param reason The reason for the warning
     * @param severity The severity level (1-5)
     */
    void warnPlayer(UUID target, UUID issuer, String reason, int severity);
    
    /**
     * Remove a warning from a player.
     * 
     * @param target The UUID of the target player
     * @param warningId The ID of the warning to remove
     */
    void removeWarning(UUID target, int warningId);
    
    /**
     * Get all warnings for a player.
     * 
     * @param target The UUID of the target player
     * @return List of warnings for the player
     */
    List<Warning> getWarnings(UUID target);
    
    /**
     * Get the total warning level for a player.
     * 
     * @param target The UUID of the target player
     * @return The total warning level
     */
    int getWarningLevel(UUID target);
    
    /**
     * Get a specific warning by ID.
     * 
     * @param warningId The ID of the warning
     * @return The warning, or null if not found
     */
    Warning getWarning(int warningId);
    
    /**
     * Check if a player should be auto-punished based on warning level.
     * 
     * @param target The UUID of the target player
     */
    void checkAutoPunishment(UUID target);
}
