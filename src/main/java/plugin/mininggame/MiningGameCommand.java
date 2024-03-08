package plugin.mininggame;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class MiningGameCommand implements CommandExecutor, Listener {

  private Main main;

  int score;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    if(sender instanceof Player player) {
      World world = player.getWorld();
      player.setLevel(30);
      player.setHealth(20);
      player.setFoodLevel(20);

      PlayerInventory inventory = player.getInventory();
      inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
      inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
      inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
      inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
      inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_PICKAXE));

      player.getActivePotionEffects().stream()
          .map(PotionEffect::getType)
          .forEach(player::removePotionEffect);

      player.sendTitle("ゲームスタート！","", 0,40, 0);

    }
    return false;
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    Player player = e.getPlayer();
    World world = e.getBlock().getWorld();
    Material material = e.getBlock().getType();

    switch(material) {
      case COAL_ORE -> score +=10;
      case COPPER_ORE -> score +=10;
      case IRON_ORE -> score +=10;
      case GOLD_ORE -> score +=30;
      case REDSTONE_ORE -> score +=30;
      case DIAMOND_ORE -> score +=50;
      case NETHER_QUARTZ_ORE -> score +=100;
    }

    player.sendMessage("ブロックを破壊しました。Material:" + material + "合計点数：" + score);
  }
}
