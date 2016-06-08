package de.crazychicken.mazenet;

import generated.MazeCom;
import generated.MazeComType;
import generated.MoveMessageType;
import generated.ObjectFactory;

public class MessageFactory {
	private final ObjectFactory objectFactory;

	public MessageFactory() {
		this.objectFactory = new ObjectFactory();
	}

	public MazeCom createLoginMessage() {
		MazeCom mc = objectFactory.createMazeCom();
		mc.setMcType(MazeComType.LOGIN);
		mc.setId(-1);

		mc.setLoginMessage(objectFactory.createLoginMessageType());
		mc.getLoginMessage().setName("crAzy chIcken");

		return mc;
	}

	public MazeCom createMoveMessage(int id, MoveMessageType move) {
		MazeCom mc = objectFactory.createMazeCom();
		mc.setMcType(MazeComType.MOVE);
		mc.setId(id);

		mc.setMoveMessage(move);

		return mc;
	}
}
