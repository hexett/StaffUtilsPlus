package me.hexett.staffUtilsPlus.manager;

import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.punishments.Punishment;
import me.hexett.staffUtilsPlus.impl.PunishmentServiceImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.List;


public class PunishmentManager {

    private final Plugin plugin;
    private final PunishmentServiceImpl punishmentService;

    public PunishmentManager(Plugin plugin) {
        this.plugin = plugin;
        this.punishmentService = ServiceRegistry.get(PunishmentServiceImpl.class);
        startAutoUnbanTask();
    }

    private void startAutoUnbanTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            List<Punishment> activeTempBans = punishmentService.getActiveTempBans();

            long now = System.currentTimeMillis();

            for (Punishment p : activeTempBans) {
                if (!p.isPermanent() && p.getExpiresAt() <= now) {
                    punishmentService.unban(null, p.getTarget());
                }
            }
        }, 0L, 20L * 60L); // every 60 seconds
    }

}
