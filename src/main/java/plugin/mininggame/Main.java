package plugin.mininggame;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        MiningGameCommand miningGameCommand = new MiningGameCommand(this);
        MaterialSpawnCommand materialSpawnCommand = new MaterialSpawnCommand();
        Bukkit.getPluginManager().registerEvents(miningGameCommand, this);
        Bukkit.getPluginManager().registerEvents(materialSpawnCommand, this);
        getCommand("mininggame").setExecutor(miningGameCommand);
        getCommand("materialSpawn").setExecutor(new MaterialSpawnCommand());

    }

}
