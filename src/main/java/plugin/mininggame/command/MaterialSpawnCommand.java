package plugin.mininggame.command;

import java.net.http.WebSocket.Listener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MaterialSpawnCommand extends BaseCommand implements Listener, org.bukkit.event.Listener {

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
    materialSpawn(player);

    return true;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label, String[] args) {
    return false;
  }

  private void materialSpawn(Player player) {
    World world = player.getWorld();
    Location playerLocation = player.getLocation();
    double x = playerLocation.getX();
    double y = playerLocation.getY();
    double z = playerLocation.getZ() +6;

    world.getBlockAt(new Location(world, x-3, y+1, z)).setType(Material.COAL_ORE);
    world.getBlockAt(new Location(world, x-3,    y,   z)).setType(Material.COAL_ORE);
    world.getBlockAt(new Location(world, x-2, y+1, z)).setType(Material.COPPER_ORE);
    world.getBlockAt(new Location(world, x-2,    y,   z)).setType(Material.COPPER_ORE);
    world.getBlockAt(new Location(world, x-1, y+1, z)).setType(Material.IRON_ORE);
    world.getBlockAt(new Location(world, x-1,    y,   z)).setType(Material.IRON_ORE);
    world.getBlockAt(new Location(world,    x,   y+1, z)).setType(Material.GOLD_ORE);
    world.getBlockAt(new Location(world,    x,      y,   z)).setType(Material.GOLD_ORE);
    world.getBlockAt(new Location(world, x+1, y+1, z)).setType(Material.REDSTONE_ORE);
    world.getBlockAt(new Location(world, x+1,    y,   z)).setType(Material.REDSTONE_ORE);
    world.getBlockAt(new Location(world, x+2, y+1, z)).setType(Material.DIAMOND_ORE);
    world.getBlockAt(new Location(world, x+2,    y,   z)).setType(Material.DIAMOND_ORE);
    world.getBlockAt(new Location(world, x+3, y+1, z)).setType(Material.NETHER_QUARTZ_ORE);
    world.getBlockAt(new Location(world, x+3,    y,   z)).setType(Material.NETHER_QUARTZ_ORE);
  }

}
