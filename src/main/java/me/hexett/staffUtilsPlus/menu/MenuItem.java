package me.hexett.staffUtilsPlus.menu;

import org.bukkit.Material;

/**
 * Represents an item in the staff menu.
 * 
 * @author Hexett
 */
public class MenuItem {
    
    private final Material material;
    private final String displayName;
    private final String description;
    private final String permission;
    
    public MenuItem(Material material, String displayName, String description, String permission) {
        this.material = material;
        this.displayName = displayName;
        this.description = description;
        this.permission = permission;
    }
    
    // Getters
    public Material getMaterial() {
        return material;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getPermission() {
        return permission;
    }
}
