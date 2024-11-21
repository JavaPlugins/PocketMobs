package dev.lone.pocketmobs;

import dev.lone.pocketmobs.utils.CustomConfigFile;

import java.util.List;

public class Settings
{
    public static CustomConfigFile lang;
    public static CustomConfigFile config;
    public static List<String> worlds;
    public static boolean returnToInvFree;
    public static boolean returnToInvCatch;
    public static boolean dropperSpawnMob;
    public static boolean reduceUsagesOnMiss;

    public static void load()
    {
        config = new CustomConfigFile(Main.inst, "config", true, false);
        lang = new CustomConfigFile(Main.inst, "lang/" + config.getString("lang"), false, true);

        worlds = config.getConfig().getStringList("worlds");
        returnToInvFree = config.getBoolean("logic.ball-behaviour.return-to-inventory.on-free-mob", false);
        returnToInvCatch = config.getBoolean("logic.ball-behaviour.return-to-inventory.on-catch-mob", false);
        dropperSpawnMob = config.getBoolean("logic.ball-behaviour.dropper-spawns-mob", false);

        reduceUsagesOnMiss = config.getBoolean("logic.ball-behaviour.reduce-usages.miss-target", true);
    }
}
