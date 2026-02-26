package me.hexett.staffUtilsPlus.commands;

import me.hexett.staffUtilsPlus.StaffUtilsPlus;
import me.hexett.staffUtilsPlus.utils.ColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StaffUtilsCommand extends BaseCommand {

    StaffUtilsPlus plugin = StaffUtilsPlus.getInstance();

    PluginDescriptionFile descriptionFile = plugin.getDescription();
    String version = descriptionFile.getVersion();

    public StaffUtilsCommand() {
        super(
                "",
                "",
                "Base Command for StaffUtilsPlus",
                false,
                0
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        switch (args.length) {
            case 0:
                sender.sendMessage(ColorUtils.translateColorCodes("&f================================"));
                sender.sendMessage(ColorUtils.translateColorCodes("&c&lStaffUtilsPlus"));
                sender.sendMessage(ColorUtils.translateColorCodes("&8Version &c") + version);
                sender.sendMessage(ColorUtils.translateColorCodes("&f================================"));
                break;
            case 1:
                if(args[0].equals("reload")) {
                    if(!sender.hasPermission("staffutils.reload")) {
                        sender.sendMessage(ColorUtils.translateColorCodes("&cYou don't have permission to do that!"));
                        return true;
                    }
                    if(!configFile.exists()) {
                        plugin.getLogger().warning("config.yml was missing! Resetting to defaults!");
                        plugin.saveDefaultConfig();
                    }
                    plugin.reloadConfig();
                    plugin.reloadMessages();
                    CommandRegistry.reloadCommands();
                    sender.sendMessage(ColorUtils.translateColorCodes("&aStaffUtilsPlus has been reloaded!"));
                    return true;
                }
                break;
        }
        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length == 1) {
            completions.add("reload");
            return completions;
        }
        return completions;
    }


}
