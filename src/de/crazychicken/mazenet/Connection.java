package de.crazychicken.mazenet;

import java.io.IOException;

import javax.xml.bind.UnmarshalException;

import generated.CardType;
import generated.MazeCom;
import generated.MazeComType;
import generated.MoveMessageType;
import generated.PositionType;
import networking.XmlInStream;
import networking.XmlOutStream;

public class Connection {
	private final XmlInStream xmlInStream;
	private final XmlOutStream xmlOutStream;

	private final MessageFactory messageFactory;

	private int playerId;

	public Connection(XmlInStream xmlInStream, XmlOutStream xmlOutStream) {
		this.xmlInStream = xmlInStream;
		this.xmlOutStream = xmlOutStream;

		this.messageFactory = new MessageFactory();
	}

	public void sendLogin() throws UnmarshalException, IOException {
		this.xmlOutStream.write(this.messageFactory.createLoginMessage());

		MazeCom response;
		response = this.xmlInStream.readMazeCom();

		if (response.getMcType() == MazeComType.LOGINREPLY) {
			// nice
			this.playerId = response.getLoginReplyMessage().getNewID();
		} else if (response.getMcType() == MazeComType.ACCEPT) {
			// oops, accept should be false, something went wrong :(
		} else if (response.getMcType() == MazeComType.DISCONNECT) {
			// dayum
		}
	}

	public void sendMove(int shiftRow, int shiftColumn, int moveRow, int moveColumn, CardType shiftCard) throws UnmarshalException, IOException {
		MoveMessageType moveMessage = new MoveMessageType();

		// shift position
		PositionType shiftPositionType = new PositionType();
		shiftPositionType.setRow(shiftRow);
		shiftPositionType.setCol(shiftColumn);
		moveMessage.setShiftPosition(shiftPositionType);

		// pins position
		PositionType pinPositionType = new PositionType();
		pinPositionType.setRow(moveRow);
		pinPositionType.setCol(moveColumn);
		moveMessage.setNewPinPos(pinPositionType);

		// the turned shift card to place
		moveMessage.setShiftCard(shiftCard);

		System.out.println("Sende Zug...");
		this.xmlOutStream.write(this.messageFactory.createMoveMessage(this.playerId, moveMessage));
	}

	public int getPlayerId() {
		return playerId;
	}

	public XmlInStream getXmlInStream() {
		return xmlInStream;
	}

	public XmlOutStream getXmlOutStream() {
		return xmlOutStream;
	}
}
