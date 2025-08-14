package me.hexett.staffUtilsPlus.commands.staff;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.vanish.VanishService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand extends BaseCommand {

    public VanishCommand() {
        super(
                "staffutils.vanish",
                "/vanish",
                "Puts yourself in vanish",
                true,
                0
        );
    }

    VanishService vanishService = ServiceRegistry.get(VanishService.class);

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        if(vanishService.isVanished(p)) {
            vanishService.unVanish(p);
        } else {
            vanishService.vanish(p);
        }
        return true;
    }
}
