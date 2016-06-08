package server.userInterface;

import java.util.List;

import generated.MoveMessageType;
import server.Board;
import server.Game;
import server.Player;

public interface UI {

	// XXX: ich bin dafuer long in int zu aendern, damit man bei Timern mit
	// Delay nicht mehr casten muss. Auf meinem System, wuerde der maximale
	// Integer eine Wartezeit von abgerundet 24 Tagen zulassen. Ist meiner
	// Meinung ausreichend ;) ~jago
	public void displayMove(MoveMessageType mm, Board b, long moveDelay,
			long shiftDelay, boolean treasureReached);

	public void updatePlayerStatistics(List<Player> stats, Integer current);
	public void init(Board b);
	public void setGame(Game g);
	public void gameEnded(Player winner);
}
