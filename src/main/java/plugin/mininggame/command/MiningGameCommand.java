package plugin.mininggame.command;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import plugin.mininggame.PlayerScoreData;
import plugin.mininggame.Main;
import plugin.mininggame.data.ExecutingPlayer;
import plugin.mininggame.mapper.data.PlayerScore;

/**
 * 制限時間内に特定の鉱石ブロックを破壊して、スコアを獲得するゲームを起動するコマンドです。
 * スコアは破壊した鉱石ブロックによって変わり、破壊した合計によりスコアが変動します。
 * 結果はプイレヤー名、点数、日時などで保存されます。
 */
public class MiningGameCommand extends BaseCommand implements Listener {

  public static final int GAME_TIME = 20;
  public static final String EASY = "easy";
  public static final String NORMAL = "normal";
  public static final String HARD = "hard";
  public static final String NONE = "none";
  public static final String LIST = "list";

  private final Main main;
  private final PlayerScoreData playerScoreData = new PlayerScoreData();
  private final List<ExecutingPlayer> executingPlayerList = new ArrayList<>();
  private Material preMaterial;


  public MiningGameCommand(Main main) {
    this.main = main;
  }

  /**
   * ゲームを実行します。規定の時間内に採掘をして、採掘した鉱石の種類に応じてスコアに加算します。合計スコアを時間経過後に表示します。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param command　コマンド
   * @param label　ラベル
   * @param args　コマンド引数
   * @return　処理の終了
   */
  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
    // 最初の引数が「list」だったらスコアを一覧表示して処理を終了する。
    if(args.length == 1 && LIST.equals(args[0])) {
      sendPlayerScoreList(player);
      return false;
    }

    String difficulty = getDifficulty(player, args);
    if(difficulty.equals(NONE)) {
      return false;
    }

    ExecutingPlayer nowPlayer = getPlayerScore(player);

    initPlayerStatus(player, difficulty);
    removePotionEffect(player);
    removeEnemies(player);

    gamePlay(player, nowPlayer, difficulty);

    removePotionEffect(player);

