package me.hexett.staffUtilsPlus.menu;

import me.hexett.staffUtilsPlus.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Server information menu for the staff menu system.
 * 
 * @author Hexett
 */
public class ServerInformationMenu {

    private static final int MENU_SIZE = 54; // 6 rows
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    /**
     * Open the server information menu for a staff member.
     * 
     * @param staff The staff member opening the menu
     */
    public void openMenu(Player staff) {
        Inventory menu = createMenuInventory(staff);
        staff.openInventory(menu);
    }

    /**
     * Create the server information menu inventory.
     * 
     * @param staff The staff member
     * @return The inventory
     */
    private Inventory createMenuInventory(Player staff) {
        String title = ColorUtils.translateColorCodes("&8Server Information");
        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, title);

        // Add server stats
        addServerStats(inventory);

        // Add performance info
        addPerformanceInfo(inventory);

        // Add world info
        addWorldInfo(inventory);

        // Add back button
        addBackButton(inventory);

        return inventory;
    }

    /**
     * Add server statistics to the menu.
     * 
     * @param inventory The inventory to add stats to
     */
    private void addServerStats(Inventory inventory) {
        // Online players
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        ItemStack playersItem = createInfoItem(Material.PLAYER_HEAD, "&aOnline Players", 
                "&7" + onlinePlayers + "/" + maxPlayers + " players online");
        inventory.setItem(10, playersItem);

        // Server uptime
        long uptime = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
        String uptimeFormatted = formatUptime(uptime);
        ItemStack uptimeItem = createInfoItem(Material.CLOCK, "&eServer Uptime", 
                "&7" + uptimeFormatted);
        inventory.setItem(11, uptimeItem);

        // Server version
        String version = Bukkit.getVersion();
        ItemStack versionItem = createInfoItem(Material.BOOK, "&bServer Version", 
                "&7" + version);
        inventory.setItem(12, versionItem);

        // Bukkit version
        String bukkitVersion = Bukkit.getBukkitVersion();
        ItemStack bukkitItem = createInfoItem(Material.CRAFTING_TABLE, "&6Bukkit Version", 
                "&7" + bukkitVersion);
        inventory.setItem(13, bukkitItem);
    }

    /**
     * Add performance information to the menu.
     * 
     * @param inventory The inventory to add performance info to
     */
    private void addPerformanceInfo(Inventory inventory) {
        // TPS - Note: getTPS() is not available in all Bukkit versions
        ItemStack tpsItem = createInfoItem(Material.REDSTONE, "&cServer Performance", 
                "&7Click to view detailed performance info");
        inventory.setItem(19, tpsItem);

        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        
        String memoryInfo = String.format("&7Used: %d MB / %d MB", usedMemory, maxMemory);
        ItemStack memoryItem = createInfoItem(Material.EMERALD, "&aMemory Usage", 
                memoryInfo);
        inventory.setItem(20, memoryItem);

        // CPU usage
        ItemStack cpuItem = createInfoItem(Material.COMPASS, "&dCPU Usage", 
                "&7Click to view detailed CPU info");
        inventory.setItem(21, cpuItem);

        // Entity count
        int entityCount = Bukkit.getWorlds().stream()
                .mapToInt(world -> world.getEntities().size())
                .sum();
        ItemStack entityItem = createInfoItem(Material.ZOMBIE_HEAD, "&4Entity Count", 
                "&7" + entityCount + " entities loaded");
        inventory.setItem(22, entityItem);
    }

    /**
     * Add world information to the menu.
     * 
     * @param inventory The inventory to add world info to
     */
    private void addWorldInfo(Inventory inventory) {
        List<org.bukkit.World> worlds = Bukkit.getWorlds();
        
        for (int i = 0; i < Math.min(worlds.size(), 9); i++) {
            org.bukkit.World world = worlds.get(i);
            ItemStack worldItem = createWorldItem(world);
            inventory.setItem(28 + i, worldItem);
        }
    }

    /**
     * Add a back button to the menu.
     * 
     * @param inventory The inventory to add the back button to
     */
    private void addBackButton(Inventory inventory) {
        ItemStack backButton = createInfoItem(Material.ARROW, "&cBack to Main Menu", 
                "&7Return to staff menu");
        inventory.setItem(49, backButton);
    }

    /**
     * Create an information item for the menu.
     * 
     * @param material The material
     * @param displayName The display name
     * @param description The description
     * @return The information item
     */
    private ItemStack createInfoItem(Material material, String displayName, String description) {
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
     * Create a world item for the menu.
     * 
     * @param world The world
     * @return The world item
     */
    private ItemStack createWorldItem(org.bukkit.World world) {
        Material material = getWorldMaterial(world.getEnvironment());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ColorUtils.translateColorCodes("&b" + world.getName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.translateColorCodes("&7Environment: &e" + world.getEnvironment().name()));
            lore.add(ColorUtils.translateColorCodes("&7Players: &a" + world.getPlayers().size()));
            lore.add(ColorUtils.translateColorCodes("&7Entities: &c" + world.getEntities().size()));
            lore.add(ColorUtils.translateColorCodes("&7Loaded Chunks: &6" + world.getLoadedChunks().length));
            lore.add(ColorUtils.translateColorCodes("&7Time: &f" + formatWorldTime(world.getTime())));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Get the appropriate material for a world environment.
     * 
     * @param environment The world environment
     * @return The material
     */
    private Material getWorldMaterial(org.bukkit.World.Environment environment) {
        return switch (environment) {
            case NORMAL -> Material.GRASS_BLOCK;
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> Material.STONE;
        };
    }

    /**
     * Format server uptime into a readable string.
     * 
     * @param uptime The uptime in milliseconds
     * @return The formatted uptime string
     */
    private String formatUptime(long uptime) {
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Format world time into a readable string.
     * 
     * @param time The world time
     * @return The formatted time string
     */
    private String formatWorldTime(long time) {
        long hours = (time / 1000 + 6) % 24;
        long minutes = (time % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
    }

    /**
     * Handle menu item clicks.
     * 
     * @param staff The staff member who clicked
     * @param slot The slot that was clicked
     */
    public void handleMenuClick(Player staff, int slot) {
        if (slot == 49) {
            // Back button
            staff.closeInventory();
        } else if (slot == 21) {
            // CPU usage - show detailed info
            showDetailedCPUInfo(staff);
        }
    }

    /**
     * Show detailed CPU information.
     * 
     * @param staff The staff member
     */
    private void showDetailedCPUInfo(Player staff) {
        staff.sendMessage(ColorUtils.translateColorCodes("&6=== CPU Information ==="));
        staff.sendMessage(ColorUtils.translateColorCodes("&7Available Processors: &e" + Runtime.getRuntime().availableProcessors()));
        staff.sendMessage(ColorUtils.translateColorCodes("&7Total Memory: &a" + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + " MB"));
        staff.sendMessage(ColorUtils.translateColorCodes("&7Free Memory: &a" + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " MB"));
        staff.sendMessage(ColorUtils.translateColorCodes("&7Max Memory: &c" + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB"));
    }
}
