package me.hexett.staffUtilsPlus.commands.punish;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.db.Database;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.Punishment;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HistoryCommand extends BaseCommand {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public HistoryCommand() {
        super(
                "staffutils.history",
                "/history <player>",
                "Shows the punishment history of a player.",
                false,
                1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        String targetPlayer = args[0];

        // Get the database from ServiceRegistry
        Database database = ServiceRegistry.get(Database.class);
        if (database == null) {
            sender.sendMessage(MessagesConfig.get("errors.database.not-found"));
            return false;
        }

        // Resolve player UUID (works for online and offline players)
        UUID targetUUID = resolvePlayerUUID(targetPlayer);
        if (targetUUID == null) {
            sender.sendMessage(MessagesConfig.get("errors.player-not-found").replace("%player%", targetPlayer));
            return false;
        }

        // Fetch punishment history asynchronously to avoid blocking the main thread
        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("StaffUtilsPlus"), () -> {
            List<Punishment> punishments = database.getPunishments(targetUUID);

            // Send results back on the main thread for chat messages
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("StaffUtilsPlus"), () -> {
                if (punishments.isEmpty()) {
                    sender.sendMessage(MessagesConfig.get("punishments.history.no-punishments").replace("%player%", targetPlayer));
                    return;
                }

                // Count active and inactive punishments
                long activePunishments = punishments.stream().filter(Punishment::isActive).count();
                long inactivePunishments = punishments.size() - activePunishments;

                sender.sendMessage(ChatColor.GOLD + "=== Punishment History for " + targetPlayer + " ===");
                sender.sendMessage(ChatColor.GRAY + "Total: " + punishments.size() +
                        ChatColor.GREEN + " (" + activePunishments + " active" +
                        ChatColor.GRAY + ", " + inactivePunishments + " inactive)");
                sender.sendMessage("");

                for (Punishment punishment : punishments) {
                    String timestamp = DATE_FORMAT.format(new Date(punishment.getIssuedAt()));
                    String type = punishment.getType().name();
                    String issuer = punishment.getIssuer() != null ?
                            resolvePlayerName(punishment.getIssuer()) : "Console";
                    String status = punishment.isActive() ?
                            ChatColor.GREEN + "[ACTIVE]" :
                            ChatColor.GRAY + "[INACTIVE]";

                    sender.sendMessage(ChatColor.YELLOW + timestamp + ChatColor.GRAY + " | " +
                            ChatColor.RED + type + ChatColor.GRAY + " | " +
                            ChatColor.AQUA + "By: " + issuer + " " + status);

                    if (punishment.getReason() != null && !punishment.getReason().isEmpty()) {
                        sender.sendMessage(ChatColor.GRAY + "  Reason: " + ChatColor.ITALIC + punishment.getReason());
                    }

                    // Show expiration info for temporary punishments
                    if (punishment.getExpiresAt() > 0) {
                        long expiresAt = punishment.getExpiresAt();
                        String expirationDate = DATE_FORMAT.format(new Date(expiresAt));

                        if (punishment.isActive()) {
                            long timeRemaining = expiresAt - System.currentTimeMillis();
                            if (timeRemaining > 0) {
                                String timeRemainingStr = formatDuration(timeRemaining);
                                sender.sendMessage(ChatColor.GRAY + "  Expires: " + expirationDate +
                                        ChatColor.DARK_GRAY + " (" + timeRemainingStr + " remaining)");
                            } else {
                                sender.sendMessage(ChatColor.GRAY + "  Expired: " + expirationDate);
                            }
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "  Expired: " + expirationDate);
                        }
                    } else if (punishment.isActive()) {
                        sender.sendMessage(ChatColor.GRAY + "  Duration: " + ChatColor.RED + "Permanent");
                    }

                    sender.sendMessage(""); // Blank line between punishments
                }

                sender.sendMessage(ChatColor.GOLD + "=== End of History ===");
            });
        });

        return true;
    }

    /**
     * Resolve a player name to their UUID.
     * Works for both online and offline players.
     *
     * @param playerName The player name
     * @return The player's UUID, or null if not found
     */
    @SuppressWarnings("deprecation")
    private UUID resolvePlayerUUID(String playerName) {
        // Try online players first (faster)
        org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        // Try offline players (slower, requires disk lookup)
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getUniqueId();
        }

        return null;
    }

    /**
     * Resolve a UUID to a player name.
     *
     * @param uuid The player's UUID
     * @return The player's name, or their UUID if the name can't be resolved
     */
    private String resolvePlayerName(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String name = player.getName();
        return name != null ? name : uuid.toString();
    }

    /**
     * Format a duration in milliseconds to a human-readable string.
     *
     * @param millis The duration in milliseconds
     * @return A formatted string like "2d 5h 30m"
     */
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
