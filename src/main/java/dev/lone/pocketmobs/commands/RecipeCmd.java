package dev.lone.pocketmobs.commands;

import dev.lone.LoneLibs.Events;
import dev.lone.pocketmobs.Settings;
import dev.lone.pocketmobs.data.Ball;
import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.utils.Mat;
import dev.lone.pocketmobs.utils.ActionBar;
import dev.lone.pocketmobs.utils.InvUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class RecipeCmd implements Listener
{
    HashMap<Player, Ball> buying = new HashMap<>();

    public RecipeCmd()
    {
        Events.register(Main.inst, this);
    }

    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if(!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        if (player.hasPermission("pocketmob.user.recipes"))
            showRecipesView(player);
        else
            player.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + "pocketmob.user.recipes");

        return true;
    }

    public static void showRecipesView(Player player)
    {
        Inventory gui = Bukkit.createInventory(null, 9*6, Settings.lang.getColored("balls"));
        for(Ball entry : Main.inst.ballsManager.balls)
            gui.addItem(entry.getItemStack());
        player.openInventory(gui);
    }

    public static void showConfirmGUI(Player player)
    {
        Inventory gui = Bukkit.createInventory(null, 9*6, Settings.lang.getColored("confirm-buy"));

        InvUtil.createDisplay(new ItemStack(Mat.GREEN_STAINED_GLASS_PANE.getMaterial()), gui, 3 + 9 * 2,
                                 Settings.lang.getColored("confirm"), "");

        InvUtil.createDisplay(new ItemStack(Mat.RED_STAINED_GLASS_PANE.getMaterial()), gui, 5 + 9 * 2,
                                 Settings.lang.getColored("cancel"), "");

         player.openInventory(gui);
    }


    @EventHandler
    public void recipeGUIClick(InventoryClickEvent e)
    {
        if(e.getInventory().getType() != InventoryType.CHEST)
            return;
        if (!e.getView().getTitle().equalsIgnoreCase(Settings.lang.getColored("balls")))
            return;
        if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;

        e.setCancelled(true);

        if(!InvUtil.hasClickedTop(e))
            return;

        Player player = (Player)e.getWhoClicked();
        Inventory recipeGUI = Bukkit.createInventory(null, 9*6, Settings.lang.getColored("recipe"));

        if(e.getCurrentItem()!=null)
        {
            Ball ballConfig = Main.inst.ballsManager.byItemStack(e.getCurrentItem());


            for(int i=0;i<recipeGUI.getSize();i++)
                InvUtil.createDisplay(new ItemStack(Mat.LIGHT_GRAY_STAINED_GLASS_PANE.getMaterial()), recipeGUI, i,
                                         " ", "");
            List<ItemStack> items = Main.inst.ballsManager.getRecipeItems(e.getCurrentItem());
            if(items == null)
            {
                ItemStack black = new ItemStack(Mat.BLACK_STAINED_GLASS_PANE.getMaterial());
                InvUtil.createDisplay(black, recipeGUI, 1 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
                InvUtil.createDisplay(black, recipeGUI, 2 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
                InvUtil.createDisplay(black, recipeGUI, 3 + 9 * 1, Settings.lang.getColored("no-recipe"), "");

                InvUtil.createDisplay(black, recipeGUI, 10 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
                InvUtil.createDisplay(black, recipeGUI, 11 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
                InvUtil.createDisplay(black, recipeGUI, 12 + 9 * 1, Settings.lang.getColored("no-recipe"), "");

                InvUtil.createDisplay(black, recipeGUI, 19 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
                InvUtil.createDisplay(black, recipeGUI, 20 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
                InvUtil.createDisplay(black, recipeGUI, 21 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
            }
            else
            {
                recipeGUI.setItem(1 + 9 * 1, items.get(0));
                recipeGUI.setItem(2 + 9 * 1, items.get(1));
                recipeGUI.setItem(3 + 9 * 1, items.get(2));

                recipeGUI.setItem(10 + 9 * 1, items.get(3));
                recipeGUI.setItem(11 + 9 * 1, items.get(4));
                recipeGUI.setItem(12 + 9 * 1, items.get(5));

                recipeGUI.setItem(19 + 9 * 1, items.get(6));
                recipeGUI.setItem(20 + 9 * 1, items.get(7));
                recipeGUI.setItem(21 + 9 * 1, items.get(8));
            }

            recipeGUI.setItem(16+9*1, e.getCurrentItem());

            if(ballConfig.buyPrice != -1 && player.hasPermission("pocketmob.user.buy." + ballConfig.name))
            {
                InvUtil.createDisplay(new ItemStack(Material.EMERALD), recipeGUI, 49,
                                         Settings.lang.getColored("buy"),
                                         Settings.lang.getColored("buy-lore")
                                                 .replace("{price}", ballConfig.buyPrice + "")
                                                 .replace("{amount}", ballConfig.buyAmount + "")
                );
                buying.put(player, ballConfig);
            }

            InvUtil.createDisplay(new ItemStack(Material.SPIDER_EYE), recipeGUI, 53,
                                      Settings.lang.getColored("back"), "");

            player.openInventory(recipeGUI);
        }
    }

    @EventHandler
    public void recipeGUIClick_preview(InventoryClickEvent e)
    {
        if(e.getInventory().getType() != InventoryType.CHEST || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;
        if (!e.getView().getTitle().equalsIgnoreCase(Settings.lang.getColored("recipe")))
            return;

        e.setCancelled(true);


        if(!e.getCurrentItem().hasItemMeta())
            return;

        if(matchGUIIconName(e.getCurrentItem(), "back"))
            showRecipesView((Player)e.getWhoClicked());
        else if(matchGUIIconName(e.getCurrentItem(), "buy"))
            showConfirmGUI((Player)e.getWhoClicked());

    }

    @EventHandler
    public void buyClick(InventoryClickEvent e)
    {
        if(e.getInventory().getType() != InventoryType.CHEST || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;
        if (!e.getView().getTitle().equalsIgnoreCase(Settings.lang.getColored("confirm-buy")))
            return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();


        if(matchGUIIconName(e.getCurrentItem(), "confirm"))
        {
            Ball ballConfig = buying.get(player);
            if(Main.inst.economy.getBalance(player) - ballConfig.buyPrice >= 0)
            {
                Main.inst.economy.withdrawPlayer(player, ballConfig.buyPrice);
                ActionBar.send(player, Settings.lang.getColored("successfully-bought").replace("{name}", ballConfig.displayName));

                ItemStack toGive = ballConfig.getItemStack().clone();
                toGive.setAmount(ballConfig.buyAmount);

                InvUtil.giveItem(player, toGive);
            }
            else
            {
                ActionBar.send(player, Settings.lang.getColored("insufficient-money"));
            }
            player.closeInventory();
        }
        else if(matchGUIIconName(e.getCurrentItem(), "cancel"))
            showRecipesView(player);

    }


    boolean matchGUIIconName(ItemStack itemStack, String configStr)
    {
        return(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName())
                .equals(ChatColor.stripColor(Settings.lang.getColored(configStr)))
        );

    }
}
