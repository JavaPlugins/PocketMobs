package dev.lone.pocketmobs.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;

public class HologramUtil
{
    static void spawn(Location location, String message, int duration)
    {
        location.getWorld().spawn(location, AreaEffectCloud.class, newEntity -> {
            newEntity.setParticle(Particle.TOWN_AURA);
            newEntity.setRadius(0.0F);

            newEntity.setCustomName(message);
            newEntity.setCustomNameVisible(true);

            newEntity.setWaitTime(0);
            newEntity.setDuration(duration);
        });
    }

    public static void send(Player player, String message, int duration)
    {
        spawn(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(2)).add(0, -1.0, 0), message, duration);
    }
}
