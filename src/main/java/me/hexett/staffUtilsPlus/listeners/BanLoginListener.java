package me.hexett.staffUtilsPlus.listeners;

import me.hexett.staffUtilsPlus.impl.PunishmentServiceImpl;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.Punishment;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import me.hexett.staffUtilsPlus.utils.ColorUtils;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Listener for handling player login attempts and checking for active bans.
 * Prevents banned players and IP banned players from joining the server.
 * 
 * @author Hexett
 */
public class BanLoginListener implements Listener {

    /**
     * Handle player pre-login events to check for active bans.
     * 
     * @param event The pre-login event
     */
    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String ipAddress = event.getAddress().getHostAddress();
        
        try {
            PunishmentService service = ServiceRegistry.get(PunishmentService.class);
            if (service == null) {
                // Service not available, allow login
                return;
            }

            // Check for regular bans
            Optional<Punishment> activeBan = service.getActivePunishment(uuid, Punishment.Type.BAN);
            if (activeBan.isEmpty()) {
                activeBan = service.getActivePunishment(uuid, Punishment.Type.TEMP_BAN);
            }
            
            if (activeBan.isPresent()) {
                Punishment ban = activeBan.get();
                String kickMessage = buildKickMessage(ban);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage);
                return;
            }

            // Check for IP bans
            Optional<Punishment> activeIPBan = service.getActiveIPBan(ipAddress);
            if (activeIPBan.isPresent()) {
                Punishment ipBan = activeIPBan.get();
                String kickMessage = buildIPBanKickMessage(ipBan);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage);
            }
        } catch (Exception e) {
            // Log error but don't prevent login
            System.err.println("Error checking ban status for " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Build a kick message for banned players.
     * 
     * @param ban The active ban
     * @return The formatted kick message
     */
    private String buildKickMessage(Punishment ban) {
        String reason = ban.getReason();
        long expiresAt = ban.getExpiresAt();

        return String.join("\n",
                MessagesConfig.getList("ban-screen")
                        .stream()
                        .map(line -> ColorUtils.translateColorCodes(
                                line.replace("%reason%", reason)
                                        .replace("%expires%", ban.isPermanent() ? "Never" : formatTime(expiresAt))
                        ))
                        .toList()
        );
    }

    /**
     * Build a kick message for IP banned players.
     * 
     * @param ipBan The active IP ban
     * @return The formatted kick message
     */
    private String buildIPBanKickMessage(Punishment ipBan) {
        String reason = ipBan.getReason();
        long expiresAt = ipBan.getExpiresAt();

        return String.join("\n",
                MessagesConfig.getList("ipban-screen")
                        .stream()
                        .map(line -> ColorUtils.translateColorCodes(
                                line.replace("%reason%", reason)
                                        .replace("%expires%", ipBan.isPermanent() ? "Never" : formatTime(expiresAt))
                        ))
                        .toList()
        );
    }

    /**
     * Format a timestamp into a readable duration string.
     * 
     * @param millis The timestamp in milliseconds
     * @return The formatted duration string
     */
    private String formatTime(long millis) {
        long currentTime = System.currentTimeMillis();
        long remainingTime = millis - currentTime;
        
        if (remainingTime <= 0) {
            return "Expired";
        }

        long seconds = remainingTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append("d ");
        }
        if (hours % 24 > 0) {
            result.append(hours % 24).append("h ");
        }
        if (minutes % 60 > 0) {
            result.append(minutes % 60).append("m");
        }

        return result.toString().trim();
    }
}