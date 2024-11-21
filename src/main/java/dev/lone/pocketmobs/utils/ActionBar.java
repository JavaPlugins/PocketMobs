package dev.lone.pocketmobs.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ActionBar
{
    @SuppressWarnings("deprecation")
    public static void send(Player player, String text)
    {
        if (player == null || text == null)
            return;

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
    }
}
