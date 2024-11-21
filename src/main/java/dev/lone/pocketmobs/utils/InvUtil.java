package dev.lone.pocketmobs.utils;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InvUtil
{
    public static boolean hasClickedTop(InventoryClickEvent event)
    {
        return event.getRawSlot() < event.getView().getTopInventory().getSize();
    }

    public static void setItemStackLore(ItemStack item, List<String> lore)
    {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static void decrementAmountMainHand(Player player)
    {
        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR || player.getInventory().getItemInMainHand().getAmount() <= 0 || player.getInventory().getItemInMainHand().getAmount() - 1 < 0)
            return;

        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
    }

    public static ItemStack decrementAmount(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0 || item.getAmount() - 1 < 0)
            return item;
        item.setAmount(item.getAmount() - 1);
        return item;
    }

    public static void giveItem(Player player, ItemStack itemStack)
    {
        player.getInventory().addItem(itemStack).forEach((index, overflow) -> {
            Item item = player.getWorld().dropItem(player.getLocation(), overflow);
            try
            {
                item.setOwner(player.getUniqueId());
            }
            catch (NoSuchMethodError e) { }
            item.setPickupDelay(0);
        });
    }

    public static void createDisplay(ItemStack itemStack, Inventory inventory, int slot, String name, String lore)
    {
        ItemStack item = itemStack.clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        ArrayList<String> Lore = new ArrayList<>();
        Lore.add(lore);
        meta.setLore(Lore);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }
}

