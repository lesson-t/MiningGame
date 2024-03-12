package plugin.mininggame.data;

import lombok.Getter;
import lombok.Setter;

/**
 * MiningGameのゲームを実行する際のスコア情報を扱うオブジェクト。
 */
@Getter
@Setter
public class PlayerScore {

  private String playerName;
  private int score;
  private int gameTime;

}
