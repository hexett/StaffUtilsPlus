package me.hexett.staffUtilsPlus.commands.util;

import me.hexett.staffUtilsPlus.StaffUtilsPlus;
import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.commands.CommandRegistry;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for reloading the plugin configuration.
 * 
 * @author Hexett
 */
public class ReloadCommand extends BaseCommand {

    public ReloadCommand() {
        super(
            "staffutils.reload",
            "/reload",
            "Reload the plugin configuration",
            false,
            0
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        try {
            // Reload main config
            StaffUtilsPlus.getInstance().reloadConfig();
            
            // Reload messages config
            StaffUtilsPlus.getInstance().reloadMessages();
            
            // Reload commands
            CommandRegistry.reloadCommands();
            
            sendMessage(sender, MessagesConfig.get("reload.success"));
            return true;
        } catch (Exception e) {
            sendMessage(sender, MessagesConfig.get("reload.error"));
            if (sender.hasPermission("staffutils.debug")) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        // No tab completion needed for reload command
        return new ArrayList<>();
    }
}
