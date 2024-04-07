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
        Bukkit.getPluginManager().registerEvents(miningGameCommand, this);
        Objects.requireNonNull(getCommand("mininggame")).setExecutor(miningGameCommand);
        Objects.requireNonNull(getCommand("materialSpawn")).setExecutor(new MaterialSpawnCommand());

    }

}
