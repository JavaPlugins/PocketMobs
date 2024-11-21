package dev.lone.pocketmobs;

import dev.lone.LoneLibs.nbt.nbtapi.NBTEntity;
import dev.lone.LoneLibs.nbt.nbtapi.NBTReflectionUtil;
import dev.lone.LoneLibs.nbt.nbtapi.utils.nmsmappings.ReflectionMethod;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class CaughtMob
{
    public EntityType type;

    //public HashMap<String, Object> properties = new HashMap<>();
    //Object nbtTagCompound;

    public String nbtTagCompound;

    @SuppressWarnings("deprecation")
    public CaughtMob(Entity mob)
    {
        type = mob.getType();
        NBTEntity nbtEntity = new NBTEntity(mob);
        nbtTagCompound = nbtEntity.asNBTString();
    }

    public double getLife()
    {
        Object nbt = ReflectionMethod.PARSE_NBT.run(null, nbtTagCompound);
        return (double) ReflectionMethod.COMPOUND_GET_DOUBLE.run(nbt, "Health");
    }

    public Entity spawnEntity(Location location, Item item)
    {
        Entity entity;
        Class<? extends Entity> clazz = type.getEntityClass();
        assert clazz != null;
        entity = location.getWorld().spawn(location, clazz, newEntity -> {

            Object nbt = ReflectionMethod.PARSE_NBT.run(null, nbtTagCompound);

            if (!Settings.config.getBoolean("keep-entity-uuid", false))
                ReflectionMethod.COMPOUND_REMOVE_KEY.run(nbt, "UUID");
            ReflectionMethod.COMPOUND_REMOVE_KEY.run(nbt, "UUIDMost");
            ReflectionMethod.COMPOUND_REMOVE_KEY.run(nbt, "UUIDLeast");
            ReflectionMethod.COMPOUND_REMOVE_KEY.run(nbt, "WorldUUIDMost");
            ReflectionMethod.COMPOUND_REMOVE_KEY.run(nbt, "WorldUUIDLeast");
            // TODO: I must test if mobs caught on previous versions of the game like 1.16 can be spawned on on 1.21+
            NBTReflectionUtil.setEntityNBTTag(nbt, NBTReflectionUtil.getNMSEntity(newEntity));

            newEntity.teleport(location);
            newEntity.setVelocity(new Vector(0, 0.1f, 0));

            newEntity.setMetadata("SpawnedWithPB", new FixedMetadataValue(Main.inst, true));

            BallsEventsListener.ballsByMob.put(newEntity.getUniqueId(), item);
        });

        return entity;
    }
}
