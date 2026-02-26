package me.hexett.staffUtilsPlus.commands.staff;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.alts.AltAccountService;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class AltsCommand extends BaseCommand {

    public AltsCommand() {
        super(
                "staffutils.alts",
                "/alts <player>",
                "Check for alternative accounts from the same IP address.",
                false,
                1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        String targetPlayer = args[0];

        // Get the alt account service
        AltAccountService altService = ServiceRegistry.get(AltAccountService.class);
        if (altService == null) {
            sender.sendMessage(MessagesConfig.get("errors.service-unavailable"));
            return false;
        }

        // Resolve player UUID
        UUID targetUUID = resolvePlayerUUID(targetPlayer);
        if (targetUUID == null) {
            sender.sendMessage(MessagesConfig.get("errors.player-not-found").replace("%player%", targetPlayer));
            return false;
        }

        // Get the player's IP address
        String ipAddress = altService.getPlayerIP(targetUUID);
        if (ipAddress == null) {
            sender.sendMessage(MessagesConfig.get("alts.no-ip-found").replace("%player%", targetPlayer));
            return false;
        }

        // Fetch alt accounts asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("StaffUtilsPlus"), () -> {
            List<UUID> alts = altService.getAltAccounts(targetUUID);

            // Send results back on the main thread
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("StaffUtilsPlus"), () -> {
                if (alts.isEmpty()) {
                    sender.sendMessage(MessagesConfig.get("alts.no-alts-found").replace("%player%", targetPlayer));
                    return;
                }

                // Display header
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        MessagesConfig.get("alts.header")
                                .replace("%player%", targetPlayer)
                                .replace("%ip%", ipAddress)
                                .replace("%count%", String.valueOf(alts.size()))));
                sender.sendMessage("");

                // List all alt accounts
                for (UUID altUUID : alts) {
                    if (altUUID.equals(targetUUID)) {
                        continue; // Skip the target player themselves
                    }

                    String altName = resolvePlayerName(altUUID);
                    Player altPlayer = Bukkit.getPlayer(altUUID);
                    String status = altPlayer != null ?
                            ChatColor.GREEN + "[ONLINE]" :
                            ChatColor.GRAY + "[OFFLINE]";

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            MessagesConfig.get("alts.format")
                                    .replace("%alt%", altName)
                                    .replace("%status%", status)
                                    .replace("%uuid%", altUUID.toString())));
                }

                sender.sendMessage("");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        MessagesConfig.get("alts.footer")));
            });
        });

        return true;
    }

    /**
     * Resolve a player name to their UUID.
     *
     * @param playerName The player name
     * @return The player's UUID, or null if not found
     */
    @SuppressWarnings("deprecation")
    private UUID resolvePlayerUUID(String playerName) {
        // Try online players first (faster)
        Player onlinePlayer = Bukkit.getPlayer(playerName);
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