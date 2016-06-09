package de.crazychicken.mazenet;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import javax.xml.bind.UnmarshalException;

import generated.AcceptMessageType;
import generated.AwaitMoveMessageType;
import generated.BoardType;
import generated.CardType;
import generated.CardType.Pin;
import generated.MazeCom;
import generated.MazeComType;
import generated.PositionType;
import generated.TreasureType;
import generated.WinMessageType.Winner;
import server.Board;
import server.Card;

public class Client {

	private Random random = new Random();
	private Connection connection;
	private long moveCount;

	public Client(Connection connection) {
		this.connection = connection;
		this.moveCount = 1;
	}

	public void play() throws UnmarshalException, IOException {
		System.out.println("Logge mich ein...");
		this.connection.sendLogin();

		System.out.println("Spiele...");

		while (true) {
			// Nachricht vom Server lesen
			MazeCom message = this.connection.getXmlInStream().readMazeCom();

			// Aktion je nach Typ der Nachricht ausführen
			if (message.getMcType() == MazeComType.DISCONNECT) {
				onDisconnect();
				return;
			} else if (message.getMcType() == MazeComType.WIN) {
				onWin(message.getWinMessage().getWinner());
			} else if (message.getMcType() == MazeComType.AWAITMOVE) {
				onAwaitMove(message.getAwaitMoveMessage());
			}
		}
	}

	private void onAwaitMove(AwaitMoveMessageType awaitMoveMessage) throws UnmarshalException, IOException {
		System.out.println("Wir sind dran! Runde " + this.moveCount);
		this.moveCount++;
		
		
		PositionType shiftPosition = new PositionType();
		PositionType pinPosition = new PositionType();
		boolean foundTreasure = false;
		for (int i = 1; i < 6; i+=2) {
			System.out.println("�u�ere Schleife: " + i);
			if (foundTreasure)  {
				break;
			}
			for (int j = 0; j < 7; j+=6) {
				System.out.println("Innere Schleife: " + j);
				
				foundTreasure = searchMove(pinPosition, shiftPosition, i, j, awaitMoveMessage);
				if(!foundTreasure){
					foundTreasure = searchMove(pinPosition, shiftPosition, j, i, awaitMoveMessage);
				}
				if(foundTreasure){
					break;
				}
				
				
				
			}
			
		}
		
		if (!foundTreasure) {
			System.out.println("Zufall!");
			Board b = new Board(awaitMoveMessage.getBoard());
			while (positionEquals(shiftPosition = randomShiftPosition(), awaitMoveMessage.getBoard().getForbidden()));
			
			CardType shiftCard = new CardType();
			shiftCard.setOpenings(awaitMoveMessage.getBoard().getShiftCard().getOpenings());
			shiftCard.setTreasure(awaitMoveMessage.getBoard().getShiftCard().getTreasure());
			CardType.Pin pin = new Pin();
			pin.getPlayerID().addAll(awaitMoveMessage.getBoard().getShiftCard().getPin().getPlayerID());
			shiftCard.setPin(pin);

			// schieben simulieren
			doShift(b, shiftCard, shiftPosition);
			PositionType currentPosition = findPlayer(b);
			List<PositionType> reachable = getReachablePositions(b, currentPosition);
			Collections.shuffle(reachable);
			pinPosition = reachable.get(0);
			
			/*
			//Collections.shuffle(emergency);
			PositionType firstKey = emergency.keySet().iterator().next(); // erstes Element?
			shiftPosition = firstKey; //key
			pinPosition = emergency.get(firstKey);//value
			*/
		}

		/*
		// zufällige Position zum Schieben finden, die nicht der letzten
		// (verbotenen) entspricht
		PositionType shiftPosition;
		while (positionEquals(shiftPosition = randomShiftPosition(), awaitMoveMessage.getBoard().getForbidden()))
			;

		
		
		// Schiebekarte kopieren
		CardType shiftCard = new CardType();
		shiftCard.setOpenings(awaitMoveMessage.getBoard().getShiftCard().getOpenings());
		shiftCard.setTreasure(awaitMoveMessage.getBoard().getShiftCard().getTreasure());
		CardType.Pin pin = new Pin();
		pin.getPlayerID().addAll(awaitMoveMessage.getBoard().getShiftCard().getPin().getPlayerID());
		shiftCard.setPin(pin);

		// schieben simulieren
		doShift(awaitMoveMessage.getBoard(), shiftCard, shiftPosition);

		// alle erreichbaren Positionen finden
		PositionType currentPosition = findPlayer(awaitMoveMessage.getBoard());
		List<PositionType> reachable = getReachablePositions(awaitMoveMessage.getBoard(), currentPosition);

		//wenn Schatz erreichbar, dort hingehen
		PositionType pinPosition = null;
		Board board = new Board(awaitMoveMessage.getBoard());
		TreasureType treasure = awaitMoveMessage.getTreasure();
		for (PositionType position : reachable) {
			Card c = new Card(board.getCard(position.getRow(), position.getCol()));
			if (c.getTreasure() == treasure) {
				pinPosition = position;
			}
		}

		if (pinPosition == null) {
			Collections.shuffle(reachable);
			pinPosition = reachable.get(0);
		}
		*/
		// zufällige Position wählen
		/*
		 * Collections.shuffle(reachable); PositionType pinPosition =
		 * reachable.get(0);
		 */
		System.out.println("Pin:" + pinPosition.getRow() + ", " + pinPosition.getCol()); 
		System.out.println("Shift:" + shiftPosition.getRow() + ", " + shiftPosition.getCol());
		if (awaitMoveMessage.getBoard().getForbidden() != null) {
			System.out.println("Forbidden:" + awaitMoveMessage.getBoard().getForbidden().getRow() + ", " + awaitMoveMessage.getBoard().getForbidden().getCol()); 
		}
		// Zug senden
		this.connection.sendMove(shiftPosition.getRow(), shiftPosition.getCol(), pinPosition.getRow(),
				pinPosition.getCol(), awaitMoveMessage.getBoard().getShiftCard());

		// Bestätigung empfangen
		AcceptMessageType acceptMessage = this.connection.getXmlInStream().readMazeCom().getAcceptMessage();
		if (!acceptMessage.isAccept()) {
			System.err.println("Fehler: " + acceptMessage.getErrorCode());
		}
	}
	
