package me.hexett.staffUtilsPlus.listeners;

import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * Listener for handling muted players and preventing them from chatting.
 * 
 * @author Hexett
 */
public class MuteListener implements Listener {

    /**
     * Handle player chat events to check for active mutes.
     * 
     * @param event The chat event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        
        try {
            PunishmentService service = ServiceRegistry.get(PunishmentService.class);
            if (service == null) {
                // Service not available, allow chat
                return;
            }

            if (service.isMuted(playerUUID)) {
                // Cancel the chat event
                event.setCancelled(true);
                
                // Send mute message to the player
                String muteMessage = MessagesConfig.get("punishments.mute.chat-blocked");
                event.getPlayer().sendMessage(muteMessage);
            }
        } catch (Exception e) {
            // Log error but don't prevent chat
            System.err.println("Error checking mute status for " + playerUUID + ": " + e.getMessage());
        }
    }
}
