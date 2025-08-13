package me.hexett.staffUtilsPlus.service.vanish;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import me.hexett.staffUtilsPlus.StaffUtilsPlus;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HideItems {

    StaffUtilsPlus plugin = StaffUtilsPlus.getInstance();

    ProtocolManager protocolManager = plugin.getProtocolManager();

    public void hideEquipment(Player target, List<Player> viewers) {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        packet.getIntegers().write(0, target.getEntityId());
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> emptyEquipment = List.of(
                new Pair<>(EnumWrappers.ItemSlot.HEAD, null),
                new Pair<>(EnumWrappers.ItemSlot.CHEST, null),
                new Pair<>(EnumWrappers.ItemSlot.LEGS, null),
                new Pair<>(EnumWrappers.ItemSlot.FEET, null),
                new Pair<>(EnumWrappers.ItemSlot.MAINHAND, null),
                new Pair<>(EnumWrappers.ItemSlot.OFFHAND, null)
        );
        packet.getSlotStackPairLists().write(0, emptyEquipment);

        for(Player viewer : viewers) {
            if(!viewer.equals(target)) {
                try {
                    protocolManager.sendServerPacket(viewer, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
