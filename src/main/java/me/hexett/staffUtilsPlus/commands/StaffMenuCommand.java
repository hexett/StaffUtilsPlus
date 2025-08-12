package me.hexett.staffUtilsPlus.commands;

import me.hexett.staffUtilsPlus.menu.StaffMenuManager;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for opening the staff menu.
 * 
 * @author Hexett
 */
public class StaffMenuCommand extends BaseCommand {

    private final StaffMenuManager menuManager;

    public StaffMenuCommand(StaffMenuManager menuManager) {
        super(
            "staffutils.menu",
            "/staffmenu",
            "Open the staff menu",
            true,
            0
        );
        this.menuManager = menuManager;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, MessagesConfig.get("errors.player-only"));
            return true;
        }

        menuManager.openMainMenu(player);
        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        // No tab completion needed for this command
        return new ArrayList<>();
    }
}
