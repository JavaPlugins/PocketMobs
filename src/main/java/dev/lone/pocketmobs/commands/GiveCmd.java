package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Settings;
import dev.lone.pocketmobs.Utils;
import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.utils.InvUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GiveCmd
{
    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender.hasPermission("pocketmob.admin.give"))
        {
            if (args.length > 2)
            {
                if (!Main.inst.ballsManager.exists(args[2]))
                {
                    sender.sendMessage(Settings.lang.getColored("item-not-found").replace("{item}", args[1]));
                    return true;
                }

                ItemStack item = Main.inst.ballsManager.getOriginalItemStack(args[2]).clone();


                int amount = 1;
                if (args.length == 4)
                    amount = Utils.parseInt(args[3], 1);

                Player playerToGive = Bukkit.getPlayer(args[1]);
                if (playerToGive == null)
                {
                    sender.sendMessage(Settings.lang.getColored("offline-player"));
                    return true;
                }

                sender.sendMessage(Settings.lang.getColored("given-item")
                        .replace("{item}", args[1])
                        .replace("{player}", playerToGive.getDisplayName())
                );

                for (int i = 0; i < amount; i++)
                {
                    item = Main.inst.ballsManager.getOriginalItemStack(args[2]).clone();
                    InvUtil.giveItem(playerToGive, item);
                }
                playerToGive.sendMessage(Settings.lang.getColored("obtained") + item.getItemMeta().getDisplayName());
            }
            else
            {

                sender.sendMessage(Settings.lang.getColored("wrong-command-usage") + ChatColor.AQUA + "/pocketmob give <player> <ball>");
                return true;
            }
        }
        else
        {
            sender.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + "pocketmob.admin.give");
        }
        return true;
    }
}