    return true;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label, String[] args) {
    return false;
  }

  /**
   * 現在登録されているスコアの一覧をメッセージに送る。
   *
   * @param player　プレイヤー
   */
  private void sendPlayerScoreList(Player player) {
    List<PlayerScore> playerScoreList = playerScoreData.selectList();
    for (PlayerScore playerScore : playerScoreList) {
      player.sendMessage(playerScore.getId() + " | "
          + playerScore.getPlayerName() + " | "
          + playerScore.getScore() + " | "
          + playerScore.getDifficulty() + " | "
          + playerScore.getRegisteredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
  }

  /**
   * 難易度をコマンド引数から取得します。
   *
   * @param player　コマンドを実行したプイレヤー
   * @param args　コマンド引数
   * @return　難易度
   */
  private String getDifficulty(Player player, String[] args) {
    if (args.length == 1 && (EASY.equals(args[0]) || NORMAL.equals(args[0]) || HARD.equals(args[0]))) {
      return args[0];
    }
    player.sendMessage(ChatColor.RED + "実行できません。コマンド引数の1つ目に難易度指定が必要です。[easy, normal, hard]");
    return NONE;
  }

  /**
   * 現在実行しているプレイヤーのスコア情報を取得する。
   *
   * @param player　コマンドを実行したプイレヤー
   * @return　現在実行しているプレイヤーのスコア情報
   */
  private ExecutingPlayer getPlayerScore(Player player) {
    ExecutingPlayer executingPlayer = new ExecutingPlayer(player.getName());

    if(executingPlayerList.isEmpty()) {
      executingPlayer = addNewPlayer(player);
    } else {
      executingPlayer = executingPlayerList.stream()
          .findFirst()
          .map(ps -> ps.getPlayerName().equals(player.getName())
              ? ps
              : addNewPlayer(player)).orElse(executingPlayer);
    }
    executingPlayer.setGameTime(GAME_TIME);
    return executingPlayer;
  }

  /**
   * 新規のプレイヤー情報をリストに追加します。
   *
   * @param player　コマンドを実行したプレイヤー
   * @return 新規プレイヤー
   */
  private ExecutingPlayer addNewPlayer(Player player) {
    ExecutingPlayer newPlayer = new ExecutingPlayer(player.getName());
    executingPlayerList.add(newPlayer);
    return newPlayer;
  }

  /**
   * プレイヤーの初期状態を設定する。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param difficulty　難易度
   */
  private void initPlayerStatus(Player player, String difficulty) {
    player.setLevel(30);
    player.setHealth(20);
    player.setFoodLevel(20);

    PlayerInventory inventory = player.getInventory();
    inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
    inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));

    switch (difficulty) {
      case NORMAL -> inventory.setItemInMainHand(new ItemStack(Material.IRON_PICKAXE));
      case HARD -> inventory.setItemInMainHand(new ItemStack(Material.STONE_PICKAXE));
      default -> inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_PICKAXE));
    }
  }

  /**
   * 周辺の敵を削除する。
   *
   * @param player　コマンドを実行したプレイヤー
   */
  private void removeEnemies(Player player) {
    List<Entity> nearbyEnemies = player.getNearbyEntities(100, 100, 100);
    for(Entity enemy : nearbyEnemies) {
      switch (enemy.getType()) {
          case SPIDER, ZOMBIE, SKELETON, WITCH, ENDERMAN, CREEPER, PHANTOM -> enemy.remove();
      }
    }
  }

  /**
   * ゲームを実行します。規定の時間内に特定の鉱石ブロックを壊すとスコアが加算されます。合計スコアが時間の経過後に表示します。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param nowExecutingPlayer　プレイヤースコア情報
   * @param difficulty　難易度
   */
  private void gamePlay(Player player, ExecutingPlayer nowExecutingPlayer, String difficulty) {
    player.sendTitle("ゲームスタート！","", 0,40, 0);
    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if(nowExecutingPlayer.getGameTime() <= 0) {
        Runnable.cancel();

        player.sendTitle("ゲームが終了しました。",
            nowExecutingPlayer.getPlayerName() + " 合計" + nowExecutingPlayer.getScore() + "点！",
            0, 60, 0);

        removePotionEffect(player);

        playerScoreData.insert(
            new PlayerScore(nowExecutingPlayer.getPlayerName()
            , nowExecutingPlayer.getScore()
            , difficulty));

        nowExecutingPlayer.setScore(0);
        return;
      }
      player.sendMessage("残り" + nowExecutingPlayer.getGameTime() + "s");
      nowExecutingPlayer.setGameTime(nowExecutingPlayer.getGameTime() - 5);
    }, 0, 5 * 20);

  }

  /**
   * ゲーム時間内に鉱石を採掘したときに、特定の鉱石だった場合加点する。
   *
   * @param e　破壊したブロックイベント
   */
  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    Player player = e.getPlayer();
    Material material = e.getBlock().getType();

    if(executingPlayerList.isEmpty()) {
      return;
    }

    executingPlayerList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst()
        .ifPresent(p -> {
          if (p.getGameTime() > 0) {
            int point = switch (material) {
              case COAL_ORE, IRON_ORE, COPPER_ORE -> 10;
              case GOLD_ORE, REDSTONE_ORE -> 30;
              case DIAMOND_ORE, LAPIS_ORE, EMERALD_ORE -> 50;
              default -> 0;
            };
            if(material.equals(preMaterial)) {
              point *=2;
              player.sendMessage("同種の鉱石を採掘！獲得点数２倍");
            }

            p.setScore(p.getScore() + point);
            player.sendMessage(material + "を採掘しました。" + point + "点を獲得！ 合計" + p.getScore() + "点");
            preMaterial = material;
          }
        });
  }

  /**
   * プイレヤーが破壊したブロックのマテリアルタイプを判定して、特定の鉱石ブロックの場合に点数を加算します。
   *
   */
//  private void registerBlockBreakListener() {
//    Bukkit.getPluginManager().registerEvents(new Listener() {
//      @EventHandler
//      public void onBlockBreak(BlockBreakEvent e) {
//        Player player = e.getPlayer();
//        Material material = e.getBlock().getType();
//
//        if(executingPlayerList.isEmpty()) {
//          return;
//        }
//
//        executingPlayerList.stream()
//            .filter(p -> p.getPlayerName().equals(player.getName()))
//            .findFirst()
//            .ifPresent(p -> {
//              int point = switch(material) {
//                case COAL_ORE, COPPER_ORE, IRON_ORE -> 10;
//                case GOLD_ORE, REDSTONE_ORE -> 30;
//                case DIAMOND_ORE, NETHER_QUARTZ_ORE -> 50;
//                default -> 0;
//              };
//              p.setScore(p.getScore() + point);
//              player.sendMessage("ブロックを破壊した。Material:" + material + "合計点数：" + p.getScore());
//            });
//      }
//    } , main);
//  }

  /**
   * プレイヤーのポーション効果を除去します。
   *
   * @param player　コマンドを実行したプイレヤー
   */
  private void removePotionEffect(Player player) {
    player.getActivePotionEffects().stream()
        .map(PotionEffect::getType)
        .forEach(player::removePotionEffect);
  }
}
