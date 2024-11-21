package dev.lone.pocketmobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class LocationUtil
{
    public static boolean canBreak(Block block, Player player)
    {
        BlockBreakEvent b = new BlockBreakEvent(block, player);
        Bukkit.getServer().getPluginManager().callEvent(b);
        boolean can = false;
        if (!b.isCancelled())
            can = true;
        b.setCancelled(true);
        return can;
    }
}
