package me.hexett.staffUtilsPlus.commands;

import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for unbanning IP addresses.
 * 
 * @author Hexett
 */
public class UnbanIPCommand extends BaseCommand {

    public UnbanIPCommand() {
        super(
            "staffutils.unbanip",
            "/unbanip <ip-address>",
            "Unban an IP address from the server",
            false,
            1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        String ipAddress = args[0];

        // Validate IP address format
        if (!isValidIPAddress(ipAddress)) {
            sendMessage(sender, MessagesConfig.get("errors.invalid-ip-address").replace("%ip%", ipAddress));
            return true;
        }

        // Check if IP is actually banned
        PunishmentService punishmentService = ServiceRegistry.get(PunishmentService.class);
        if (!punishmentService.isIPBanned(ipAddress)) {
            sendMessage(sender, MessagesConfig.get("punishments.unbanip.not-banned").replace("%ip%", ipAddress));
            return true;
        }

        // Get issuer UUID
        java.util.UUID issuerUUID = null;
        if (sender instanceof org.bukkit.entity.Player) {
            issuerUUID = ((org.bukkit.entity.Player) sender).getUniqueId();
        }

        // Execute the IP unban
        punishmentService.unbanIP(issuerUUID, ipAddress);

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Tab complete banned IP addresses
            // In a real implementation, you might want to query the database
            // for banned IP addresses to provide better tab completion
            String partialIP = args[0].toLowerCase();
            
            // This is a simplified tab completion - in a real implementation,
            // you might want to query the database for banned IP addresses
            if (partialIP.isEmpty() || partialIP.startsWith("192.168.")) {
                completions.add("192.168.1.1");
                completions.add("192.168.1.100");
            }
        }

        return completions;
    }

    /**
     * Validate if a string is a valid IP address.
     * 
     * @param ipAddress The string to validate
     * @return true if valid IP address, false otherwise
     */
    private boolean isValidIPAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
