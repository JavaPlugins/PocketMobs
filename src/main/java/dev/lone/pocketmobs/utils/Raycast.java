package dev.lone.pocketmobs.utils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

public class Raycast
{
    private Player player;
    private final Location location;
    private final double distance;
    private Entity hitEntity;
    private Block hitBlock;
    private Location hitLocation;
    @Nullable
    private BlockFace hitFace;

    private FluidCollisionMode spigotFluidCollisionMode = FluidCollisionMode.NEVER;

    public Raycast(final Location location, final double distance)
    {
        this.location = location;
        this.distance = distance;
    }

    public Raycast(final Player player, final double distance)
    {
        this(player.getEyeLocation(), distance);
        this.player = player;
    }

    public void setSpigotFluidCollisionMode(FluidCollisionMode spigotFluidCollisionMode)
    {
        this.spigotFluidCollisionMode = spigotFluidCollisionMode;
    }

    private RayTraceResult spigotRaytraceBlocks()
    {
        return location.getWorld().rayTraceBlocks(
                location,
                location.getDirection(),
                distance,
                spigotFluidCollisionMode,
                //skipPassableBlocks
                true
        );
    }

    private RayTraceResult spigotRaytraceEntities()
    {
        if (player == null)
            return location.getWorld().rayTraceEntities(location, location.getDirection(), distance);
        return location.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                distance,
                0.0,
                entity -> player != null && entity.getEntityId() != player.getEntityId()
        );
    }

    private RayTraceResult spigotRaytraceAll()
    {
        if (player == null)
        {
            return location.getWorld().rayTrace(
                    location,
                    location.getDirection(),
                    distance,
                    spigotFluidCollisionMode,
                    true,
                    0.0d,
                    entity -> player != null && entity.getEntityId() != player.getEntityId()
            );
        }
        return location.getWorld().rayTrace(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                distance,
                spigotFluidCollisionMode,
                true,
                0.0d,
                entity -> player != null && entity.getEntityId() != player.getEntityId()
        );
    }

    public boolean compute(final RaycastType rayCastType)
    {
        RayTraceResult spigotRaytrace = null;
        switch (rayCastType)
        {
            case ENTITY_AND_BLOCK:
                spigotRaytrace = spigotRaytraceAll();
                break;
            case BLOCK:
                spigotRaytrace = spigotRaytraceBlocks();
                break;
            case ENTITY:
                spigotRaytrace = spigotRaytraceEntities();
                break;
        }

        if (spigotRaytrace != null)
        {
            hitBlock = null;
            hitEntity = spigotRaytrace.getHitEntity();
            hitBlock = spigotRaytrace.getHitBlock();
            hitLocation = new Location(location.getWorld(),
                    spigotRaytrace.getHitPosition().getX(),
                    spigotRaytrace.getHitPosition().getY(),
                    spigotRaytrace.getHitPosition().getZ()
            );
            hitFace = spigotRaytrace.getHitBlockFace();
            return true;
        }
        return false;
    }


    public Entity getHitEntity()
    {
        return this.hitEntity;
    }

    public boolean hasHitEntity()
    {
        return hitEntity != null;
    }

    public Block getHitBlock()
    {
        return this.hitBlock;
    }

    public boolean hasHitBlock()
    {
        return this.hitBlock != null;
    }

    public Location getHitLocation()
    {
        return this.hitLocation;
    }

    public boolean hasHitLocation()
    {
        return hitLocation != null;
    }

    public @Nullable BlockFace getHitFace()
    {
        return hitFace;
    }

    public enum RaycastType
    {
        ENTITY_AND_BLOCK("ENTITY_AND_BLOCK", 0),
        ENTITY("ENTITY", 1),
        BLOCK("BLOCK", 2);

        RaycastType(final String s, final int n)
        {
        }
    }
}
