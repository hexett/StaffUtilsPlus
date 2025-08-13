package me.hexett.staffUtilsPlus.commands.punish;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import me.hexett.staffUtilsPlus.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command for unmuting players.
 * 
 * @author Hexett
 */
public class UnmuteCommand extends BaseCommand {

    public UnmuteCommand() {
        super(
            "staffutils.unmute",
            "/unmute <player>",
            "Unmute a player on the server",
            false,
            1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        String targetName = args[0];

        // Get target UUID
        UUID targetUUID = PlayerUtils.getPlayerUUID(targetName);
        if (targetUUID == null) {
            sendMessage(sender, MessagesConfig.get("errors.player-not-found").replace("%player%", targetName));
            return true;
        }

        // Check if player is actually muted
        PunishmentService punishmentService = ServiceRegistry.get(PunishmentService.class);
        if (!punishmentService.isMuted(targetUUID)) {
            sendMessage(sender, MessagesConfig.get("punishments.unmute.not-muted").replace("%target%", targetName));
            return true;
        }

        // Get issuer UUID
        UUID issuerUUID = null;
        if (sender instanceof Player) {
            issuerUUID = ((Player) sender).getUniqueId();
        }

        // Execute the unmute
        punishmentService.unmute(issuerUUID, targetUUID);

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Tab complete muted player names
            String partialName = args[0].toLowerCase();
            
            // This is a simplified tab completion - in a real implementation,
            // you might want to query the database for muted players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }


}
