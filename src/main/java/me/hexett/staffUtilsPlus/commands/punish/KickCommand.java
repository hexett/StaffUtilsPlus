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

public class KickCommand extends BaseCommand {

    public KickCommand() {
        super(
                "staffutils.kick",
                "/kick <player> [reason]",
                "Kick a player from the server",
                false,
                1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        String targetName = args[0];
        String reason = args.length > 1 ? args[1] : MessagesConfig.get("punishments.kick.default-reason");

        // Get target UUID
        UUID targetUUID = PlayerUtils.getPlayerUUID(targetName);
        if (targetUUID == null) {
            sendMessage(sender, MessagesConfig.get("errors.player-not-found").replace("%player%", targetName));
            return true;
        }
        PunishmentService punishmentService = ServiceRegistry.get(PunishmentService.class);

        // Get issuer UUID
        UUID issuerUUID = null;
        if (sender instanceof Player) {
            issuerUUID = ((Player) sender).getUniqueId();
        }

        // Execute the ban
        punishmentService.kick(issuerUUID, targetUUID, reason);

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Tab complete player names
            String partialName = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            // Tab complete common reasons
            completions.add("Auto-rejoin-test");
            completions.add("AutoClicking");
            completions.add("Spam");
        }
        return completions;
    }
}
