package me.hexett.staffUtilsPlus.listeners;

import me.hexett.staffUtilsPlus.utils.IPAddressManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for tracking player IP addresses.
 * Stores IP addresses when players join and removes them when they leave.
 * 
 * @author Hexett
 */
public class IPAddressListener implements Listener {

    /**
     * Handle player join events to store their IP address.
     * 
     * @param event The player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        IPAddressManager.storePlayerIP(event.getPlayer());
    }

    /**
     * Handle player quit events to remove their IP address.
     * 
     * @param event The player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        IPAddressManager.removePlayerIP(event.getPlayer().getUniqueId());
    }
}
