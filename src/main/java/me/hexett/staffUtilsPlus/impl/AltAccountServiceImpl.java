package me.hexett.staffUtilsPlus.impl;

import me.hexett.staffUtilsPlus.db.Database;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.alts.AltAccountService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of AltAccountService for tracking and managing alternative accounts.
 * Tracks player IP addresses and detects when players join from known IPs.
 * Uses the Database service for persistence.
 *
 * @author Hexett
 */
public class AltAccountServiceImpl implements AltAccountService, Listener {

    private final Plugin plugin;
    private final Database database;

    public AltAccountServiceImpl(Plugin plugin) {
        this.plugin = plugin;
        this.database = ServiceRegistry.get(Database.class);

        if (database == null) {
            throw new IllegalStateException("Database service must be registered before AltAccountService");
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("AltAccountService initialized with database backend");
    }

    /**
     * Test-friendly constructor allowing dependency injection of the Database.
     *
     * @param database The database implementation to use
     * @param plugin The plugin instance (may be a mock in tests)
     */
    public AltAccountServiceImpl(Database database, Plugin plugin) {
        this.plugin = plugin;
        this.database = database;

        if (this.database == null) {
            throw new IllegalArgumentException("database must not be null");
        }

        if (this.plugin != null) {
            try {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
                plugin.getLogger().info("AltAccountService initialized with injected database backend");
            } catch (Exception ignored) {
                // In unit tests the plugin mock may not support registration; ignore.
            }
        }
    }

    @Override
    public String getPlayerIP(UUID uuid) {
        return database.getPlayerIP(uuid);
    }

    @Override
    public List<UUID> getAltAccounts(UUID uuid) {
        String ip = database.getPlayerIP(uuid);
        if (ip == null) {
            return Collections.emptyList();
        }

        return database.getPlayersByIP(ip);
    }

    @Override
    public List<UUID> getPlayersByIP(String ipAddress) {
        return database.getPlayersByIP(ipAddress);
    }

    /**
     * Handle player join events to track IPs and notify staff of alts.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        UUID uuid = player.getUniqueId();

        // Check for existing alts and record IP asynchronously
        CompletableFuture.runAsync(() -> {
            // Get existing alts before recording the new IP
            List<UUID> existingAlts = database.getPlayersByIP(ipAddress);
            boolean hasAlts = existingAlts.size() > 0 && !existingAlts.stream().allMatch(u -> u.equals(uuid));

            // Record the player's IP address
            database.recordPlayerIP(uuid, ipAddress);

            // Notify staff if player has alts (on main thread)
            if (hasAlts) {
                Bukkit.getScheduler().runTask(plugin, () -> notifyStaffOfAlts(player, existingAlts));
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().warning("Error checking alts for " + player.getName() + ": " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }

    /**
     * Notify staff members when a player with known alts joins.
     */
    private void notifyStaffOfAlts(Player player, List<UUID> alts) {
        // Build alt names list
        List<String> altNames = new ArrayList<>();
        for (UUID altUUID : alts) {
            if (altUUID.equals(player.getUniqueId())) {
                continue; // Skip the current player
            }

            org.bukkit.OfflinePlayer altPlayer = Bukkit.getOfflinePlayer(altUUID);
            String name = altPlayer.getName();
            if (name != null) {
                altNames.add(name);
            }
        }

        if (altNames.isEmpty()) {
            return; // No alts to notify about
        }

        // Format the alt names
        String altList = String.join("&7, &f", altNames);

        // Get the message from config
        String prefix = me.hexett.staffUtilsPlus.utils.MessagesConfig.get("prefix");
        String message = me.hexett.staffUtilsPlus.utils.MessagesConfig.get("alts.join-notify")
                .replace("%prefix%", prefix)
                .replace("%player%", player.getName())
                .replace("%alts%", altList)
                .replace("%count%", String.valueOf(altNames.size()));

        // Send to all online staff with permission
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("staffutils.alts.notify")) {
                staff.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
            }
        }

        // Also log to console
        plugin.getLogger().info("Player " + player.getName() + " joined with " + altNames.size() + " known alt(s): " + String.join(", ", altNames));
    }
}