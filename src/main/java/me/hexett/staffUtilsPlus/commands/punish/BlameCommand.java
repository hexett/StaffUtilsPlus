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

public class BlameCommand extends BaseCommand {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BlameCommand() {
        super(
                "staffutils.blame",
                "/blame <player>",
                "Lists punishments carried out by certain players.",
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
        UUID issuerUUID = resolvePlayerUUID(targetPlayer);
        if (issuerUUID == null) {
            sender.sendMessage(MessagesConfig.get("errors.player-not-found").replace("%player%", targetPlayer));
            return false;
        }

        // Fetch punishments by issuer asynchronously to avoid blocking the main thread
        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("StaffUtilsPlus"), () -> {
            List<Punishment> punishments = database.getPunishmentsByIssuer(issuerUUID);

            // Send results back on the main thread for chat messages
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("StaffUtilsPlus"), () -> {
                if (punishments.isEmpty()) {
                    sender.sendMessage(MessagesConfig.get("punishments.blame.no-punishments").replace("%player%", targetPlayer));
                    return;
                }

                sender.sendMessage(ChatColor.GOLD + "=== Punishments by " + targetPlayer + " ===");
                sender.sendMessage(ChatColor.GRAY + "Total: " + punishments.size());
                sender.sendMessage("");

                for (Punishment punishment : punishments) {
                    String timestamp = DATE_FORMAT.format(new Date(punishment.getIssuedAt()));
                    String type = punishment.getType().name();
                    String target = resolvePlayerName(punishment.getTarget());
                    String status = punishment.isActive() ? ChatColor.GREEN + "[ACTIVE]" : ChatColor.GRAY + "[INACTIVE]";

                    sender.sendMessage(ChatColor.YELLOW + timestamp + ChatColor.GRAY + " | " +
                            ChatColor.RED + type + ChatColor.GRAY + " | " +
                            ChatColor.WHITE + target + " " + status);

                    if (punishment.getReason() != null && !punishment.getReason().isEmpty()) {
                        sender.sendMessage(ChatColor.GRAY + "  Reason: " + ChatColor.ITALIC + punishment.getReason());
                    }
                }

                sender.sendMessage("");
                sender.sendMessage(ChatColor.GOLD + "=== End of List ===");
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
}
