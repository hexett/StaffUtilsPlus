package me.hexett.staffUtilsPlus.menu;

import me.hexett.staffUtilsPlus.utils.ColorUtils;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Player management menu for the staff menu system.
 * 
 * @author Hexett
 */
public class PlayerManagementMenu {

    private static final int MENU_SIZE = 54; // 6 rows

    /**
     * Open the player management menu for a staff member.
     * 
     * @param staff The staff member opening the menu
     */
    public void openMenu(Player staff) {
        Inventory menu = createMenuInventory(staff);
        staff.openInventory(menu);
    }

    /**
     * Create the player management menu inventory.
     * 
     * @param staff The staff member
     * @return The inventory
     */
    private Inventory createMenuInventory(Player staff) {
        String title = ColorUtils.translateColorCodes("&8Player Management");
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, title);

        // Add online players
        addOnlinePlayers(inventory, staff);

        // Add management tools
        addManagementTools(inventory);

        return inventory;
    }

    /**
     * Add online players to the menu.
     * 
     * @param inventory The inventory to add players to
     * @param staff The staff member
     */
    private void addOnlinePlayers(Inventory inventory, Player staff) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        for (int i = 0; i < Math.min(onlinePlayers.size(), 36); i++) {
            Player player = onlinePlayers.get(i);
            ItemStack playerHead = createPlayerHead(player);
            inventory.setItem(i, playerHead);
        }
    }

    /**
     * Add management tools to the menu.
     * 
     * @param inventory The inventory to add tools to
     */
    private void addManagementTools(Inventory inventory) {
        // Player search
        ItemStack searchItem = createToolItem(Material.COMPASS, "&bSearch Players", 
                "&7Search for offline players");
        inventory.setItem(45, searchItem);

        // Player history
        ItemStack historyItem = createToolItem(Material.BOOK, "&ePlayer History", 
                "&7View comprehensive player history");
        inventory.setItem(46, historyItem);

        // Quick actions
        ItemStack quickActionsItem = createToolItem(Material.ANVIL, "&aQuick Actions", 
                "&7Quick ban, mute, kick actions");
        inventory.setItem(47, quickActionsItem);

        // Back button
        ItemStack backButton = createToolItem(Material.ARROW, "&cBack to Main Menu", 
                "&7Return to staff menu");
        inventory.setItem(49, backButton);
    }

    /**
     * Create a player head item for the menu.
     * 
     * @param player The player
     * @return The player head item
     */
    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(ColorUtils.translateColorCodes("&a" + player.getName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.translateColorCodes("&7Click to manage this player"));
            lore.add("");
            lore.add(ColorUtils.translateColorCodes("&7Health: &c" + Math.round(player.getHealth()) + "/" + Math.round(player.getAttribute(Attribute.MAX_HEALTH).getValue())));
            lore.add(ColorUtils.translateColorCodes("&7Food: &6" + player.getFoodLevel() + "/20"));
            lore.add(ColorUtils.translateColorCodes("&7Gamemode: &e" + player.getGameMode().name()));
            lore.add(ColorUtils.translateColorCodes("&7World: &b" + player.getWorld().getName()));
            
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        
        return head;
    }

    /**
     * Create a tool item for the menu.
     * 
     * @param material The material
     * @param displayName The display name
     * @param description The description
     * @return The tool item
     */
    private ItemStack createToolItem(Material material, String displayName, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ColorUtils.translateColorCodes(displayName));
            
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.translateColorCodes(description));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Handle menu item clicks.
     * 
     * @param staff The staff member who clicked
     * @param slot The slot that was clicked
     */
    public void handleMenuClick(Player staff, int slot) {
        if (slot < 36) {
            // Player head clicked
            handlePlayerClick(staff, slot);
        } else if (slot == 45) {
            // Search players
            staff.sendMessage(ColorUtils.translateColorCodes("&aPlayer search feature coming soon!"));
        } else if (slot == 46) {
            // Player history
            staff.sendMessage(ColorUtils.translateColorCodes("&aPlayer history feature coming soon!"));
        } else if (slot == 47) {
            // Quick actions
            staff.sendMessage(ColorUtils.translateColorCodes("&aQuick actions feature coming soon!"));
        } else if (slot == 49) {
            // Back button
            // This would need to be handled by the main menu system
            staff.closeInventory();
        }
    }

    /**
     * Handle player head clicks.
     * 
     * @param staff The staff member
     * @param slot The slot clicked
     */
    private void handlePlayerClick(Player staff, int slot) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        if (slot < onlinePlayers.size()) {
            Player target = onlinePlayers.get(slot);
            openPlayerActionsMenu(staff, target);
        }
    }

    /**
     * Open the player actions menu for a specific player.
     * 
     * @param staff The staff member
     * @param target The target player
     */
    private void openPlayerActionsMenu(Player staff, Player target) {
        Inventory actionsMenu = Bukkit.createInventory(null, 27, 
                ColorUtils.translateColorCodes("&8Actions for " + target.getName()));

        // Ban button
        ItemStack banButton = createActionButton(Material.BARRIER, "&4Ban Player", 
                "&7Ban this player from the server");
        actionsMenu.setItem(10, banButton);

        // Mute button
        ItemStack muteButton = createActionButton(Material.MUTTON, "&6Mute Player", 
                "&7Mute this player");
        actionsMenu.setItem(11, muteButton);

        // Kick button
        ItemStack kickButton = createActionButton(Material.IRON_BOOTS, "&cKick Player", 
                "&7Kick this player from the server");
        actionsMenu.setItem(12, kickButton);

        // Notes button
        ItemStack notesButton = createActionButton(Material.BOOK, "&eView Notes", 
                "&7View and manage player notes");
        actionsMenu.setItem(13, notesButton);

        // Warnings button
        ItemStack warningsButton = createActionButton(Material.WRITABLE_BOOK, "&cView Warnings", 
                "&7View and manage player warnings");
        actionsMenu.setItem(14, kickButton);

        // Back button
        ItemStack backButton = createActionButton(Material.ARROW, "&7Back", 
                "&7Return to player list");
        actionsMenu.setItem(22, backButton);

        staff.openInventory(actionsMenu);
    }

    /**
     * Create an action button for the player actions menu.
     * 
     * @param material The material
     * @param displayName The display name
     * @param description The description
     * @return The action button
     */
    private ItemStack createActionButton(Material material, String displayName, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ColorUtils.translateColorCodes(displayName));
            
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.translateColorCodes(description));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
