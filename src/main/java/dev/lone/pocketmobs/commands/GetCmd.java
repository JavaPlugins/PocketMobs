package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Settings;
import dev.lone.pocketmobs.Utils;
import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.utils.InvUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GetCmd
{
    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (player.hasPermission("pocketmob.admin.get"))
        {
            if (args.length > 1)
            {
                if (!Main.inst.ballsManager.exists(args[1]))
                {

                    player.sendMessage(Settings.lang.getColored("item-not-found").replace("{item}", args[1]));
                    return true;
                }

                ItemStack item = Main.inst.ballsManager.getOriginalItemStack(args[1]).clone();

                int amount = 1;
                if (args.length == 3)
                    amount = Utils.parseInt(args[2], 1);

                for (int i = 0; i < amount; i++)
                {
                    item = Main.inst.ballsManager.getOriginalItemStack(args[1]).clone();
                    InvUtil.giveItem(player, item);
                }

                player.sendMessage(Settings.lang.getColored("obtained") + item.getItemMeta().getDisplayName());
            }
            else
            {
                player.sendMessage(Settings.lang.getColored("wrong-command-usage") + ChatColor.AQUA + "/pocketmob get <ball>");
            }
        }
        else
        {
            player.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + "pocketmob.admin.get");
        }
        return true;
    }
}
