package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.TicTacToeMessage;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class SimpleClient extends AbstractClient {

	private static SimpleClient client = null;

	private SimpleClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		// If the message is related to the game, post it to the GUI controller
		if (msg.getClass().equals(TicTacToeMessage.class)) {
			EventBus.getDefault().post((TicTacToeMessage) msg);
		}
	}

	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}
}