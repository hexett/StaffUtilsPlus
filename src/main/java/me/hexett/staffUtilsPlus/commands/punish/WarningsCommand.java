package me.hexett.staffUtilsPlus.commands.punish;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.warnings.Warning;
import me.hexett.staffUtilsPlus.service.warnings.WarningService;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import me.hexett.staffUtilsPlus.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command for managing player warnings.
 * 
 * @author Hexett
 */
public class WarningsCommand extends BaseCommand {

    public WarningsCommand() {
        super(
            "staffutils.warnings",
            "/warnings <player> [add | remove] [reason | severity | id]",
            "Manage player warnings",
            false,
            1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        String targetName = args[0];
        
        // Get target UUID
        UUID targetUUID = PlayerUtils.getPlayerUUID(targetName);
        if (targetUUID == null) {
            sendMessage(sender, MessagesConfig.get("errors.player-not-found").replace("%player%", targetName));
            return true;
        }

        if (args.length == 1) {
            // Show warnings
            showWarnings(sender, targetUUID, targetName);
        } else if (args.length >= 4 && args[1].equalsIgnoreCase("add")) {
            // Add warning
            try {
                int severity = Integer.parseInt(args[2]);
                if (severity < 1 || severity > 5) {
                    sendMessage(sender, MessagesConfig.get("errors.invalid-severity"));
                    return true;
                }
                
                String reason = String.join(" ", args).substring(args[0].length() + args[1].length() + args[2].length() + 3);
                addWarning(sender, targetUUID, reason, severity);
            } catch (NumberFormatException e) {
                sendMessage(sender, MessagesConfig.get("errors.invalid-severity"));
            }
        } else if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
            // Remove warning
            try {
                int warningId = Integer.parseInt(args[2]);
                removeWarning(sender, targetUUID, warningId);
            } catch (NumberFormatException e) {
                sendMessage(sender, MessagesConfig.get("errors.invalid-warning-id"));
            }
        } else {
            sendMessage(sender, getUsage());
        }

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Tab complete player names
            String partialName = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            completions.add("add");
            completions.add("remove");
        } else if (args.length == 3 && args[1].equalsIgnoreCase("add")) {
            completions.add("1");
            completions.add("2");
            completions.add("3");
            completions.add("4");
            completions.add("5");
        }

        return completions;
    }

    /**
     * Show all warnings for a player.
     * 
     * @param sender The command sender
     * @param targetUUID The target player's UUID
     * @param targetName The target player's name
     */
    private void showWarnings(CommandSender sender, UUID targetUUID, String targetName) {
        WarningService warningService = ServiceRegistry.get(WarningService.class);
        if (warningService == null) {
            sendMessage(sender, MessagesConfig.get("errors.service-unavailable"));
            return;
        }

        List<Warning> warnings = warningService.getWarnings(targetUUID);
        int warningLevel = warningService.getWarningLevel(targetUUID);
        
        if (warnings.isEmpty()) {
            sendMessage(sender, MessagesConfig.get("warnings.none").replace("%target%", targetName));
            return;
        }

        sendMessage(sender, MessagesConfig.get("warnings.header").replace("%target%", targetName));
        sendMessage(sender, MessagesConfig.get("warnings.level").replace("%level%", String.valueOf(warningLevel)));
        
        for (Warning warning : warnings) {
            if (!warning.isActive()) continue;
            
            String issuerName = getPlayerName(warning.getIssuer());
            String timestamp = formatTimestamp(warning.getTimestamp());
            String severityColor = getSeverityColor(warning.getSeverity());
            
            String warningMessage = MessagesConfig.get("warnings.format")
                    .replace("%id%", String.valueOf(warning.getId()))
                    .replace("%severity%", severityColor + warning.getSeverity())
                    .replace("%reason%", warning.getReason())
                    .replace("%issuer%", issuerName)
                    .replace("%timestamp%", timestamp);
            
            sendMessage(sender, warningMessage);
        }
    }

    /**
     * Add a warning to a player.
     * 
     * @param sender The command sender
     * @param targetUUID The target player's UUID
     * @param reason The warning reason
     * @param severity The warning severity (1-5)
     */
    private void addWarning(CommandSender sender, UUID targetUUID, String reason, int severity) {
        if (reason.isEmpty()) {
            sendMessage(sender, MessagesConfig.get("errors.warning-reason-empty"));
            return;
        }

        WarningService warningService = ServiceRegistry.get(WarningService.class);
        if (warningService == null) {
            sendMessage(sender, MessagesConfig.get("errors.service-unavailable"));
            return;
        }

        UUID issuerUUID = null;
        if (sender instanceof Player) {
            issuerUUID = ((Player) sender).getUniqueId();
        }

        warningService.warnPlayer(targetUUID, issuerUUID, reason, severity);
        
        String targetName = getPlayerName(targetUUID);
        String severityColor = getSeverityColor(severity);
        String successMessage = MessagesConfig.get("warnings.added")
                .replace("%target%", targetName)
                .replace("%severity%", severityColor + severity)
                .replace("%reason%", reason);
        sendMessage(sender, successMessage);
        
        // Check for auto-punishment
        warningService.checkAutoPunishment(targetUUID);
    }

    /**
     * Remove a warning from a player.
     * 
     * @param sender The command sender
     * @param targetUUID The target player's UUID
     * @param warningId The warning ID to remove
     */
    private void removeWarning(CommandSender sender, UUID targetUUID, int warningId) {
        WarningService warningService = ServiceRegistry.get(WarningService.class);
        if (warningService == null) {
            sendMessage(sender, MessagesConfig.get("errors.service-unavailable"));
            return;
        }

        warningService.removeWarning(targetUUID, warningId);
        
        String targetName = getPlayerName(targetUUID);
        String successMessage = MessagesConfig.get("warnings.removed")
                .replace("%target%", targetName)
                .replace("%id%", String.valueOf(warningId));
        sendMessage(sender, successMessage);
    }

    /**
     * Get a player's name from their UUID.
     * 
     * @param uuid The player's UUID
     * @return The player's name, or "Unknown" if not found
     */
    private String getPlayerName(UUID uuid) {
        if (uuid == null) {
            return "Console";
        }
        
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "Unknown";
    }

    /**
     * Get the color for a severity level.
     * 
     * @param severity The severity level
     * @return The color code
     */
    private String getSeverityColor(int severity) {
        return switch (severity) {
            case 1 -> "&a"; // Green
            case 2 -> "&e"; // Yellow
            case 3 -> "&6"; // Gold
            case 4 -> "&c"; // Red
            case 5 -> "&4"; // Dark Red
            default -> "&f"; // White
        };
    }

    /**
     * Format a timestamp into a readable string.
     * 
     * @param timestamp The timestamp in milliseconds
     * @return The formatted timestamp
     */
    private String formatTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;
        
        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            long minutes = diff / 60000;
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (diff < 86400000) { // Less than 1 day
            long hours = diff / 3600000;
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else {
            long days = diff / 86400000;
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        }
    }
}
