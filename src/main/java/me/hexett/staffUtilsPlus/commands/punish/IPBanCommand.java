package me.hexett.staffUtilsPlus.commands.punish;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import me.hexett.staffUtilsPlus.utils.IPAddressManager;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import me.hexett.staffUtilsPlus.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command for IP banning players.
 * 
 * @author Hexett
 */
public class IPBanCommand extends BaseCommand {

    public IPBanCommand() {
        super(
            "staffutils.ipban",
            "/ipban <player> [reason] [duration]",
            "IP ban a player on the server",
            false,
            1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        String targetName = args[0];
        String reason = args.length > 1 ? args[1] : MessagesConfig.get("punishments.ipban.default-reason");
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

        // Get target's IP address
        String ipAddress = getPlayerIP(targetUUID);
        if (ipAddress == null) {
            sendMessage(sender, MessagesConfig.get("errors.cannot-get-ip").replace("%player%", targetName));
            return true;
        }

        // Check if IP is already banned
        PunishmentService punishmentService = ServiceRegistry.get(PunishmentService.class);
        if (punishmentService.isIPBanned(ipAddress)) {
            sendMessage(sender, MessagesConfig.get("punishments.ipban.already-banned").replace("%ip%", ipAddress));
            return true;
        }

        // Get issuer UUID
        UUID issuerUUID = null;
        if (sender instanceof Player) {
            issuerUUID = ((Player) sender).getUniqueId();
        }

        // Calculate expiration time
        long expiresAt = duration == -1 ? -1 : System.currentTimeMillis() + duration;

        // Execute the IP ban
        punishmentService.ipBan(issuerUUID, targetUUID, ipAddress, reason, expiresAt);

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
            completions.add("Griefing");
            completions.add("Hacking/Cheating");
            completions.add("Inappropriate behavior");
            completions.add("Spam");
            completions.add("Harassment");
            completions.add("Advertising");
            completions.add("VPN/Proxy abuse");
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
     * Get the IP address of a player.
     * 
     * @param playerUUID The player's UUID
     * @return The player's IP address, or null if not available
     */
    private String getPlayerIP(UUID playerUUID) {
        // First try to get from IPAddressManager (for online players)
        String ipAddress = IPAddressManager.getPlayerIP(playerUUID);
        if (ipAddress != null) {
            return ipAddress;
        }
        
        // Fallback to online player check
        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null && onlinePlayer.getAddress() != null) {
            return onlinePlayer.getAddress().getAddress().getHostAddress();
        }
        
        // If not online, we can't get IP for IP ban
        return null;
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
