package me.hexett.staffUtilsPlus.commands;

import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import me.hexett.staffUtilsPlus.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command for muting players.
 * 
 * @author Hexett
 */
public class MuteCommand extends BaseCommand {

    public MuteCommand() {
        super(
            "staffutils.mute",
            "/mute <player> [reason] [duration]",
            "Mute a player on the server",
            false,
            1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        String targetName = args[0];
        String reason = args.length > 1 ? args[1] : MessagesConfig.get("punishments.mute.default-reason");
        long duration = -1; // Permanent by default

        // Parse duration if provided
        if (args.length > 2) {
            try {
                duration = parseDuration(args[2]);
            } catch (IllegalArgumentException e) {
                sendMessage(sender, MessagesConfig.get("errors.invalid-duration"));
                return true;
            }
        }

        // Get target UUID
        UUID targetUUID = PlayerUtils.getPlayerUUID(targetName);
        if (targetUUID == null) {
            sendMessage(sender, MessagesConfig.get("errors.player-not-found").replace("%player%", targetName));
            return true;
        }

        // Check if player is already muted
        PunishmentService punishmentService = ServiceRegistry.get(PunishmentService.class);
        if (punishmentService.isMuted(targetUUID)) {
            sendMessage(sender, MessagesConfig.get("punishments.mute.already-muted").replace("%target%", targetName));
            return true;
        }

        // Get issuer UUID
        UUID issuerUUID = null;
        if (sender instanceof Player) {
            issuerUUID = ((Player) sender).getUniqueId();
        }

        // Calculate expiration time
        long expiresAt = duration == -1 ? -1 : System.currentTimeMillis() + duration;

        // Execute the mute
        punishmentService.mute(issuerUUID, targetUUID, reason, expiresAt);

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
            // Tab complete common reasons
            completions.add("Spam");
            completions.add("Inappropriate");
            completions.add("Harassment");
            completions.add("Advertising");
        } else if (args.length == 3) {
            // Tab complete duration formats
            completions.add("1h");
            completions.add("1d");
            completions.add("1w");
            completions.add("1m");
            completions.add("permanent");
        }

        return completions;
    }



    /**
     * Parse a duration string into milliseconds.
     * 
     * @param duration The duration string (e.g., "1h", "1d", "1w")
     * @return Duration in milliseconds
     * @throws IllegalArgumentException If the duration format is invalid
     */
    private long parseDuration(String duration) throws IllegalArgumentException {
        if (duration.equalsIgnoreCase("permanent")) {
            return -1;
        }

        if (duration.length() < 2) {
            throw new IllegalArgumentException("Invalid duration format");
        }

        String numberStr = duration.substring(0, duration.length() - 1);
        char unit = duration.charAt(duration.length() - 1);

        try {
            long number = Long.parseLong(numberStr);
            
            return switch (unit) {
                case 's' -> number * 1000L;
                case 'm' -> number * 60 * 1000L;
                case 'h' -> number * 60 * 60 * 1000L;
                case 'd' -> number * 24 * 60 * 60 * 1000L;
                case 'w' -> number * 7 * 24 * 60 * 60 * 1000L;
                case 'M' -> number * 30L * 24 * 60 * 60 * 1000L;
                case 'y' -> number * 365L * 24 * 60 * 60 * 1000L;
                default -> throw new IllegalArgumentException("Invalid time unit: " + unit);
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + numberStr);
        }
    }
}
