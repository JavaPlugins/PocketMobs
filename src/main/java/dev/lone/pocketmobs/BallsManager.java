package dev.lone.pocketmobs;

import dev.lone.pocketmobs.utils.CustomConfigFile;
import dev.lone.pocketmobs.data.BallEffect;
import dev.lone.pocketmobs.data.ParticleData;
import dev.lone.pocketmobs.data.SoundData;
import dev.lone.pocketmobs.data.Ball;
import dev.lone.pocketmobs.utils.Mat;
import dev.lone.LoneLibs.nbt.nbtapi.NBTItem;
import dev.lone.LoneLibs.nbt.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class BallsManager
{
    Plugin plugin;

    public CustomConfigFile config;
    public List<Ball> balls;
    public HashMap<String, Ball> ballsByName;


    BallEffect defaultCatchConfig;
    BallEffect defaultFreeConfig;
    BallEffect defaultMissedConfig;
    BallEffect defaultNotSpawnedConfig;


    public BallsManager(Plugin plugin)
    {
        this.plugin = plugin;
        config = new CustomConfigFile(this.plugin, "balls", false, false);

        reload();
    }

    public void load()
    {
        this.balls = new ArrayList<>();
        this.ballsByName = new HashMap<>();

        Set<String> list = config.getConfig().getConfigurationSection("balls").getKeys(false);
        for (String key : list)
        {
            if(!config.getBoolean("balls." + key + ".enabled", true))
                continue;
            Ball tmp = new Ball(
                    key,
                    config.getString("balls." + key + ".head-texture")
            );

            // Add color support to displayname of balls
            // Old tmp.setDisplayName(ChatColor.RESET + config.getString("balls." + key + ".display-name"));
            tmp.setDisplayName(ChatColor.translateAlternateColorCodes('&',config.getString("balls." + key + ".display-name")));

            if(Settings.config.getBoolean("logic.buy.enabled", true))
            {
                if (Main.economy != null && config.getBoolean("balls." + key + ".buy.enabled", false))
                {
                    tmp.setBuyPrice(config.getDouble("balls." + key + ".buy.price"));
                    tmp.buyAmount = config.getInt("balls." + key + ".buy.amount");
                }
            }
            for(String entityType : config.getConfig().getStringList("balls." + key + ".catchable-entities"))
            {
                tmp.addCatchableEntity(entityType);
            }

            for(String entityType : config.getConfig().getStringList("balls." + key + ".blacklisted-entities"))
            {
                tmp.addBlacklistedEntity(entityType);
            }


            loadParticles(tmp, key);
            loadMaxUsages(tmp, key);
            loadCatchSuccessPercentage(tmp, key);

            tmp.initLore();

            if(Settings.config.getBoolean("logic.craft.enabled", true))
                loadRecipe(tmp, key);

            balls.add(tmp);
            ballsByName.put(key, tmp);
        }
    }

    public void reload()
    {
        config.reloadFromFile();

        defaultCatchConfig = loadDefaultEffectsConfig("catch");
        defaultFreeConfig = loadDefaultEffectsConfig("free");
        defaultMissedConfig = loadDefaultEffectsConfig("missed");
        defaultNotSpawnedConfig = loadDefaultEffectsConfig("not_spawned");
        this.load();
    }

    private void loadCatchSuccessPercentage(Ball tmp, String key)
    {
        double value = config.getDouble("balls." + key + ".catch-success", 100);
        tmp.setCatchSuccessPercentage(value);
    }

    private void loadMaxUsages(Ball tmp, String key)
    {
        if(config.getBoolean("balls." + key + ".unlimited-usages", false))
        {
            tmp.setUnlimitedUsages(true);
            tmp.setMaxUsages(-1);
        }
        else
        {
            int value = config.getInt("balls." + key + ".max-usages", 1);
            tmp.setMaxUsages(value);
        }
    }

    private void loadRecipe(Ball tmp, String key)
    {
        if (config.hasKey("balls." + key + ".craft-recipe"))
        {
            if(!config.getBoolean("balls." + key + ".craft-recipe.enabled"))
                return;

            ItemStack copy = tmp.getItemStack().clone();
            copy.setAmount(config.getInt("balls." + key + ".craft-recipe.amount", 1));

            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, key), copy);


            String[] shape = config.getConfig().getStringList("balls." + key + ".craft-recipe.pattern").toArray(new String[0]);
            recipe.shape(shape);

            ConfigurationSection defined = config.getConfig().getConfigurationSection("balls." + key + ".craft-recipe.ingredients");
            for (String ingredientKey : defined.getKeys(false))
            {
                if (ingredientKey.charAt(0) != 'X')
                {
                    try
                    {
                        if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_13_R1.getVersionId())
                            recipe.setIngredient(ingredientKey.charAt(0), Mat.valueOf(defined.getString(ingredientKey)).getMaterial());
                        else
                            recipe.setIngredient(ingredientKey.charAt(0), Mat.valueOf(defined.getString(ingredientKey)).getItemStack().getData());
                    }catch(IllegalArgumentException e)
                    {
                        e.printStackTrace();
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Can't find material: " + defined.getString(ingredientKey));
                    }
                }
            }

            try
            {
                Bukkit.addRecipe(recipe);
            }
            catch(IllegalStateException ignored)
            {
                Bukkit.removeRecipe(new NamespacedKey(plugin, key));
                Bukkit.addRecipe(recipe);
            }
            tmp.setRecipe(recipe);
        }
    }

    private void loadParticles(Ball tmp, String key)
    {
        tmp.catchEffect = loadEffectsConfig(key, defaultCatchConfig, "catch");
        tmp.freeEffect = loadEffectsConfig(key, defaultFreeConfig, "free");
        tmp.missedEffect = loadEffectsConfig(key, defaultMissedConfig, "missed");
        tmp.notSpawnedEffect = loadEffectsConfig(key, defaultNotSpawnedConfig, "not_spawned");
    }

    public BallEffect loadEffectsConfig(String configKey, BallEffect defaultConfig, String name)
    {
        try
        {
            BallEffect.BallEffectBuilder builder = new BallEffect.BallEffectBuilder();
            if (config.hasKey("balls." + configKey + ".effects." + name + ".particle"))
            {
                builder.setParticleConfig(new ParticleData(
                        config.getString("balls." + configKey + ".effects." + name + ".particle.name"),
                        config.getInt("balls." + configKey + ".effects." + name + ".particle.amount")
                ));
            }
            else
            {
                builder.setParticleConfig(defaultConfig.particleData);
            }
            if (config.hasKey("balls." + configKey + ".effects." + name + ".sound"))
            {
                builder.setSoundConfig(new SoundData(
                        config.getString("balls." + configKey + ".effects." + name + ".sound.name"),
                        config.getInt("balls." + configKey + ".effects." + name + ".sound.volume"),
                        config.getInt("balls." + configKey + ".effects." + name + ".sound.pitch")
                ));
            }
            else
            {
                builder.setSoundConfig(defaultConfig.soundData);
            }
            return builder.build();
        }
        catch (IllegalArgumentException e)
        {
            Main.inst.getLogger().severe(ChatColor.RED + "[PocketMobs] Error in file balls.yml, invalid particle or sound name in '" + name + "' effect of '" + configKey + "' ball.");
            return defaultConfig;
        }
    }

    public BallEffect loadDefaultEffectsConfig(String name)
    {
        try
        {
            BallEffect.BallEffectBuilder builder = new BallEffect.BallEffectBuilder();
            builder.setParticleConfig(new ParticleData(
                    Settings.config.getString("default.ball.effects." + name + ".particle.name"),
                    Settings.config.getInt("default.ball.effects." + name + ".particle.amount")
            ));
            builder.setSoundConfig(new SoundData(
                    Settings.config.getString("default.ball.effects." + name + ".sound.name"),
                    Settings.config.getInt("default.ball.effects." + name + ".sound.volume"),
                    Settings.config.getInt("default.ball.effects." + name + ".sound.pitch")
            ));
            return builder.build();
        }
        catch (IllegalArgumentException e)
        {
            Main.inst.getLogger().severe("[PocketMobs] Error in file balls.yml, invalid particle or sound name in '" + name + "' effect in config.yml.");
            return new BallEffect(new ParticleData("FLAME", 10), new SoundData("ENTITY_EXPERIENCE_ORB_PICKUP", 1, 1));
        }
    }

    public boolean exists(String string)
    {
        return byKey(string) != null;
    }

    public ItemStack getOriginalItemStack(String string)
    {
        return byKey(string).getItemStack();
    }

    public ItemStack getOriginalItemStack(ItemStack ballInstance)
    {
        return byItemStack(ballInstance).getItemStack();
    }

    public Ball byItemStack(ItemStack ballInstance)
    {
        return ballsByName.get(new NBTItem(ballInstance).getString("PBName"));
    }
    public Ball byKey(String string)
    {
        if(!ballsByName.containsKey(string))
            return null;
        return ballsByName.get(string);
    }

    public List<String> getKeys()
    {
        List<String> names = new ArrayList<>();
        for(Ball entry : balls)
            names.add(entry.name);
        return names;
    }

    public List<String> getKeys(String searchWord)
    {
        List<String> names = new ArrayList<>();
        for(Ball entry : balls)
            if(entry.name.contains(searchWord))
                names.add(entry.name);
        return names;
    }

    public List<ItemStack> getRecipeItems(ItemStack item)
    {
        List<ItemStack> items = new ArrayList<>();

        Ball ball = byKey(Ball.getName(item));
        if(ball == null)
            return null;
        ShapedRecipe recipe = ball.recipe;
        if(recipe == null)
            return null;

        Map<Character, ItemStack> chart = recipe.getIngredientMap();
        String[] shape = recipe.getShape();
        for(String line : shape)
            for(char letter : line.toCharArray())
                for (Map.Entry<Character, ItemStack> entry : chart.entrySet())
                    if(letter == entry.getKey())
                        items.add(entry.getValue());
        return items;
    }
}