	private boolean searchMove(PositionType pinPosition, PositionType shiftPosition, int row, int col, AwaitMoveMessageType awaitMoveMessage){
		shiftPosition.setRow(row);
		shiftPosition.setCol(col);				
		if (!positionEquals(shiftPosition, awaitMoveMessage.getBoard().getForbidden())) {
			System.out.println("nicht verboten");
			// Schiebekarte kopieren
			CardType shiftCard = new CardType();
			shiftCard.setOpenings(awaitMoveMessage.getBoard().getShiftCard().getOpenings());
			shiftCard.setTreasure(awaitMoveMessage.getBoard().getShiftCard().getTreasure());
			CardType.Pin pin = new Pin();
			pin.getPlayerID().addAll(awaitMoveMessage.getBoard().getShiftCard().getPin().getPlayerID());
			shiftCard.setPin(pin);

			// schieben simulieren
			Board board = new Board(awaitMoveMessage.getBoard());
			doShift(board, shiftCard, shiftPosition);

			// alle erreichbaren Positionen finden
			PositionType currentPosition = findPlayer(board);
			List<PositionType> reachable = getReachablePositions(board, currentPosition);

			//wenn Schatz erreichbar, dort hingehen
			TreasureType treasure = awaitMoveMessage.getTreasure();
			System.out.println("Suche Schatz " + row +";" +col);
			for (PositionType position : reachable) {
				Card c = new Card(board.getCard(position.getRow(), position.getCol()));					
				if (c.getTreasure() == treasure) {
					pinPosition.setCol(position.getCol()); 
					pinPosition.setRow(position.getRow());
					System.out.println("Schatz gefunden!");
					System.out.println("Pin:" + pinPosition.getRow() + ", " + pinPosition.getCol()); 
					System.out.println("Shift:" + shiftPosition.getRow() + ", " + shiftPosition.getCol());
					return true;							
				}
			}
			
		}
		return false;
	}

