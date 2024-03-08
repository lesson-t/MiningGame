package plugin.mininggame;

import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MaterialSpawnCommand implements CommandExecutor, Listener, org.bukkit.event.Listener {


  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    if(sender instanceof Player player) {
      World world = player.getWorld();
      Location playerLocation = player.getLocation();
      double x = playerLocation.getX();
      double y = playerLocation.getY();
      double z = playerLocation.getZ() +3;

      world.getBlockAt(new Location(world, x-3, y, z+3)).setType(Material.COAL_ORE);
      world.getBlockAt(new Location(world, x-2, y, z+3)).setType(Material.COPPER_ORE);
      world.getBlockAt(new Location(world, x-1, y, z+3)).setType(Material.IRON_ORE);
      world.getBlockAt(new Location(world, x, y, z+3)).setType(Material.GOLD_ORE);
      world.getBlockAt(new Location(world, x+1, y, z+3)).setType(Material.REDSTONE_ORE);
      world.getBlockAt(new Location(world, x+2, y, z+3)).setType(Material.DIAMOND_ORE);
      world.getBlockAt(new Location(world, x+3, y, z+3)).setType(Material.NETHER_QUARTZ_ORE);

    }
    return false;
  }

}
