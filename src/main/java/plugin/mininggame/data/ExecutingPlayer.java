package plugin.mininggame.data;

import lombok.Getter;
import lombok.Setter;

/**
 * MiningGameのゲームを実行する際のプレイヤー情報を扱うオブジェクト。
 */
@Getter
@Setter
public class ExecutingPlayer {

  private String playerName;
  private int score;
  private int gameTime;

  public ExecutingPlayer(String playerName) {
    this.playerName = playerName;
  }
}
