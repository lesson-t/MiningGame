package plugin.mininggame;

import java.util.Objects;
import org.bukkit.Bukkit;
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

  private Player player;

  private int score;
  
  private int gameTime;

  public MiningGameCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    if(sender instanceof Player player) {
      this.player = player;
      World world = player.getWorld();
      gameTime = 30;

      player.setLevel(30);
      player.setHealth(20);
      player.setFoodLevel(20);

      PlayerInventory inventory = player.getInventory();
      inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
      inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
      inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
      inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
      inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_PICKAXE));

      removePotionEffect(player);

      player.sendTitle("ゲームスタート！","", 0,40, 0);

      Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
        if(gameTime <= 0) {
          Runnable.cancel();

          player.sendTitle("ゲームが終了しました。",
              player.getName() + "合計 " + score + "点！",
              0, 60, 0);

          removePotionEffect(player);

          return;
        }
        player.sendMessage("残り" + gameTime + "s");
        gameTime -=5;
      }, 0, 5 * 20);

    }
    return false;
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    Player player = e.getPlayer();
    Material material = e.getBlock().getType();

    if(Objects.isNull(player))
      return;
    if(Objects.isNull(this.player))
      return;

    if(this.player.getName().equals(player.getName())) {

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

  private static void removePotionEffect(Player player) {
    player.getActivePotionEffects().stream()
        .map(PotionEffect::getType)
        .forEach(player::removePotionEffect);
  }

}
