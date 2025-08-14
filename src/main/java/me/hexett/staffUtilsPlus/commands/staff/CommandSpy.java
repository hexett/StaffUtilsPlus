package me.hexett.staffUtilsPlus.commands.staff;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.listeners.CommandSpyListeners;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpy extends BaseCommand {

    private final CommandSpyListeners spy;

    public CommandSpy(CommandSpyListeners spy) {
        super(
                "staffutils.commandspy",
                "/commandspy",
                "Allows you to see the commands a player runs",
                true,
                0
        );
        this.spy = spy;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        spy.toggle(player);
        return true;
    }
}
