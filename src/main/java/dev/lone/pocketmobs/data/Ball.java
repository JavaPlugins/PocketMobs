package dev.lone.pocketmobs.data;

import de.erethon.headlib.HeadLib;
import dev.lone.pocketmobs.CaughtMob;
import dev.lone.pocketmobs.Settings;
import dev.lone.pocketmobs.Utils;
import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.utils.EntityUtil;
import dev.lone.pocketmobs.utils.InvUtil;
import dev.lone.LoneLibs.nbt.nbtapi.NBTItem;
import dev.lone.LoneLibs.nbt.nbtapi.utils.GsonWrapper;
import dev.lone.LoneLibs.nbt.nbtapi.utils.MinecraftVersion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Ball
{
    public String name;
    public String displayName;
    public String headTexture;
    public List<String> catchableEntities;
    public List<String> blacklistedEntities;

    public int maxUsages = 1;
    public boolean unlimitedUsages;
    public double catchSuccessPercentage = 100;

    public double buyPrice = -1;
    public int buyAmount = -1;

    public BallEffect catchEffect;
    public BallEffect freeEffect;
    public BallEffect missedEffect;
    public BallEffect notSpawnedEffect;
    public ShapedRecipe recipe;

    public List<String> lore;

    public List<String> catchableMobsStringList;


    public Ball(String name, String headTexture)
    {
        this.name = name;
        this.headTexture = headTexture;
        this.catchableEntities = new ArrayList<>();
        this.blacklistedEntities = new ArrayList<>();

        this.lore = new ArrayList<>();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getHeadTexture()
    {
        return headTexture;
    }

    public void setHeadTexture(String headTexture)
    {
        this.headTexture = headTexture;
    }

    public List<String> getCatchableEntities()
    {
        return catchableEntities;
    }

    public ShapedRecipe getRecipe()
    {
        return recipe;
    }

    public void setRecipe(ShapedRecipe recipe)
    {
        this.recipe = recipe;
    }

    public int getMaxUsages()
    {
        return maxUsages;
    }

    public void setMaxUsages(int maxUsages)
    {
        this.maxUsages = maxUsages;
    }

    public boolean isUnlimitedUsages()
    {
        return unlimitedUsages;
    }

    public void setUnlimitedUsages(boolean unlimitedUsages)
    {
        this.unlimitedUsages = unlimitedUsages;
    }

    public double getCatchSuccessPercentage()
    {
        return catchSuccessPercentage;
    }

    public void setCatchSuccessPercentage(double catchSuccessPercentage)
    {
        this.catchSuccessPercentage = catchSuccessPercentage;
    }

    public List<String> getLore()
    {
        return lore;
    }

    public void setLore(List<String> lore)
    {
        this.lore = lore;
    }

    public double getBuyPrice()
    {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice)
    {
        this.buyPrice = buyPrice;
    }

    public void addCatchableEntity(EntityType type)
    {
        this.catchableEntities.add(type.toString());
    }

    public void addCatchableEntity(String type)
    {
        this.catchableEntities.add(type);
    }

    public void addBlacklistedEntity(String type)
    {
        this.blacklistedEntities.add(type);
    }

    public ItemStack getItemStack()
    {
        ItemStack item = new ItemStack(Material.STONE);

        item = new ItemStack(Material.PLAYER_HEAD, 1);
        item = HeadLib.setSkullOwner(item, UUID.randomUUID(), headTexture);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("PBName", name);
        nbtItem.setBoolean("PBIsPokeyball", true);
        nbtItem.setInteger("PBMaxUsages", maxUsages);
        nbtItem.setInteger("PBUsages", maxUsages);
        nbtItem.setDouble("PBCatchSuccess", catchSuccessPercentage);
        nbtItem.setInteger("PBAntiStackRandom", Utils.getRandomInt(1, 99999));
        item.setItemMeta(nbtItem.getItem().getItemMeta());

        InvUtil.setItemStackLore(item, lore);

        return item;
    }

    public static String getName(ItemStack itemStack)
    {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey("PBName"))
            return nbtItem.getString("PBName");
        return null;
    }

    public static int getMaxUsages(ItemStack itemStack)
    {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey("PBMaxUsages"))
            return nbtItem.getInteger("PBMaxUsages");
        return 1;
    }

    public static int getUsages(ItemStack itemStack)
    {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey("PBUsages"))
            return nbtItem.getInteger("PBUsages");
        return 1;
    }

    public static double getCatchSuccess(ItemStack itemStack)
    {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey("PBCatchSuccess"))
            return nbtItem.getDouble("PBCatchSuccess");
        return 100;
    }

    public static ItemStack reduceUsages(ItemStack itemStack, int amount)
    {
        return reduceUsages(itemStack, amount, null);
    }

    public static ItemStack reduceUsages(ItemStack itemStack, int amount, Entity entity)
    {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey("PBUsages"))
        {
            int newUsages = nbtItem.getInteger("PBUsages") - amount;
            nbtItem.setInteger("PBUsages", newUsages);
        }
        return Ball.updateLore(nbtItem.getItem(), entity);
    }

    public static boolean is(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR)
            return false;
        if (!item.hasItemMeta())
            return false;

        NBTItem nbtItem = new NBTItem(item);
        if (nbtItem.hasKey("PBIsPokeyball"))
            return nbtItem.getBoolean("PBIsPokeyball");
        return false;
    }

    public static boolean hasMob(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR)
            return false;
        if (!item.hasItemMeta())
            return false;

        NBTItem nbtItem = new NBTItem(item);
        return (nbtItem.hasKey("PBMobNBT"));
    }


    public static ItemStack catchMob(ItemStack ballItemStack, Entity mob)
    {
        CaughtMob caughtMob = new CaughtMob(mob);

        NBTItem nbtItem = new NBTItem(ballItemStack);
        nbtItem.setObject("PBMobNBT", caughtMob);
        ballItemStack.setItemMeta(nbtItem.getItem().getItemMeta());
        updateLore(ballItemStack, mob, caughtMob);
        return ballItemStack;
    }

    public static Entity freeMob(Item drop, Location location)
    {
        NBTItem nbtItem = new NBTItem(drop.getItemStack());
        CaughtMob caughtMob;
        if (MinecraftVersion.getVersion() == MinecraftVersion.MC1_16_R1)
        {
            String str = nbtItem.getString("PBMobNBT");
            caughtMob = GsonWrapper.deserializeJson(str, CaughtMob.class);
        }
        else
            caughtMob = nbtItem.getObject("PBMobNBT", CaughtMob.class);

        return caughtMob.spawnEntity(location, drop);
    }

    public static ItemStack updateLore(ItemStack ballItemStack)
    {
        return updateLore(ballItemStack, null);
    }

    public static ItemStack updateLore(ItemStack ballItemStack, Entity entity)
    {
        return updateLore(ballItemStack, entity, null);
    }

    public static ItemStack updateLore(ItemStack ballItemStack, Entity entity, CaughtMob caughtMob)
    {
        if (hasMob(ballItemStack))
        {
            if (caughtMob == null)
            {
                NBTItem nbtItem = new NBTItem(ballItemStack);
                caughtMob = nbtItem.getObject("PBMobNBT", CaughtMob.class);
            }

            String mobDescription = "";
            if (entity != null)
            {
                mobDescription = EntityUtil.getReadableEntityTypeName(entity, true);
            }
            else
            {
                mobDescription = EntityUtil.getReadableEntityTypeName(caughtMob.type);
            }

            List<String> lore = new ArrayList<>();
            lore.add(Settings.lang.getColored("usages").replace("{value}", getUsages(ballItemStack) + ""));
            lore.add(Settings.lang.getColored("catch-chance").replace("{value}", getCatchSuccess(ballItemStack) + "%"));
            lore.add(Settings.lang.getColored("mob-type").replace("{value}", mobDescription));
            lore.add(Settings.lang.getColored("mob-life").replace("{value}", caughtMob.getLife() + ""));

            InvUtil.setItemStackLore(ballItemStack, lore);
        }
        else
        {
            Ball original = Main.inst.ballsManager.byItemStack(ballItemStack);

            List<String> lore = new ArrayList<>();
            lore.add(Settings.lang.getColored("usages").replace("{value}", getUsages(ballItemStack) + ""));
            lore.add(Settings.lang.getColored("catch-chance").replace("{value}", getCatchSuccess(ballItemStack) + "%"));
            lore.add(Settings.lang.getColored("catchable-mobs"));
            lore.addAll(original.catchableMobsStringList);

            InvUtil.setItemStackLore(ballItemStack, lore);
        }
        return ballItemStack;
    }

    public void initLore()
    {
        StringBuilder catchableMobsStr = new StringBuilder(ChatColor.GRAY + "");
        for (String entityType : catchableEntities)
        {
            try
            {
                catchableMobsStr.append(EntityUtil.getReadableEntityTypeName(EntityType.valueOf(entityType))).append(", ");
            }
            catch (IllegalArgumentException exc)
            {
                switch (entityType)
                {
                    case "ALL_ANIMALS":
                        catchableMobsStr.append(Settings.lang.getColored("all-animals")).append(", ");
                        break;
                    case "ALL_MONSTERS":
                        catchableMobsStr.append(Settings.lang.getColored("all-monsters")).append(", ");
                        break;
                    case "ALL_FISH":
                        catchableMobsStr.append(Settings.lang.getColored("all-fish")).append(", ");
                        break;
                    case "ALL_WATER_MOBS":
                        catchableMobsStr.append(Settings.lang.getColored("all-water-mobs")).append(", ");
                        break;
                    case "ALL_MOBS":
                        catchableMobsStr.append(Settings.lang.getColored("all-mobs")).append(", ");
                        break;
                }
            }
        }

        catchableMobsStr = new StringBuilder(catchableMobsStr.substring(0, catchableMobsStr.length() - 2));

        if (catchableMobsStringList == null)
        {
            catchableMobsStringList = Arrays.asList(catchableMobsStr.toString().replaceAll("(?:\\s*)(.{1,27})(?:\\s+|\\s*$)", "$1\n").split("\n"));
            catchableMobsStringList.replaceAll(String::trim);
            catchableMobsStringList.replaceAll(s -> ChatColor.GRAY + s);
        }

        List<String> lore = new ArrayList<>();
        lore.add(Settings.lang.getColored("usages").replace("{value}", maxUsages + ""));
        lore.add(Settings.lang.getColored("catch-chance").replace("{value}", catchSuccessPercentage + "%"));
        lore.add(Settings.lang.getColored("catchable-mobs"));
        lore.addAll(catchableMobsStringList);

        setLore(lore);
    }

    /**
     * Returns cloned itemstack without the mob in NBT anymore
     */
    public static ItemStack removeMob(ItemStack ball)
    {
        NBTItem nbtItem = new NBTItem(ball.clone());
        nbtItem.removeKey("PBMobNBT");

        return updateLore(nbtItem.getItem());
    }

    public boolean isCatchableMob(Entity entity)
    {
        if (blacklistedEntities.contains(entity.getType().toString()))
            return false;

        if (blacklistedEntities.contains("ALL_MONSTERS") && entity instanceof Monster)
            return false;
        if (blacklistedEntities.contains("ALL_ANIMALS") && entity instanceof Animals)
            return false;
        if (blacklistedEntities.contains("ALL_FISH") && entity instanceof Fish)
            return false;
        if (blacklistedEntities.contains("ALL_WATER_MOBS") && entity instanceof WaterMob)
            return false;


        if (catchableEntities.contains("ALL_MOBS"))
            return true;

        if (catchableEntities.contains(entity.getType().toString()))
            return true;

        if (catchableEntities.contains("ALL_MONSTERS") && entity instanceof Monster)
            return true;
        if (catchableEntities.contains("ALL_ANIMALS") && entity instanceof Animals)
            return true;
        if (catchableEntities.contains("ALL_FISH") && entity instanceof Fish)
            return true;
        if (catchableEntities.contains("ALL_WATER_MOBS") && entity instanceof WaterMob)
            return true;
        return false;
    }

    public void playEffect(BallEffectType ballEffectType, Location location)
    {
        BallEffect ballEffect = null;

        switch (ballEffectType)
        {
            case CATCH:
                ballEffect = catchEffect;
                break;
            case FREE:
                ballEffect = freeEffect;
                break;
            case MISSED:
                ballEffect = missedEffect;
                break;
            case NOT_SPAWNED:
                ballEffect = notSpawnedEffect;
                break;
        }

        spawnParticle(ballEffect, location);
        playSound(ballEffect, location);
    }

    public void spawnParticle(BallEffect ballEffect, Location location)
    {
        ballEffect.particleData.getType().spawn(
                location.getWorld(),
                location,
                ballEffect.particleData.getAmount()
        );
    }

    public void playSound(BallEffect ballEffect, Location location)
    {
        location.getWorld().playSound(location,
                ballEffect.soundData.sound.playSound(),
                ballEffect.soundData.volume,
                ballEffect.soundData.pitch);
    }

    public boolean rollDice()
    {
        return Math.random() * 100 < catchSuccessPercentage;
    }
}
