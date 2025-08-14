package me.hexett.staffUtilsPlus.commands.staff;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.PunishmentService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.List;

public class WhoIsCommand extends BaseCommand {

    public WhoIsCommand() {
        super(
                "staffutils.whois",
                "/whois <player>",
                "",
                false,
                1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        sender.sendMessage(ChatColor.YELLOW + "=== Whois: " + target.getName() + " ===");

        sender.sendMessage(ChatColor.GRAY + "UUID: " + target.getUniqueId());

        if (target.isOnline()) {
            Player onlineTarget = (Player) target;
            if(sender.hasPermission("staffutils.whois.ip")) {
                sender.sendMessage(ChatColor.GRAY + "IP: " + onlineTarget.getAddress().getAddress().getHostAddress());
            }
            sender.sendMessage(ChatColor.GRAY + "World: " + onlineTarget.getWorld().getName());
            sender.sendMessage(ChatColor.GRAY + "Location: X=" + onlineTarget.getLocation().getBlockX() +
                    " Y=" + onlineTarget.getLocation().getBlockY() +
                    " Z=" + onlineTarget.getLocation().getBlockZ());
            sender.sendMessage(ChatColor.GRAY + "Health: " + Math.round(((Player) target).getHealth()));
            sender.sendMessage(ChatColor.GRAY + "Gamemode: " + onlineTarget.getGameMode());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sender.sendMessage(ChatColor.GRAY + "First joined: " + sdf.format(target.getFirstPlayed()));
        if (target.getLastPlayed() > 0) {
            sender.sendMessage(ChatColor.GRAY + "Last seen: " + sdf.format(target.getLastPlayed()));
        }
        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

}
