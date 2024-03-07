package plugin.mininggame;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        MiningGameCommand miningGameCommand = new MiningGameCommand();
        Bukkit.getPluginManager().registerEvents(miningGameCommand, this);
        getCommand("mininggame").setExecutor(new MiningGameCommand());

    }

}
