package me.hexett.staffUtilsPlus.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.hexett.staffUtilsPlus.db.Database;
import me.hexett.staffUtilsPlus.service.punishments.Punishment;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of the PunishmentService interface.
 * Handles all punishment-related operations including bans, mutes, and kicks.
 * 
 * @author Hexett
 */
public class PunishmentServiceImpl implements PunishmentService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String CONSOLE_NAME = "Console";
    
    private final Database database;
    private final Cache<UUID, List<Punishment>> cache;
    private final Plugin plugin;

    /**
     * Create a new PunishmentServiceImpl.
     * 
     * @param database The database to use for persistence
     * @param plugin The plugin instance
     */
    public PunishmentServiceImpl(Database database, Plugin plugin) {
        this.database = database;
        this.plugin = plugin;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void ban(UUID issuer, UUID target, String reason, long expiresAt) {
        if (isBanned(target)) {
            sendToIssuer(issuer, MessagesConfig.get("punishments.ban.already-banned")
                    .replace("%target%", getName(target)));
            return;
        }

        Punishment punishment = new Punishment(target, 
                expiresAt == -1 ? Punishment.Type.BAN : Punishment.Type.TEMP_BAN, 
                reason, System.currentTimeMillis(), expiresAt, issuer);
        
        database.insertPunishment(punishment);
        cache.invalidate(target);

        // Kick online player if present
        Player onlinePlayer = Bukkit.getPlayer(target);
        if (onlinePlayer != null) {
            String kickMessage = buildKickMessage(reason, expiresAt);
            onlinePlayer.kickPlayer(kickMessage);
        }

        // Broadcast ban notification
        String notification = MessagesConfig.get("punishments.ban.notify")
                .replace("%target%", getName(target))
                .replace("%issuer%", issuer != null ? getName(issuer) : CONSOLE_NAME)
                .replace("%reason%", reason);
        Bukkit.broadcast(notification, "staffutils.notify.ban");

        // Send success message to issuer
        String successMessage = MessagesConfig.get("punishments.ban.success")
                .replace("%target%", getName(target))
                .replace("%reason%", reason);
        sendToIssuer(issuer, successMessage);
    }

    @Override
    public void unban(UUID issuer, UUID target) {
        if (!isBanned(target)) {
            sendToIssuer(issuer, MessagesConfig.get("punishments.unban.not-banned")
                    .replace("%target%", getName(target)));
            return;
        }
        
        database.deactivatePunishment(target, Punishment.Type.BAN);
        database.deactivatePunishment(target, Punishment.Type.TEMP_BAN);
        cache.invalidate(target);
        
        sendToIssuer(issuer, MessagesConfig.get("punishments.unban.success")
                .replace("%target%", getName(target)));
    }

    @Override
    public boolean isBanned(UUID target) {
        return getActivePunishment(target, Punishment.Type.BAN).isPresent() ||
                getActivePunishment(target, Punishment.Type.TEMP_BAN).isPresent();
    }

    @Override
    public void kick(UUID issuer, UUID target, String reason) {
        Punishment punishment = new Punishment(target, Punishment.Type.KICK, reason, System.currentTimeMillis(), issuer);

        database.insertPunishment(punishment);
        cache.invalidate(target);

        // Kick online player if present
        Player onlinePlayer = Bukkit.getPlayer(target);
        if (onlinePlayer != null) {
            String kickMessage = buildKickMessage(reason);
            onlinePlayer.kickPlayer(kickMessage);
        }

        // Broadcast ban notification
        String notification = MessagesConfig.get("punishments.kick.notify")
                .replace("%target%", getName(target))
                .replace("%issuer%", issuer != null ? getName(issuer) : CONSOLE_NAME)
                .replace("%reason%", reason);
        Bukkit.broadcast(notification, "staffutils.notify.kick");

        // Send success message to issuer
        String successMessage = MessagesConfig.get("punishments.kick.success")
                .replace("%target%", getName(target))
                .replace("%reason%", reason);
        sendToIssuer(issuer, successMessage);
    }

    /**
     * Get the active ban for a player.
     * 
     * @param target The target player's UUID
     * @return Optional containing the active ban, or empty if not banned
     */
    public Optional<Punishment> getActiveBan(UUID target) {
        return getActivePunishment(target, Punishment.Type.BAN)
                .or(() -> getActivePunishment(target, Punishment.Type.TEMP_BAN));
    }

    @Override
    public Optional<Punishment> getActivePunishment(UUID target, Punishment.Type type) {
        return database.getPunishments(target).stream()
                .filter(p -> p.getType() == type && 
                        (p.isPermanent() || p.getExpiresAt() > System.currentTimeMillis()))
                .findFirst();
    }

    @Override
    public void mute(UUID issuer, UUID target, String reason, long expiresAt) {
        if (isMuted(target)) {
            sendToIssuer(issuer, MessagesConfig.get("punishments.mute.already-muted")
                    .replace("%target%", getName(target)));
            return;
        }
        
        Punishment punishment = new Punishment(target, 
                expiresAt == -1 ? Punishment.Type.MUTE : Punishment.Type.TEMP_MUTE, 
                reason, System.currentTimeMillis(), expiresAt, issuer);
        
        database.insertPunishment(punishment);
        cache.invalidate(target);

        // Broadcast mute notification
        String notification = MessagesConfig.get("punishments.mute.notify")
                .replace("%target%", getName(target))
                .replace("%issuer%", issuer != null ? getName(issuer) : CONSOLE_NAME)
                .replace("%reason%", reason);
        Bukkit.broadcast(notification, "staffutils.notify.mute");

        // Send success message to issuer
        String successMessage = MessagesConfig.get("punishments.mute.success")
                .replace("%target%", getName(target))
                .replace("%reason%", reason);
        sendToIssuer(issuer, successMessage);
    }

    @Override
    public void unmute(UUID issuer, UUID target) {
        if (!isMuted(target)) {
            sendToIssuer(issuer, MessagesConfig.get("punishments.unmute.not-muted")
                    .replace("%target%", getName(target)));
            return;
        }
        
        database.deactivatePunishment(target, Punishment.Type.MUTE);
        database.deactivatePunishment(target, Punishment.Type.TEMP_MUTE);
        cache.invalidate(target);
        
        sendToIssuer(issuer, MessagesConfig.get("punishments.unmute.success")
                .replace("%target%", getName(target)));
    }

    @Override
    public boolean isMuted(UUID target) {
        return getActivePunishment(target, Punishment.Type.MUTE).isPresent() ||
                getActivePunishment(target, Punishment.Type.TEMP_MUTE).isPresent();
    }

    /**
     * Get the display name for a UUID.
     * 
     * @param uuid The player's UUID
     * @return The player's name, or "Unknown" if not found
     */
    private String getName(UUID uuid) {
        if (uuid == null) {
            return CONSOLE_NAME;
        }
        
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "Unknown";
    }

    /**
     * Send a message to the issuer of a punishment.
     * 
     * @param issuer The issuer's UUID, or null for console
     * @param message The message to send
     */
    private void sendToIssuer(UUID issuer, String message) {
        if (issuer == null) {
            Bukkit.getConsoleSender().sendMessage(message);
            return;
        }
        
        Player player = Bukkit.getPlayer(issuer);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    /**
     * Build a kick message for banned players.
     * 
     * @param reason The ban reason
     * @param expiresAt When the ban expires
     * @return The formatted kick message
     */
    private String buildKickMessage(String reason, long expiresAt) {
        return String.join("\n", MessagesConfig.getList("ban-screen")
                .stream()
                .map(line -> line.replace("%reason%", reason)
                        .replace("%expires%", expiresAt == -1 ? "Never" : formatTime(expiresAt)))
                .toList());
    }

    /**
     * Format a timestamp into a readable string.
     * 
     * @param millis The timestamp in milliseconds
     * @return The formatted date string
     */
    private String formatTime(long millis) {
        return DATE_FORMAT.format(new Date(millis));
    }

    @Override
    public List<Punishment> getActiveTempBans() {
        List<Punishment> tempBans = new ArrayList<>();
        
        // Get all players from the database and check for active temp bans
        // This is a simplified implementation - in a real database, you'd want
        // a more efficient query that filters by type and expiration
        for (UUID playerUUID : getAllPlayerUUIDs()) {
            Optional<Punishment> tempBan = getActivePunishment(playerUUID, Punishment.Type.TEMP_BAN);
            if (tempBan.isPresent()) {
                tempBans.add(tempBan.get());
            }
        }
        
        return tempBans;
    }
    
    /**
     * Get all player UUIDs from the database.
     * This is a placeholder - implement based on your database structure.
     */
    private Set<UUID> getAllPlayerUUIDs() {
        // For now, return online players
        // In a real implementation, you'd query the database for all players
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());
    }

    @Override
    public void ipBan(UUID issuer, UUID target, String ipAddress, String reason, long expiresAt) {
        if (isIPBanned(ipAddress)) {
            sendToIssuer(issuer, MessagesConfig.get("punishments.ipban.already-banned")
                    .replace("%ip%", ipAddress));
            return;
        }

        Punishment punishment = new Punishment(target,
                Punishment.Type.IP_BAN,
                reason, System.currentTimeMillis(), expiresAt, issuer, ipAddress);
        
        database.insertPunishment(punishment);
        cache.invalidate(target);

        // Kick online players with this IP if present
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getAddress() != null && 
                onlinePlayer.getAddress().getAddress().getHostAddress().equals(ipAddress)) {
                String kickMessage = buildIPBanKickMessage(reason, expiresAt);
                onlinePlayer.kickPlayer(kickMessage);
            }
        }

        // Broadcast IP ban notification
        String notification = MessagesConfig.get("punishments.ipban.notify")
                .replace("%target%", getName(target))
                .replace("%ip%", ipAddress)
                .replace("%issuer%", issuer != null ? getName(issuer) : CONSOLE_NAME)
                .replace("%reason%", reason);
        Bukkit.broadcast(notification, "staffutils.notify.ipban");

        // Send success message to issuer
        String successMessage = MessagesConfig.get("punishments.ipban.success")
                .replace("%target%", getName(target))
                .replace("%ip%", ipAddress)
                .replace("%reason%", reason);
        sendToIssuer(issuer, successMessage);
    }

    @Override
    public void unbanIP(UUID issuer, String ipAddress) {
        if (!isIPBanned(ipAddress)) {
            sendToIssuer(issuer, MessagesConfig.get("punishments.unbanip.not-banned")
                    .replace("%ip%", ipAddress));
            return;
        }
        
        database.deactivateIPBan(ipAddress);
        
        // Invalidate cache for all players (IP bans affect multiple players)
        cache.invalidateAll();
        
        sendToIssuer(issuer, MessagesConfig.get("punishments.unbanip.success")
                .replace("%ip%", ipAddress));
    }

    @Override
    public boolean isIPBanned(String ipAddress) {
        return getActiveIPBan(ipAddress).isPresent();
    }

    @Override
    public Optional<Punishment> getActiveIPBan(String ipAddress) {
        return database.getPunishmentsByIP(ipAddress).stream()
                .filter(p -> p.getType() == Punishment.Type.IP_BAN && 
                        (p.isPermanent() || p.getExpiresAt() > System.currentTimeMillis()))
                .findFirst();
    }

    /**
     * Build a kick message for IP banned players.
     * 
     * @param reason The IP ban reason
     * @param expiresAt When the IP ban expires
     * @return The formatted kick message
     */
    private String buildIPBanKickMessage(String reason, long expiresAt) {
        return String.join("\n", MessagesConfig.getList("ipban-screen")
                .stream()
                .map(line -> line.replace("%reason%", reason)
                        .replace("%expires%", expiresAt == -1 ? "Never" : formatTime(expiresAt)))
                .toList());
    }

    private String buildKickMessage(String reason) {
        return String.join("\n", MessagesConfig.getList("kick-screen")
                .stream()
                .map(line -> line.replace("%reason%", reason))
                .toList());
    }
}
