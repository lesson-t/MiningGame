package plugin.mininggame;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.mininggame.command.MaterialSpawnCommand;
import plugin.mininggame.command.MiningGameCommand;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        MiningGameCommand miningGameCommand = new MiningGameCommand(this);
        MaterialSpawnCommand materialSpawnCommand = new MaterialSpawnCommand();
        Bukkit.getPluginManager().registerEvents(miningGameCommand, this);
        Bukkit.getPluginManager().registerEvents(materialSpawnCommand, this);
        Objects.requireNonNull(getCommand("mininggame")).setExecutor(miningGameCommand);
        Objects.requireNonNull(getCommand("materialSpawn")).setExecutor(materialSpawnCommand);

    }

}
