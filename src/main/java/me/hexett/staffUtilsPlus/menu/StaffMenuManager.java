package me.hexett.staffUtilsPlus.menu;

import me.hexett.staffUtilsPlus.utils.ColorUtils;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the paginated staff menu system.
 * 
 * @author Hexett
 */
public class StaffMenuManager {
    
    private static final int MENU_SIZE = 54; // 6 rows
    private static final int ITEMS_PER_PAGE = 45; // 5 rows of items, 1 row for navigation
    
    private final PlayerManagementMenu playerManagementMenu;
    private final ServerInformationMenu serverInformationMenu;
    
    public StaffMenuManager() {
        this.playerManagementMenu = new PlayerManagementMenu();
        this.serverInformationMenu = new ServerInformationMenu();
    }
    
    /**
     * Open the main staff menu for a player.
     * 
     * @param player The player to open the menu for
     */
    public void openMainMenu(Player player) {
        openMenuPage(player, 0);
    }
    
    /**
     * Open a specific page of the staff menu.
     * 
     * @param player The player to open the menu for
     * @param page The page number (0-based)
     */
    public void openMenuPage(Player player, int page) {
        Inventory menu = createMenuInventory(page);
        player.openInventory(menu);
    }
    
    /**
     * Create the inventory for a specific menu page.
     * 
     * @param page The page number
     * @return The inventory
     */
    private Inventory createMenuInventory(int page) {
        String title = ColorUtils.translateColorCodes("&8Staff Menu &7- Page " + (page + 1));
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, title);
        
        // Add menu items
        addMenuItems(inventory, page);
        
        // Add navigation items
        addNavigationItems(inventory, page);
        
