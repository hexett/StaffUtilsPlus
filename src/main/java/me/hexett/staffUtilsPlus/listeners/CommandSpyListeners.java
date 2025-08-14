package me.hexett.staffUtilsPlus.listeners;

import me.hexett.staffUtilsPlus.utils.ColorUtils;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CommandSpyListeners implements Listener {

    private final Set<UUID> enabledPlayers = new HashSet<>();

    public boolean toggle(Player player) {
        if (enabledPlayers.contains(player.getUniqueId())) {
            enabledPlayers.remove(player.getUniqueId());
            player.sendMessage(ColorUtils.translateColorCodes(
                    MessagesConfig.get("commandspy.disable")
                            .replace("%player%", player.getDisplayName())));
            return false;
        } else {
            enabledPlayers.add(player.getUniqueId());
            player.sendMessage(ColorUtils.translateColorCodes(
                    MessagesConfig.get("commandspy.enable")
                            .replace("%player%", player.getDisplayName())));
            return true;
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player sender = event.getPlayer();

        for (UUID uuid : enabledPlayers) {
            Player spy = Bukkit.getPlayer(uuid);
            if (spy != null && spy != sender && spy.hasPermission("staffutils.commandspy")) {
                spy.sendMessage(ColorUtils.translateColorCodes(
                        MessagesConfig.get("commandspy.message")
                                .replace("%sender%", sender.getName())
                                .replace("%command%", command)
                ));
            }
        }
    }

}
