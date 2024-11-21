package dev.lone.pocketmobs.utils;

import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class EntityUtil
{
    // Reimplement this using NMS to access the translated text.
    @Deprecated
    public static String getReadableEntityTypeName(EntityType type)
    {
        return WordUtils.capitalizeFully(type.toString()).replace("_", " ");
    }

    public static String getReadableEntityTypeName(Entity entity, boolean useCustomNameIfSet)
    {
        if(useCustomNameIfSet && entity.getCustomName() != null)
        {
            // Mcmmo and other health bar plugins use hearts to display health.
            if(!entity.getCustomName().contains("\u2665") && !entity.getCustomName().contains("\u2764"))
                return entity.getCustomName();
        }
        return WordUtils.capitalizeFully(entity.getType().toString()).replace("_", " ");
    }
}
