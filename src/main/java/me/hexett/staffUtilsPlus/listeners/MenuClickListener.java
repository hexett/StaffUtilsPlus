package me.hexett.staffUtilsPlus.listeners;

import me.hexett.staffUtilsPlus.menu.StaffMenuManager;
import me.hexett.staffUtilsPlus.menu.PlayerManagementMenu;
import me.hexett.staffUtilsPlus.menu.ServerInformationMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Listener for handling menu interactions and clicks.
 * 
 * @author Hexett
 */
public class MenuClickListener implements Listener {

    private final StaffMenuManager staffMenuManager;
    private final PlayerManagementMenu playerManagementMenu;
    private final ServerInformationMenu serverInformationMenu;

    public MenuClickListener(StaffMenuManager staffMenuManager) {
        this.staffMenuManager = staffMenuManager;
        this.playerManagementMenu = staffMenuManager.getPlayerManagementMenu();
        this.serverInformationMenu = staffMenuManager.getServerInformationMenu();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();

        // Handle different menu types
        if (title.contains("Staff Menu")) {
            handleStaffMenuClick(player, event);
        } else if (title.contains("Player Management")) {
            handlePlayerManagementClick(player, event);
        } else if (title.contains("Server Information")) {
            handleServerInformationClick(player, event);
        } else if (title.contains("Actions for")) {
            handlePlayerActionsClick(player, event);
        }
    }

    /**
     * Handle clicks in the main staff menu.
     * 
     * @param player The player who clicked
     * @param event The click event
     */
    private void handleStaffMenuClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        // Extract page number from title
        String title = event.getView().getTitle();
        int page = extractPageNumber(title);
        
        staffMenuManager.handleMenuClick(player, event.getRawSlot(), page);
    }

    /**
     * Handle clicks in the player management menu.
     * 
     * @param player The player who clicked
     * @param event The click event
     */
    private void handlePlayerManagementClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (event.getRawSlot() < 36) {
            // Player head clicked - this will open the actions menu
            return;
        }
        
        playerManagementMenu.handleMenuClick(player, event.getRawSlot());
    }

    /**
     * Handle clicks in the server information menu.
     * 
     * @param player The player who clicked
     * @param event The click event
     */
    private void handleServerInformationClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        serverInformationMenu.handleMenuClick(player, event.getRawSlot());
    }

    /**
     * Handle clicks in the player actions menu.
     * 
     * @param player The player who clicked
     * @param event The click event
     */
    private void handlePlayerActionsClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        if (slot == 10) {
            // Ban button
            player.sendMessage("Ban feature coming soon!");
        } else if (slot == 11) {
            // Mute button
            player.sendMessage("Mute feature coming soon!");
        } else if (slot == 12) {
            // Kick button
            player.sendMessage("Kick feature coming soon!");
        } else if (slot == 13) {
            // Notes button
            player.sendMessage("Notes feature coming soon!");
        } else if (slot == 14) {
            // Warnings button
            player.sendMessage("Warnings feature coming soon!");
        } else if (slot == 22) {
            // Back button
            player.closeInventory();
            playerManagementMenu.openMenu(player);
        }
    }

    /**
     * Extract the page number from a menu title.
     * 
     * @param title The menu title
     * @return The page number (0-based)
     */
    private int extractPageNumber(String title) {
        try {
            if (title.contains("Page ")) {
                String pageStr = title.split("Page ")[1].split(" ")[0];
                return Integer.parseInt(pageStr) - 1; // Convert to 0-based
            }
        } catch (Exception e) {
            // If parsing fails, default to page 0
        }
        return 0;
    }
}
