package me.hexett.staffUtilsPlus;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.hexett.staffUtilsPlus.commands.punish.*;
import me.hexett.staffUtilsPlus.commands.util.HelpCommand;
import me.hexett.staffUtilsPlus.commands.util.NotesCommand;
import me.hexett.staffUtilsPlus.commands.util.ReloadCommand;
import me.hexett.staffUtilsPlus.commands.util.VanishCommand;
import me.hexett.staffUtilsPlus.db.Database;
import me.hexett.staffUtilsPlus.db.LocalDatabase;
import me.hexett.staffUtilsPlus.db.SQLDatabase;
import me.hexett.staffUtilsPlus.impl.PunishmentServiceImpl;
import me.hexett.staffUtilsPlus.impl.VanishServiceImpl;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import me.hexett.staffUtilsPlus.service.vanish.VanishService;
import me.hexett.staffUtilsPlus.utils.ColorUtils;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import me.hexett.staffUtilsPlus.commands.*;
import me.hexett.staffUtilsPlus.listeners.*;
import me.hexett.staffUtilsPlus.menu.StaffMenuManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;
import me.hexett.staffUtilsPlus.service.notes.NoteService;
import me.hexett.staffUtilsPlus.impl.NoteServiceImpl;
import me.hexett.staffUtilsPlus.service.warnings.WarningService;
import me.hexett.staffUtilsPlus.impl.WarningServiceImpl;

/**
 * Main plugin class for StaffUtilsPlus - A comprehensive moderation plugin.
 * 
 * @author Hexett
 * @version 1.0
 */
public final class StaffUtilsPlus extends JavaPlugin {

    private static StaffUtilsPlus instance;
    private static Database database;
    private static PunishmentService punishmentService;
    private static boolean debugMode;
    private StaffMenuManager menuManager;
    private ProtocolManager protocolManager;

    private final Logger log = getLogger();

    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        saveDefaultConfig();
        debugMode = getConfig().getBoolean("debug-mode", false);

        // Load messages configuration
        MessagesConfig.load(this);
        
        initDatabase();
        initServices();
        displayStartupMessage();
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
        displayShutdownMessage();
    }

    /**
     * Initialize the database connection based on configuration.
     */
    private void initDatabase() {
        String type = getConfig().getString("database.type");
        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port", 3306);
        String name = getConfig().getString("database.name");
        String user = getConfig().getString("database.user");
        String pass = getConfig().getString("database.pass");
        boolean dbEnabled = getConfig().getBoolean("database.enabled", false);

        if (!dbEnabled) {
            log.warning(MessagesConfig.get("errors.database.not-enabled"));
            database = new LocalDatabase(this);
            return;
        }

        // Validate required database configuration
        if (type == null || type.isEmpty() ||
                (type.equalsIgnoreCase("mysql") && (host == null || host.isEmpty() || 
                 name == null || name.isEmpty() || user == null || user.isEmpty()))) {
            log.warning(MessagesConfig.get("errors.database.incomplete-info"));
            database = new LocalDatabase(this);
            return;
        }

        try {
            database = new SQLDatabase(this, type, host, port, name, user, pass);
            database.connect();
            log.info("Database connection established successfully.");
        } catch (Exception e) {
            log.severe("Failed to connect to the database: " + e.getMessage());
            log.warning("Falling back to local storage...");
            database = new LocalDatabase(this);
        }
    }

    /**
     * Initialize and register all services.
     */
    private void initServices() {
        punishmentService = new PunishmentServiceImpl(database, this);
        menuManager = new StaffMenuManager();
        
        // Initialize new services
        NoteService noteService = new NoteServiceImpl(database, this);
        WarningService warningService = new WarningServiceImpl(database, punishmentService, this);
        VanishService vanishService = new VanishServiceImpl();

        ServiceRegistry.register(Database.class, database);
        ServiceRegistry.register(PunishmentService.class, punishmentService);
        ServiceRegistry.register(NoteService.class, noteService);
        ServiceRegistry.register(WarningService.class, warningService);
        ServiceRegistry.register(VanishService.class, vanishService);

        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
    }

    /**
     * Register all plugin commands.
     */
    private void registerCommands() {
        // Register all commands
        CommandRegistry.registerCommand("ban", new BanCommand(), "tempban");
        CommandRegistry.registerCommand("unban", new UnbanCommand());
        CommandRegistry.registerCommand("kick", new KickCommand());
        CommandRegistry.registerCommand("mute", new MuteCommand(), "tempmute");
        CommandRegistry.registerCommand("unmute", new UnmuteCommand());
        CommandRegistry.registerCommand("ipban", new IPBanCommand());
        CommandRegistry.registerCommand("unbanip", new UnbanIPCommand());
        CommandRegistry.registerCommand("notes", new NotesCommand());
        CommandRegistry.registerCommand("warnings", new WarningsCommand());
        // CommandRegistry.registerCommand("staffmenu", new StaffMenuCommand(menuManager));
        CommandRegistry.registerCommand("help", new HelpCommand());
        CommandRegistry.registerCommand("reload", new ReloadCommand());
        CommandRegistry.registerCommand("vanish", new VanishCommand());
        
        log.info("Registered " + CommandRegistry.getCommandCount() + " commands.");
    }

    /**
     * Register all plugin listeners.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BanLoginListener(), this);
        getServer().getPluginManager().registerEvents(new MuteListener(), this);
        getServer().getPluginManager().registerEvents(new IPAddressListener(), this);
        getServer().getPluginManager().registerEvents(new MenuClickListener(menuManager), this);
        getServer().getPluginManager().registerEvents(new VanishListeners(), this);
        
        log.info("Registered listeners for bans, IP bans, mutes, and menus");
    }

    /**
     * Display the plugin startup message.
     */
    private void displayStartupMessage() {
        log.info(ColorUtils.translateColorCodes("&f================================"));
        log.info(ColorUtils.translateColorCodes("&f========[&6StaffUtilsPlus]&f========"));
        log.info(ColorUtils.translateColorCodes("&f========[&aPlugin Enabled]&f========"));
        log.info(ColorUtils.translateColorCodes("&f========[&8Made by Hexett]&f========"));
        log.info(ColorUtils.translateColorCodes("&f================================"));
    }

    /**
     * Display the plugin shutdown message.
     */
    private void displayShutdownMessage() {
        log.info(ColorUtils.translateColorCodes("&f================================"));
        log.info(ColorUtils.translateColorCodes("&f========[&6StaffUtilsPlus]&f========"));
        log.info(ColorUtils.translateColorCodes("&f========[&cPlugin Disabled]&f======="));
        log.info(ColorUtils.translateColorCodes("&f================================"));
    }

    /**
     * Get the plugin instance.
     * 
     * @return The plugin instance
     */
    public static StaffUtilsPlus getInstance() {
        return instance;
    }

    /**
     * Check if debug mode is enabled.
     * 
     * @return true if debug mode is enabled, false otherwise
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Reload the messages' configuration.
     */
    public void reloadMessages() {
        MessagesConfig.load(this);
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
