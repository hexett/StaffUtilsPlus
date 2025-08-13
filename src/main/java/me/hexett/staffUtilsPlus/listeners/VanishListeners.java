package me.hexett.staffUtilsPlus.listeners;

import me.hexett.staffUtilsPlus.StaffUtilsPlus;
import me.hexett.staffUtilsPlus.impl.VanishServiceImpl;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.vanish.HideItems;
import me.hexett.staffUtilsPlus.service.vanish.VanishService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VanishListeners implements Listener {

    VanishService vanishService = ServiceRegistry.get(VanishService.class);
    Set<UUID> vanished = vanishService.getVanished();
    Plugin plugin = StaffUtilsPlus.getInstance();
    HideItems hideItems = new HideItems();


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();
        for(UUID uuid : vanished) {
            Player vanishedPlayer = Bukkit.getPlayer(uuid);
            if(vanishedPlayer != null && !joining.hasPermission("staffutils.vanish")) {
                joining.hidePlayer(plugin, vanishedPlayer);
                hideItems.hideEquipment(vanishedPlayer, List.of(joining));
            }
        }
    }

    @EventHandler
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if(event.getTarget() instanceof Player target) {
            if(target.hasMetadata("vanished")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if(vanishService.isVanished(event.getPlayer())) {
            event.setQuitMessage(null);
        }
    }

}
