package me.hexett.staffUtilsPlus.service.vanish;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface VanishService {

    Set<UUID> vanished = new HashSet<>();

    void vanish(Player player);
    void unVanish(Player player);

    boolean isVanished(Player player);

    default Set<UUID> getVanished() {
        return vanished;
    }

}
