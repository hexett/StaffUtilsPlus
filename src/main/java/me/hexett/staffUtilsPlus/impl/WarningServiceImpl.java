package me.hexett.staffUtilsPlus.impl;

import me.hexett.staffUtilsPlus.db.Database;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import me.hexett.staffUtilsPlus.service.warnings.Warning;
import me.hexett.staffUtilsPlus.service.warnings.WarningService;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the WarningService interface.
 * 
 * @author Hexett
 */
public class WarningServiceImpl implements WarningService {

    private final Database database;
    private final PunishmentService punishmentService;
    private final Plugin plugin;

    public WarningServiceImpl(Database database, PunishmentService punishmentService, Plugin plugin) {
        this.database = database;
        this.punishmentService = punishmentService;
        this.plugin = plugin;
    }

    @Override
    public void warnPlayer(UUID target, UUID issuer, String reason, int severity) {
        Warning warning = new Warning(target, issuer, reason, severity);
        database.insertWarning(warning);
    }

    @Override
    public void removeWarning(UUID target, int warningId) {
        database.removeWarning(target, warningId);
    }

    @Override
    public List<Warning> getWarnings(UUID target) {
        return database.getWarnings(target);
    }

    @Override
    public int getWarningLevel(UUID target) {
        List<Warning> warnings = getWarnings(target);
        return warnings.stream()
                .filter(Warning::isActive)
                .mapToInt(Warning::getSeverity)
                .sum();
    }

    @Override
    public Warning getWarning(int warningId) {
        return database.getWarning(warningId);
    }

    @Override
    public void checkAutoPunishment(UUID target) {
        int warningLevel = getWarningLevel(target);
        
        // Auto-punishment thresholds
        if (warningLevel >= 15) {
            // Auto-ban for 24 hours
            punishmentService.ban(null, target, "Auto-ban: High warning level (" + warningLevel + ")", 
                    System.currentTimeMillis() + (24 * 60 * 60 * 1000));
        } else if (warningLevel >= 10) {
            // Auto-mute for 2 hours
            punishmentService.mute(null, target, "Auto-mute: High warning level (" + warningLevel + ")", 
                    System.currentTimeMillis() + (2 * 60 * 60 * 1000));
        } else if (warningLevel >= 5) {
            // Auto-kick
            // Note: This would require a kick method in PunishmentService
            // For now, we'll just log it
            plugin.getLogger().info("Player " + target + " should be auto-kicked (warning level: " + warningLevel + ")");
        }
    }
}
