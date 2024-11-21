package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter
{
    GetCmd getCommand = new GetCmd();
    GiveCmd giveCommand = new GiveCmd();
    DebugCmd debugCommand = new DebugCmd();
    RecipeCmd recipeCommand = new RecipeCmd();
    ReloadCmd reloadCmd = new ReloadCmd();

    public MainCommand()
    {
        Main.inst.getCommand("pocketmob").setExecutor(this);
        Bukkit.getPluginCommand("pocketmob").setTabCompleter(this);
    }

    public static List<String> getAutocompletedWorldsArgs(String partialWord)
    {
        List<String> list = new ArrayList<>();
        if("get".contains(partialWord))
            list.add("get");
        if("debug".contains(partialWord))
            list.add("debug");
        if("recipes".contains(partialWord))
            list.add("recipes");
        if("give".contains(partialWord))
            list.add("give");
        if("reload".contains(partialWord))
            list.add("reload");
        return list;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if(args.length == 0)
        {
            sender.sendMessage(Settings.lang.getColored("wrong-command-usage") + ChatColor.AQUA + " /pocketmob <get|recipes|debug>");
            return true;
        }

        if(args[0].equalsIgnoreCase("get"))
        {
            getCommand.handle(sender, command, label, args);
            return true;
        }
        else if(args[0].equalsIgnoreCase("give"))
        {
            giveCommand.handle(sender, command, label, args);
            return true;
        }
        else if(args[0].equalsIgnoreCase("debug"))
        {
            debugCommand.handle(sender, command, label, args);
            return true;
        }
        else if(args[0].equalsIgnoreCase("recipes"))
        {
            recipeCommand.handle(sender, command, label, args);
            return true;
        }
        else if(args[0].equalsIgnoreCase("reload"))
        {
            reloadCmd.handle(sender, command, label, args);
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){

        if(args.length <= 1)
        {
            String partialWorld = "";
            if(args.length > 0)
                partialWorld = args[0];
            return getAutocompletedWorldsArgs(partialWorld);
        }

        if(args[0].equalsIgnoreCase("get"))
        {
            if(args.length > 2)
            {
                return Arrays.asList("1", "5", "15", "16", "32", "64");
            }
            else
            {
                List<String> names = Main.inst.ballsManager.getKeys(args[1]);
                if(!names.isEmpty())
                    return names;
                else
                    return Arrays.asList(ChatColor.RED + "Item not found!");
            }
        }
        else if(args[0].equalsIgnoreCase("give"))
        {
            if(args.length == 2)
            {
                List<String> players = new ArrayList<>();
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    players.add(online.getName());
                }
                return players;
            }
            else if(args.length == 3)
            {
                List<String> names = Main.inst.ballsManager.getKeys(args[2]);
                if (!names.isEmpty())
                    return names;
                else
                    return Arrays.asList(ChatColor.RED + "Item not found!");
            }
            else if(args.length == 4)
            {
                return Arrays.asList("1", "5", "15", "16", "32", "64");
            }
        }

        return Arrays.asList("");
    }
}
