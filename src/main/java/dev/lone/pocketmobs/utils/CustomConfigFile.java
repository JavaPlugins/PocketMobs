package dev.lone.pocketmobs.utils;

import dev.lone.pocketmobs.Utils;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Deprecated // Revamp this shit
public class CustomConfigFile
{
    private FileConfiguration config;
    private final Plugin plugin;
    private final String fileName;
    private final File configFile;
    private boolean needsToBeUpdatedIfDifferentFromResourceFile = false;
    private boolean announceCustomLanguage = false;

    public CustomConfigFile(Plugin plugin, String fileName, boolean needsToBeUpdatedIfDifferentFromResourceFile, boolean announceCustomLanguage)
    {
        this.plugin = plugin;
        this.fileName = fileName;
        this.needsToBeUpdatedIfDifferentFromResourceFile = needsToBeUpdatedIfDifferentFromResourceFile;
        this.announceCustomLanguage = announceCustomLanguage;
        this.configFile = new File("plugins/PocketMobs/" + this.fileName + ".yml");

        config = YamlConfiguration.loadConfiguration(configFile);
        update();
    }

    public void reloadFromFile()
    {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void update()
    {
        try
        {
            if (!configFile.exists())
            {
                // carico il file di default contenuto nel jar
                FileUtils.copyInputStreamToFile(plugin.getResource(this.fileName + ".yml"), configFile);
            }
            else
            {
                if (needsToBeUpdatedIfDifferentFromResourceFile)
                {
                    FileConfiguration c = YamlConfiguration
                            .loadConfiguration((new InputStreamReader(plugin.getResource(this.fileName + ".yml"))));
                    for (String k : c.getKeys(true))
                    {
                        if (!config.contains(k))
                            config.set(k, c.get(k));
                    }
                    config.save(configFile);
                }
            }
            config.load(configFile);
        }
        catch (Exception e)
        {
			//e.printStackTrace();
            //Msg.sendConsoleError("ERROR LOADING file '" + filePath + "'");
            if (announceCustomLanguage)
                Bukkit.getServer().getConsoleSender()
                        .sendMessage(org.bukkit.ChatColor.YELLOW + "Using custom language file '" + fileName + "'");
        }
    }

    public String getLang(String name)
    {
        try
        {
            return Utils.convertColor(config.getString(name));
        }
        catch (NullPointerException exc)
        {
            exc.printStackTrace();
            Bukkit.getServer().getConsoleSender()
                    .sendMessage("[PocketMobs] " + ChatColor.RED + "Error in file " + fileName + ".yml"
                            + ", please check its integrity or delete it and reload the plugin." + " " + "'" + name
                            + "'" + " not found.");
            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (player.isOp())
                {
                    player.sendMessage("[PocketMobs] " + ChatColor.RED + "Error in file " + fileName + ".yml"
                            + ", please check its integrity or delete it and reload the plugin." + " " + "'" + name
                            + "'" + " not found.");
                }
            }

        }

        return ChatColor.RED + "ErrorInLangFile " + fileName + ".yml";
    }

    public String getColored(String name)
    {
        if (hasKey(name))
            return Utils.convertColor(config.getString(name));
        else
            return "Error in lang file";
    }

    public String getStripped(String name)
    {
        return ChatColor.stripColor(getColored(name));
    }

    public List<String> getColoredList(String ymlPath)
    {
        ArrayList<String> coloredList = new ArrayList<>();
        try
        {
            ArrayList<String> list = (ArrayList<String>) config.getStringList(ymlPath);
            for (String entry : list)
                coloredList.add(Utils.convertColor(entry));
        }
        catch (NullPointerException exc)
        {
            exc.printStackTrace();
            Bukkit.getServer().getConsoleSender()
                    .sendMessage("[BackPacks] " + ChatColor.RED + "Error in file " + fileName + ".yml"
                            + ", please check its integrity or delete it and reload the plugin." + " " + "'" + ymlPath
                            + "'" + " not found.");
            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (player.isOp())
                {
                    player.sendMessage("[BackPacks] " + ChatColor.RED + "Error in file " + fileName + ".yml"
                            + ", please check its integrity or delete it and reload the plugin." + " " + "'" + ymlPath
                            + "'" + " not found.");
                }
            }
        }

        return coloredList;
    }

    public List<String> getSectionKeysList(String path)
    {
        return new ArrayList<>(config.getConfigurationSection(path).getKeys(false));
    }

    public FileConfiguration getConfig()
    {
        return config;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void set(String path, Object value)
    {
        this.config.set(path, value);
    }

    public void save()
    {
        try
        {
            this.config.save(configFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getString(String path)
    {
        return config.getString(path);
    }

    public String getString(String path, String defaultValue)
    {
        if (!hasKey(path))
            return defaultValue;
        return config.getString(path);
    }

    public int getInt(String path)
    {
        return config.getInt(path);
    }

    /**
     * Riturna interno se trovato, se no ritorna defaultValue
     *
     * @param path
     * @param defaultValue
     * @return
     */
    public int getInt(String path, int defaultValue)
    {
        if (hasKey(path))
            return config.getInt(path);
        return defaultValue;
    }

    /**
     * Riturna double se trovato, se no ritorna defaultValue
     *
     * @param path
     * @param defaultValue
     * @return
     */
    public double getDouble(String path, double defaultValue)
    {
        if (hasKey(path))
            return config.getDouble(path);
        return defaultValue;
    }

    public double getDouble(String path)
    {
        return config.getDouble(path);
    }

    public boolean getBoolean(String path)
    {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean defaultValue)
    {
        if (hasKey(path))
            return config.getBoolean(path);
        return defaultValue;
    }

    public Material getMaterial(String path)
    {
        if (hasKey(path))
            return Material.valueOf(config.getString(path));
        else
            return null;
    }

    public PotionEffectType getPotionEffect(String path)
    {
        if (hasKey(path))
            return PotionEffectType.getByName(config.getString(path));
        else
            return null;
    }

    public boolean hasKey(String path)
    {
        return (config.get(path) != null);
    }

}
