package me.hexett.staffUtilsPlus.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.hexett.staffUtilsPlus.StaffUtilsPlus;
import me.hexett.staffUtilsPlus.service.vanish.HideItems;
import me.hexett.staffUtilsPlus.service.vanish.VanishService;
import me.hexett.staffUtilsPlus.utils.ColorUtils;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
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
                Bukkit.broadcastMessage(MessagesConfig.get("vanish.leave-message").replace("player%", player.getName()));
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
        showMessageTask(player);
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
                Bukkit.broadcastMessage(MessagesConfig.get("vanish.join-message").replace("player", player.getName()));
            }
        }

        for(Player target : Bukkit.getOnlinePlayers()) {
            target.showPlayer(plugin, player);
        }

        player.setSilent(false);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removeMetadata("vanished", plugin);

        Integer taskId = vanishTasks.remove(player.getUniqueId());
        if (taskId != null) Bukkit.getScheduler().cancelTask(taskId);

        player.sendMessage(MessagesConfig.get("vanish.unvanish-success"));

    }

    @Override
    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public Set<UUID> getVanishedPlayers() {
        return vanished;
    }

    public void showMessageTask(Player player) {

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline() && isVanished(player)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(ColorUtils.translateColorCodes("&cVanished &8(&a&l✓&r&8) &cTPS: &a" + plugin.getServer().getServerTickManager().getTickRate()
                            )));
                }
            }
        }, 0L, 20L).getTaskId();

        vanishTasks.put(player.getUniqueId(), taskId);
    }


}
