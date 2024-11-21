package dev.lone.pocketmobs;

import dev.lone.LoneLibs.Events;
import dev.lone.pocketmobs.commands.MainCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This shit plugin was coded a lot of years ago, please be kind. Thanks.
 */
public final class Main extends JavaPlugin
{
    public static Main inst;

    public static Economy economy;
    public BallsManager ballsManager;
    public static MainCommand mainCommand;

    @Override
    public void onEnable()
    {
        inst = this;

        if (!setupEconomy())
            getLogger().info("No Vault dependency found or no economy plugin installed (example: EssentialsX)!");

        Settings.load();

        mainCommand = new MainCommand();
        ballsManager = new BallsManager(this);

        Events.register(this, new BallsEventsListener());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = (RegisteredServiceProvider<Economy>) this.getServer().getServicesManager().getRegistration((Class) Economy.class);
        if (economyProvider != null)
            economy = economyProvider.getProvider();

        return (economy != null);
    }
}
