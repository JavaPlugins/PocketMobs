package dev.lone.pocketmobs;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import dev.lone.LoneLibs.nbt.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Random;

public class Utils
{
    public static Random random = new Random();

    public static int getRandomInt(int min, int max)
    {
        return random.nextInt((max - min) + 1) + min;
    }

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public static String convertColor(String name)
    {
        return name.replace("&", "\u00a7");
    }

    public static int parseInt(String number, int defaultValue)
    {
        try
        {
            return Integer.parseInt(number);
        }
        catch (Exception ignored) { }
        return defaultValue;
    }

    public static void hideEntity(Entity entity)
    {
        int id = entity.getEntityId();
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        if(MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_17_R1.getVersionId())
            packet.getIntLists().write(0, Collections.singletonList(id));
        else if(MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_16_R1.getVersionId())
            packet.getIntegerArrays().write(0, new int[] {id});
        else //old
            packet.getIntegers().write(0, id);

        Bukkit.getOnlinePlayers().forEach(player -> sendPacket(player, packet));
    }

    public static void sendPacket(Player receiver, PacketContainer container)
    {
        ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, container);
    }

    public static Location getBlockCenter(Block block)
    {
        Location loc = block.getLocation();
        loc.setX(loc.getX() + 0.5f);
        loc.setY(loc.getY());
        loc.setZ(loc.getZ() + 0.5f);
        return loc;
    }
}
