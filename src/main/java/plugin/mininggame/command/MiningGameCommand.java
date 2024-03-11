package plugin.mininggame.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import plugin.mininggame.Main;
import plugin.mininggame.data.PlayerScore;

public class MiningGameCommand implements CommandExecutor, Listener {

  private Main main;

  private List<PlayerScore> playerScoreList = new ArrayList<>();

  private int gameTime;

  public MiningGameCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    if(sender instanceof Player player) {
      if(playerScoreList.isEmpty()) {
        addNewPlayer(player);
      } else {
        for(PlayerScore playerScore: playerScoreList) {
          if(!playerScore.getPlayerName().equals(player.getName())) {
            addNewPlayer(player);
          }
        }
      }


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
              player.getName() + "合計 " + "playerscore.getScore() 点！",
              0, 60, 0);
          //playerscore.getScore()の表示未実装

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

    if(Objects.isNull(player) || playerScoreList.isEmpty()) {
      return;
    }

    for(PlayerScore playerScore : playerScoreList) {
      if(playerScore.getPlayerName().equals(player.getName())) {
        switch(material) {
          case COAL_ORE -> playerScore.setScore(playerScore.getScore() + 10);
          case COPPER_ORE -> playerScore.setScore(playerScore.getScore() + 10);
          case IRON_ORE -> playerScore.setScore(playerScore.getScore() + 10);
          case GOLD_ORE -> playerScore.setScore(playerScore.getScore() + 30);
          case REDSTONE_ORE -> playerScore.setScore(playerScore.getScore() + 30);
          case DIAMOND_ORE -> playerScore.setScore(playerScore.getScore() + 50);
          case NETHER_QUARTZ_ORE -> playerScore.setScore(playerScore.getScore() + 50);
        }
        player.sendMessage("ブロックを破壊しました。Material:" + material + "合計点数：" + playerScore.getScore());
      }
    }
  }

  /**
   * 新規のプレイヤー情報をリストに追加します。
   * @param player　コマンドを実行したプレイヤー
   */
  private void addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
  }

  /**
   * プレイヤーのポーション効果を除去します。
   * @param player　コマンドを実行したプイレヤー
   */
  private static void removePotionEffect(Player player) {
    player.getActivePotionEffects().stream()
        .map(PotionEffect::getType)
        .forEach(player::removePotionEffect);
  }

}