	private boolean positionEquals(PositionType position1, PositionType position2) {
		if (position1 == position2) {
			return true;
		} else if (position1 == null) {
			return false;
		} else if (position2 == null) {
			return false;
		}

		return (position1.getRow() == position2.getRow()) && (position1.getCol() == position2.getCol());
	}

	private PositionType findPlayer(BoardType board) {
		// über alle Karten iterieren
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				CardType card = board.getRow().get(i).getCol().get(j);

				// Spieler gefunden
				if (card.getPin().getPlayerID().contains(this.connection.getPlayerId())) {
					PositionType position = new PositionType();
					position.setRow(i);
					position.setCol(j);
					return position;
				}
			}
		}

		// nicht gefunden
		return null;
	}

	private void doShift(BoardType board, CardType shiftCard, PositionType shiftPosition) {
		int row = shiftPosition.getRow();
		int column = shiftPosition.getCol();

		// schieben
		CardType newShift;
		if (row == 0 && column % 2 == 1) { // nach unten
			newShift = board.getRow().get(7 - 1).getCol().get(column);
			for (int shiftRow = 7 - 1; shiftRow > 0; shiftRow--) {
				board.getRow().get(shiftRow).getCol().set(column,
						board.getRow().get(shiftRow - 1).getCol().get(column));
			}
		} else if (row == 7 - 1 && column % 2 == 1) { // nach oben
			newShift = board.getRow().get(0).getCol().get(column);
			for (int shiftRow = 0; shiftRow < 7 - 1; shiftRow++) {
				board.getRow().get(shiftRow).getCol().set(column,
						board.getRow().get(shiftRow + 1).getCol().get(column));
			}
		} else if (column == 0 && row % 2 == 1) { // nach rechts
			newShift = board.getRow().get(row).getCol().get(7 - 1);
			for (int shiftColumn = 7 - 1; shiftColumn > 0; shiftColumn--) {
				board.getRow().get(row).getCol().set(shiftColumn,
						board.getRow().get(row).getCol().get(shiftColumn - 1));
			}
		} else if (column == 7 - 1 && row % 2 == 1) { // nach links
			newShift = board.getRow().get(row).getCol().get(0);
			for (int shiftColumn = 0; shiftColumn < 7 - 1; shiftColumn++) {
				board.getRow().get(row).getCol().set(shiftColumn,
						board.getRow().get(row).getCol().get(shiftColumn + 1));
			}
		} else {
			// Position ist keine gültige Schiebeposition
			throw new IllegalArgumentException();
		}

		// Schiebekarte platzieren
		board.getRow().get(row).getCol().set(column, shiftCard);

		// Spieler umsetzen wenn sie vom Brett geschoben wurden
		shiftCard.getPin().getPlayerID().clear();
		if (!newShift.getPin().getPlayerID().isEmpty()) {
			shiftCard.getPin().getPlayerID().addAll(newShift.getPin().getPlayerID());
			newShift.getPin().getPlayerID().clear();
		}
	}

	private void onWin(Winner winner) {
		System.out.println("Spiel vorbei! Gewinner hat: " + winner.getValue());
	}

	private void onDisconnect() {
		System.err.println("Verbindung getrennt!");
	}

	private PositionType randomShiftPosition() {
		PositionType position = new PositionType();

		switch (random.nextInt(4)) {
		case 0:
			position.setRow(0);
			position.setCol(this.random.nextInt(7 / 2) * 2 + 1);
			break;
		case 1:
			position.setRow(7 - 1);
			position.setCol(this.random.nextInt(7 / 2) * 2 + 1);
			break;
		case 2:
			position.setRow(this.random.nextInt(7 / 2) * 2 + 1);
			position.setCol(0);
			break;
		case 3:
			position.setRow(this.random.nextInt(7 / 2) * 2 + 1);
			position.setCol(7 - 1);
			break;
		}

		return position;
	}
	
	/*
	private PositionType shiftPosition(PositionType forbidden) {
		PositionType position = new PositionType();
		

		switch (random.nextInt(4)) {
		case 0:
			position.setRow(0);
			position.setCol(this.random.nextInt(7 / 2) * 2 + 1);
			break;
		case 1:
			position.setRow(7 - 1);
			position.setCol(this.random.nextInt(7 / 2) * 2 + 1);
			break;
		case 2:
			position.setRow(this.random.nextInt(7 / 2) * 2 + 1);
			position.setCol(0);
			break;
		case 3:
			position.setRow(this.random.nextInt(7 / 2) * 2 + 1);
			position.setCol(7 - 1);
			break;
		}

		return position;
	}
	*/

	public List<PositionType> getReachablePositions(BoardType board, PositionType start) {
		System.out.println("Suche erreichbare Positionen...");

		// Breitensuche
		Queue<PositionType> queue = new ArrayDeque<>();
		queue.add(start);

		List<PositionType> reachable = new ArrayList<>();

		while (!queue.isEmpty()) {
			PositionType current = queue.remove();
			reachable.add(current);

			CardType currentCard = board.getRow().get(current.getRow()).getCol().get(current.getCol());

			// Nachbarn hinzufügen:

			// oben
			PositionType up = new PositionType();
			up.setRow(current.getRow() - 1);
			up.setCol(current.getCol());
			if (up.getRow() >= 0 && up.getRow() < 7 && up.getCol() >= 0 && up.getCol() < 7) {
				CardType cardUp = board.getRow().get(up.getRow()).getCol().get(up.getCol());
				if (currentCard.getOpenings().isTop() && cardUp.getOpenings().isBottom()) {
					addToQueue(queue, up, reachable);
				}
			}

			// unten
			PositionType down = new PositionType();
			down.setRow(current.getRow() + 1);
			down.setCol(current.getCol());
			if (down.getRow() >= 0 && down.getRow() < 7 && down.getCol() >= 0 && down.getCol() < 7) {
				CardType cardBottom = board.getRow().get(down.getRow()).getCol().get(down.getCol());
				if (currentCard.getOpenings().isBottom() && cardBottom.getOpenings().isTop()) {
					addToQueue(queue, down, reachable);
				}
			}

			// links
			PositionType left = new PositionType();
			left.setRow(current.getRow());
			left.setCol(current.getCol() - 1);
			if (left.getRow() >= 0 && left.getRow() < 7 && left.getCol() >= 0 && left.getCol() < 7) {
				CardType cardLeft = board.getRow().get(left.getRow()).getCol().get(left.getCol());
				if (currentCard.getOpenings().isLeft() && cardLeft.getOpenings().isRight()) {
					addToQueue(queue, left, reachable);
				}
			}

			// rechts
			PositionType right = new PositionType();
			right.setRow(current.getRow());
			right.setCol(current.getCol() + 1);
			if (right.getRow() >= 0 && right.getRow() < 7 && right.getCol() >= 0 && right.getCol() < 7) {
				CardType cardRight = board.getRow().get(right.getRow()).getCol().get(right.getCol());
				if (currentCard.getOpenings().isRight() && cardRight.getOpenings().isLeft()) {
					addToQueue(queue, right, reachable);
				}
			}
		}

		return reachable;
	}

	private void addToQueue(Queue<PositionType> queue, PositionType position, List<PositionType> history) {
		// abbrechen wenn Position schon vorhanden
		for (PositionType pos : history) {
			if (positionEquals(position, pos)) {
				return;
			}
		}
		for (PositionType pos : queue) {
			if (positionEquals(position, pos)) {
				return;
			}
		}

		queue.add(position);
	}
}
