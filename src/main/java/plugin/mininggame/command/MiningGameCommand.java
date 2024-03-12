package plugin.mininggame.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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


  public MiningGameCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    if(sender instanceof Player player) {
      PlayerScore nowPlayer = getPlayerScore(player);
      nowPlayer.setGameTime(30);

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
        if(nowPlayer.getGameTime() <= 0) {
          Runnable.cancel();

          player.sendTitle("ゲームが終了しました。",
              nowPlayer.getPlayerName() + " 合計" + nowPlayer.getScore() + "点！",
              0, 60, 0);
          nowPlayer.setScore(0);

          removePotionEffect(player);

          return;
        }
        player.sendMessage("残り" + nowPlayer.getGameTime() + "s");
        nowPlayer.setGameTime(nowPlayer.getGameTime() - 5);
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
          case COAL_ORE, COPPER_ORE, IRON_ORE -> playerScore.setScore(playerScore.getScore() + 10);
          case GOLD_ORE, REDSTONE_ORE -> playerScore.setScore(playerScore.getScore() + 30);
          case DIAMOND_ORE, NETHER_QUARTZ_ORE -> playerScore.setScore(playerScore.getScore() + 50);
        }
        player.sendMessage("ブロックを破壊しました。Material:" + material + "合計点数：" + playerScore.getScore());
      }
    }
  }

  /**
   * 現在実行しているプレイヤーのスコア情報を取得する。
   * @param player　コマンドを実行したプイレヤー
   * @return　現在実行しているプレイヤーのスコア情報
   */
  private PlayerScore getPlayerScore(Player player) {
    if(playerScoreList.isEmpty()) {
      return addNewPlayer(player);
    } else {
      for(PlayerScore playerScore: playerScoreList) {
        if(!playerScore.getPlayerName().equals(player.getName())) {
          return addNewPlayer(player);
        } else {
          return playerScore;
        }
      }
    }
    return null;
  }

  /**
   * 新規のプレイヤー情報をリストに追加します。
   * @param player　コマンドを実行したプレイヤー
   * @return 新規プレイヤー
   */
  private PlayerScore addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
    return newPlayer;
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
