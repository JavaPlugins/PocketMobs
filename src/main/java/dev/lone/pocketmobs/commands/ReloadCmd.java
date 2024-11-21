package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.Settings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadCmd
{
    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (player.hasPermission("pocketmob.admin.reload"))
        {
            Settings.config.reloadFromFile();
            Settings.lang.reloadFromFile();
            Main.inst.ballsManager.reload();
            player.sendMessage(Settings.lang.getColored("reloaded"));
        }
        else
        {
            player.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + "pocketmob.admin.reload");
        }
        return true;
    }
}
