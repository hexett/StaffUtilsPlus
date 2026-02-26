package me.hexett.staffUtilsPlus.menu;

import org.bukkit.Material;

/**
 * Represents an item in the staff menu.
 *
 * @author Hexett
 */
public record MenuItem(Material material, String displayName, String description, String permission) {

}