        return inventory;
    }
    
    /**
     * Add menu items to the inventory.
     * 
     * @param inventory The inventory to add items to
     * @param page The current page
     */
    private void addMenuItems(Inventory inventory, int page) {
        List<MenuItem> allItems = getMenuItems();
        int startIndex = page * ITEMS_PER_PAGE;
        
        for (int i = 0; i < ITEMS_PER_PAGE && (startIndex + i) < allItems.size(); i++) {
            MenuItem item = allItems.get(startIndex + i);
            inventory.setItem(i, createMenuItem(item));
        }
    }
    
    /**
     * Add navigation items to the inventory.
     * 
     * @param inventory The inventory to add items to
     * @param page The current page
     */
    private void addNavigationItems(Inventory inventory, int page) {
        List<MenuItem> allItems = getMenuItems();
        int totalPages = (int) Math.ceil((double) allItems.size() / ITEMS_PER_PAGE);
        
        // Previous page button
        if (page > 0) {
            ItemStack prevButton = createNavigationItem(Material.ARROW, "&cPrevious Page", 
                "&7Click to go to page " + page);
            inventory.setItem(45, prevButton);
        }
        
        // Next page button
        if (page < totalPages - 1) {
            ItemStack nextButton = createNavigationItem(Material.ARROW, "&aNext Page", 
                "&7Click to go to page " + (page + 2));
            inventory.setItem(53, nextButton);
        }
        
        // Page info
        ItemStack pageInfo = createNavigationItem(Material.PAPER, "&ePage " + (page + 1) + " of " + totalPages,
            "&7Total items: " + allItems.size());
        inventory.setItem(49, pageInfo);
    }
    
    /**
     * Get all available menu items.
     * 
     * @return List of menu items
     */
    private List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        
        // Player Management
        items.add(new MenuItem(Material.PLAYER_HEAD, "&aPlayer Management", 
            "&7Manage players, view history", "staffmenu.player"));
        items.add(new MenuItem(Material.BOOK, "&ePlayer Notes", 
            "&7View and manage player notes", "staffmenu.notes"));
        items.add(new MenuItem(Material.WRITABLE_BOOK, "&cPlayer Warnings", 
            "&7View and manage player warnings", "staffmenu.warnings"));
        
        // Punishments
        items.add(new MenuItem(Material.BARRIER, "&4Ban Management", 
            "&7Ban and unban players", "staffmenu.bans"));
        items.add(new MenuItem(Material.MUTTON, "&6Mute Management", 
            "&7Mute and unmute players", "staffmenu.mutes"));
        items.add(new MenuItem(Material.IRON_BARS, "&8IP Ban Management", 
            "&7Manage IP bans", "staffmenu.ipbans"));
        
        // Server Tools
        items.add(new MenuItem(Material.COMPASS, "&bServer Information", 
            "&7View server statistics", "staffmenu.server"));
        items.add(new MenuItem(Material.CLOCK, "&dPerformance Monitor", 
            "&7Monitor server performance", "staffmenu.performance"));
        items.add(new MenuItem(Material.GRASS_BLOCK, "&3World Management", 
            "&7Manage server worlds", "staffmenu.worlds"));
        
        // Staff Tools
        items.add(new MenuItem(Material.COMMAND_BLOCK, "&5Command Shortcuts", 
            "&7Quick access to commands", "staffmenu.commands"));
        items.add(new MenuItem(Material.REDSTONE, "&1Staff Activity", 
            "&7View staff activity logs", "staffmenu.activity"));
        items.add(new MenuItem(Material.CRAFTING_TABLE, "&9Plugin Management", 
            "&7Manage server plugins", "staffmenu.plugins"));
        
        return items;
    }
    
    /**
     * Create a menu item from a MenuItem.
     * 
     * @param item The MenuItem
     * @return The ItemStack
     */
    private ItemStack createMenuItem(MenuItem item) {
        ItemStack itemStack = new ItemStack(item.material());
        ItemMeta meta = itemStack.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ColorUtils.translateColorCodes(item.displayName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.translateColorCodes(item.description()));
            lore.add("");
            lore.add(ColorUtils.translateColorCodes("&7Permission: &e" + item.permission()));
            
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        
        return itemStack;
    }
    
    /**
     * Create a navigation item.
     * 
     * @param material The material
     * @param displayName The display name
     * @param description The description
     * @return The ItemStack
     */
    private ItemStack createNavigationItem(Material material, String displayName, String description) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ColorUtils.translateColorCodes(displayName));
            
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.translateColorCodes(description));
            
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        
        return itemStack;
    }
    
    /**
     * Handle menu item clicks.
     * 
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param page The current page
     */
    public void handleMenuClick(Player player, int slot, int page) {
        if (slot >= ITEMS_PER_PAGE) {
            // Navigation item clicked
            handleNavigationClick(player, slot, page);
            return;
        }
        
        // Menu item clicked
        List<MenuItem> items = getMenuItems();
        int itemIndex = page * ITEMS_PER_PAGE + slot;
        
        if (itemIndex < items.size()) {
            MenuItem item = items.get(itemIndex);
            handleMenuItemClick(player, item);
        }
    }
    
    /**
     * Handle navigation item clicks.
     * 
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param page The current page
     */
    private void handleNavigationClick(Player player, int slot, int page) {
        if (slot == 45 && page > 0) {
            // Previous page
            openMenuPage(player, page - 1);
        } else if (slot == 53) {
            // Next page
            openMenuPage(player, page + 1);
        }
    }
    
    /**
     * Handle menu item clicks.
     * 
     * @param player The menu item that was clicked
     * @param item The menu item that was clicked
     */
    private void handleMenuItemClick(Player player, MenuItem item) {
        if (!player.hasPermission(item.permission())) {
            player.sendMessage(MessagesConfig.get("errors.no-permission"));
            return;
        }
        
        // Handle different menu items
        switch (item.permission()) {
            case "staffmenu.notes":
                // Open notes menu
                player.sendMessage(ColorUtils.translateColorCodes("&aOpening notes menu..."));
                break;
            case "staffmenu.warnings":
                // Open warnings menu
                player.sendMessage(ColorUtils.translateColorCodes("&aOpening warnings menu..."));
                break;
            case "staffmenu.player":
                // Open player management
                playerManagementMenu.openMenu(player);
                break;
            case "staffmenu.server":
                // Open server information
                serverInformationMenu.openMenu(player);
                break;
            case "staffmenu.performance":
                // Open performance monitor
                player.sendMessage(ColorUtils.translateColorCodes("&aPerformance monitor coming soon!"));
                break;
            case "staffmenu.worlds":
                // Open world management
                player.sendMessage(ColorUtils.translateColorCodes("&aWorld management coming soon!"));
                break;
            case "staffmenu.commands":
                // Open command shortcuts
                player.sendMessage(ColorUtils.translateColorCodes("&aCommand shortcuts coming soon!"));
                break;
            case "staffmenu.activity":
                // Open staff activity
                player.sendMessage(ColorUtils.translateColorCodes("&aStaff activity logs coming soon!"));
                break;
            case "staffmenu.plugins":
                // Open plugin management
                player.sendMessage(ColorUtils.translateColorCodes("&aPlugin management coming soon!"));
                break;
            default:
                player.sendMessage(ColorUtils.translateColorCodes("&7Feature coming soon: " + item.displayName()));
                break;
        }
    }
    
    /**
     * Get the player management menu.
     * 
     * @return The player management menu
     */
    public PlayerManagementMenu getPlayerManagementMenu() {
        return playerManagementMenu;
    }
    
    /**
     * Get the server information menu.
     * 
     * @return The server information menu
     */
    public ServerInformationMenu getServerInformationMenu() {
        return serverInformationMenu;
    }
}
