package me.hexett.staffUtilsPlus.impl;

import me.hexett.staffUtilsPlus.StaffUtilsPlus;
import me.hexett.staffUtilsPlus.service.vanish.HideItems;
import me.hexett.staffUtilsPlus.service.vanish.VanishService;
import me.hexett.staffUtilsPlus.utils.ColorUtils;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class VanishServiceImpl implements VanishService {

    private final Plugin plugin;
    private final HideItems hideItems;

    public VanishServiceImpl() {
        this.plugin = StaffUtilsPlus.getInstance();
        this.hideItems = new HideItems();
    }

    @Override
    public void vanish(Player player) {
        vanished.add(player.getUniqueId());

        if(plugin.getConfig().getBoolean("vanish-fake-messages")) {
            if (MessagesConfig.get("vanish.leave-message") == null) {
                Bukkit.broadcastMessage(ColorUtils.translateColorCodes("&e" + player.getName() + " &ehas left the server."));
            } else {
                Map<String, String> placeholder = new HashMap<>();
                placeholder.put("player", player.getName());
                Bukkit.broadcastMessage(MessagesConfig.get("vanish.leave-message"));
            }
        }

        for(Player target : Bukkit.getOnlinePlayers()) {
            if(!target.equals(player) && !target.hasPermission("staffutils.vanish")) {
                target.hidePlayer(plugin, player);
            }
        }

        player.setSilent(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        List<Player> viewers = Bukkit.getServer().getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .collect(Collectors.toList());
        hideItems.hideEquipment(player, viewers);

        player.sendMessage(MessagesConfig.get("vanish.success"));
    }

    @Override
    public void unVanish(Player player) {
        if(!isVanished(player)) return;
        vanished.remove(player.getUniqueId());

        if(plugin.getConfig().getBoolean("vanish-fake-messages")) {
            if (MessagesConfig.get("vanish.join-message") == null) {
                Bukkit.broadcastMessage(ColorUtils.translateColorCodes("&e" + player.getName() + " &ehas joined the server."));
            } else {
                Map<String, String> placeholder = new HashMap<>();
                placeholder.put("player", player.getName());
                Bukkit.broadcastMessage(MessagesConfig.get("vanish.join-message"));
            }
        }

        for(Player target : Bukkit.getOnlinePlayers()) {
            target.showPlayer(plugin, player);
        }

        player.setSilent(false);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removeMetadata("vanished", plugin);

        player.sendMessage(MessagesConfig.get("vanish.unvanish-success"));
    }

    @Override
    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public Set<UUID> getVanishedPlayers() {
        return vanished;
    }
}
