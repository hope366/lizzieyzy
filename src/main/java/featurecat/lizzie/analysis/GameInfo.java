package featurecat.lizzie.analysis;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.gui.LizzieFrame;
import java.util.Date;

public class GameInfo {
  public static final String DEFAULT_NAME_CPU_PLAYER = "Computer";
  public static double DEFAULT_KOMI = 7.5;
  public boolean changedKomi = false;

  private String playerBlack = "";
  private String playerWhite = "";
  private Date date = new Date();
  private double komi = DEFAULT_KOMI;
  private int handicap = 0;
  private String result = "";

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
    Lizzie.frame.setResult(result);
  }

  public String getPlayerBlack() {
    return playerBlack;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public void setPlayerBlack(String playerBlack) {
    this.playerBlack = playerBlack;
  }

  public String getPlayerWhite() {
    return playerWhite;
  }

  public String getSaveFileName() {
    if (playerBlack.equals("") && playerWhite.equals(""))
      return Lizzie.resourceBundle.getString("GameInfo.untitled");
    else return playerBlack + "_Vs_" + playerWhite;
  }

  public void setPlayerWhite(String playerWhite) {
    this.playerWhite = playerWhite;
  }

  public double getKomi() {
    return komi;
  }

  public void setKomi(double komi) {
    this.komi = komi;
    LizzieFrame.menu.txtKomi.setText(String.valueOf(komi));
    if (Lizzie.frame.isInScoreMode) Lizzie.board.showGroupResult();
  }

  public void setKomiNoMenu(double komi) {
    this.komi = komi;
  }

  public void changeKomi() {
    changedKomi = true;
  }

  public int getHandicap() {
    return handicap;
  }

  public void setHandicap(int handicap) {
    this.handicap = handicap;
  }

  public void resetAllNoKomi() {
    this.handicap = 0;
    this.playerBlack = "";
    this.playerWhite = "";
    this.date = new Date();
    this.result = "";
    Lizzie.frame.setResult("");
  }
}
