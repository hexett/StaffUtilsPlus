package me.hexett.staffUtilsPlus.commands.util;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.commands.CommandRegistry;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Command for displaying help information.
 * 
 * @author Hexett
 */
public class HelpCommand extends BaseCommand {

    public HelpCommand() {
        super(
            null, // No permission required for help
            "/help [page]",
            "Display help information for available commands",
            false,
            0
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        displayHelp(sender, page);
        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Tab complete page numbers
            completions.add("1");
            completions.add("2");
            completions.add("3");
        }
        
        return completions;
    }

    /**
     * Display help information for the specified page.
     * 
     * @param sender The command sender
     * @param page The page number to display
     */
    private void displayHelp(CommandSender sender, int page) {
        Map<String, BaseCommand> commands = CommandRegistry.getAllCommands();
        List<BaseCommand> availableCommands = new ArrayList<>();
        
        // Filter commands by permission
        for (BaseCommand command : commands.values()) {
            if (command.getPermission() == null || sender.hasPermission(command.getPermission())) {
                availableCommands.add(command);
            }
        }
        
        if (availableCommands.isEmpty()) {
            sendMessage(sender, MessagesConfig.get("help.no-commands"));
            return;
        }
        
        int commandsPerPage = 8;
        int totalPages = (int) Math.ceil((double) availableCommands.size() / commandsPerPage);
        
        if (page > totalPages) {
            page = totalPages;
        }
        
        // Display header
        sendMessage(sender, "&6&l=== StaffUtilsPlus Help ===");
        sendMessage(sender, "&7Page &e" + page + "&7 of &e" + totalPages);
        sendMessage(sender, "");
        
        // Display commands for this page
        int startIndex = (page - 1) * commandsPerPage;
        int endIndex = Math.min(startIndex + commandsPerPage, availableCommands.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            BaseCommand command = availableCommands.get(i);
            displayCommandHelp(sender, command);
        }
        
        // Display navigation
        if (totalPages > 1) {
            sendMessage(sender, "");
            if (page > 1) {
                sendMessage(sender, "&7Use &e/help " + (page - 1) + "&7 for previous page");
            }
            if (page < totalPages) {
                sendMessage(sender, "&7Use &e/help " + (page + 1) + "&7 for next page");
            }
        }
        
        sendMessage(sender, "&6&l========================");
    }

    /**
     * Display help information for a single command.
     * 
     * @param sender The command sender
     * @param command The command to display help for
     */
    private void displayCommandHelp(CommandSender sender, BaseCommand command) {
        StringBuilder line = new StringBuilder();
        
        // Command usage
        line.append("&e").append(command.getUsage());
        
        // Description
        if (command.getDescription() != null && !command.getDescription().isEmpty()) {
            line.append(" &7- ").append(command.getDescription());
        }
        
        // Permission requirement
        if (command.getPermission() != null && !command.getPermission().isEmpty()) {
            line.append(" &8(&c").append(command.getPermission()).append("&8)");
        }
        
        sendMessage(sender, line.toString());
    }
}
