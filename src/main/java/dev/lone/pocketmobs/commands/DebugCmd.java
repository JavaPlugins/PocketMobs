package dev.lone.pocketmobs.commands;

import dev.lone.LoneLibs.nbt.nbtapi.NBTItem;
import dev.lone.pocketmobs.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DebugCmd
{
    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (player.hasPermission("pocketmob.admin.debug"))
        {
            NBTItem nbtItem = new NBTItem(player.getItemInHand());
            Bukkit.getConsoleSender().sendMessage(nbtItem.toString());
        }
        else
        {
            player.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + "pocketmob.admin.debug");
        }
        return true;
    }
}
