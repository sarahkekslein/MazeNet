package timeouts;

import java.util.TimerTask;

import generated.ErrorType;
import networking.Connection;

public class SendMessageTimeout extends TimerTask {

	private Connection con;

	public SendMessageTimeout(Connection con) {
		this.con = con;
	}

	@Override
	public void run() {
		this.con.disconnect(ErrorType.TIMEOUT);
	}

}
