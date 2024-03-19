package plugin.mininggame.command;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import plugin.mininggame.Main;
import plugin.mininggame.data.ExecutingPlayer;
import plugin.mininggame.mapper.PlayerScoreMapper;
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

  private Main main;
  private List<ExecutingPlayer> executingPlayerList = new ArrayList<>();
  private SqlSessionFactory sqlSessionFactory;

  public MiningGameCommand(Main main) {
    this.main = main;

    try {
      InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
      this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
    if(args.length == 1 && LIST.equals(args[0])) {
      try (SqlSession session = sqlSessionFactory.openSession()) {
        PlayerScoreMapper mapper = session.getMapper(PlayerScoreMapper.class);
        List<PlayerScore> playerScoreList = mapper.selectList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (PlayerScore playerScore : playerScoreList) {
          LocalDateTime date = LocalDateTime.parse(playerScore.getRegisteredAt(), formatter);
          player.sendMessage(playerScore.getId() + " | "
              + playerScore.getPlayerName() + " | "
              + playerScore.getScore() + " | "
              + playerScore.getDifficulty() + " | "
              + date.format(formatter));
        }
      }

//      try (Connection con = DriverManager.getConnection(
//          "jdbc:mysql://localhost/spigot_server",
//          "root",
//          "mysql");
//          Statement statement = con.createStatement();
//          ResultSet resultSet = statement.executeQuery("select * from player_score;")) {
//        while (resultSet.next()) {
//          int id = resultSet.getInt("id");
//          String name = resultSet.getString("player_name");
//          int score = resultSet.getInt("score");
//          String difficulty = resultSet.getString("difficulty");
//
//          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//          LocalDateTime date = LocalDateTime.parse(resultSet.getString("registered_at"), formatter);
//
//          player.sendMessage(id + " | " + name + " | " + score +" | " + difficulty + " | " + date.format(
//              formatter));
//        }
//      } catch (SQLException e) {
//        e.printStackTrace();
//      }
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

    player.sendTitle("ゲームスタート！","", 0,40, 0);
    gamePlay(player, nowPlayer, difficulty);

    removePotionEffect(player);

    return true;
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

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label, String[] args) {
    return false;
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
    HandlerList.unregisterAll(main);

    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if(nowExecutingPlayer.getGameTime() <= 0) {
        Runnable.cancel();

        player.sendTitle("ゲームが終了しました。",
            nowExecutingPlayer.getPlayerName() + " 合計" + nowExecutingPlayer.getScore() + "点！",
            0, 60, 0);

        try (Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost/spigot_server",
            "root",
            "mysql");
            Statement statement = con.createStatement()) {

          statement.executeUpdate(
              "insert player_score(player_name, score, difficulty, registered_at) "
              + "values('" + nowExecutingPlayer.getPlayerName() + "', " + nowExecutingPlayer.getScore() + ", '"
              + difficulty + "', now());");
        } catch (SQLException e) {
          e.printStackTrace();
        }

        nowExecutingPlayer.setScore(0);
        HandlerList.unregisterAll(main);

        return;
      }
      player.sendMessage("残り" + nowExecutingPlayer.getGameTime() + "s");
      nowExecutingPlayer.setGameTime(nowExecutingPlayer.getGameTime() - 5);
    }, 0, 5 * 20);

    if(nowExecutingPlayer.getGameTime() > 0) {
      registerBlockBreakListener(nowExecutingPlayer);
    }
  }

  /**
   * プイレヤーが破壊したブロックのマテリアルタイプを判定して、特定の鉱石ブロックの場合に点数を加算します。
   *
   * @param nowPlayer コマンドを実行したプイレヤーのスコア情報
   */
  private void registerBlockBreakListener(ExecutingPlayer nowPlayer) {
    Bukkit.getPluginManager().registerEvents(new Listener() {
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
              int point = switch(material) {
                case COAL_ORE, COPPER_ORE, IRON_ORE -> 10;
                case GOLD_ORE, REDSTONE_ORE -> 30;
                case DIAMOND_ORE, NETHER_QUARTZ_ORE -> 50;
                default -> 0;
              };
              p.setScore(p.getScore() + point);
              player.sendMessage("ブロックを破壊した。Material:" + material + "合計点数：" + p.getScore());
            });
      }
    } , main);
  }

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
