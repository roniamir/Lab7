package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.TicTacToeMessage;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SimpleServer extends AbstractServer {

	private String[][] board = new String[3][3]; // The game board
	private String currentPlayer = "X"; // X always starts
	private ConnectionToClient playerX = null;
	private ConnectionToClient playerO = null;// Count connections

	public SimpleServer(int port) {
		super(port);
		resetBoard(); // Initialize empty board
	}

	private void resetBoard() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				board[i][j] = "";
			}
		}
        sendToAllClients(new TicTacToeMessage("RESET", "RESET CONTENT"));
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		// We expect a TicTacToeMessage from the client
		if (msg instanceof TicTacToeMessage) {
			TicTacToeMessage message = (TicTacToeMessage) msg;

			// 1. Check if we have 2 players
			if (playerX == null || playerO == null) {
				sendToClient(client, new TicTacToeMessage("ERROR", "Waiting for second player..."));
				return;
			}

			// 2. Identify the player based on connection ID (Simple logic: first is X, second is O)
			if (currentPlayer.equals("X") && client != playerX) {
				return;
			}
			if (currentPlayer.equals("O") && client != playerO) {
				return;
			}

			// 3. Process the Move
			int row = message.getRow();
			int col = message.getCol();

			// Check if cell is empty
			if (board[row][col].isEmpty()) {
				board[row][col] = currentPlayer; // Place X or O

				// Broadcast the move to BOTH players so they can update their screens
				sendToAllClients(new TicTacToeMessage(currentPlayer, row, col));
                if(currentPlayer.equals("X")) {
                    sendToClient(playerX, new TicTacToeMessage( "MESSAGE","You are player X. Wait for your opponent's turn."));
                    sendToClient(playerO, new TicTacToeMessage( "MESSAGE","You are player O. Your turn."));
                }else{
                    sendToClient(playerX, new TicTacToeMessage( "MESSAGE","You are player X. Your turn."));
                    sendToClient(playerO, new TicTacToeMessage( "MESSAGE","You are player O. Wait for your opponent's turn."));
                }

				// 4. Check for Win
				if (checkWin(currentPlayer)) {
					sendToAllClients(new TicTacToeMessage("GAME_OVER", "Player " + currentPlayer + " Wins!"));
					resetBoard(); // Reset for next game
				}
				// 5. Check for Draw (Board full)
				else if (isBoardFull()) {
					sendToAllClients(new TicTacToeMessage("GAME_OVER", "It's a Draw!"));
					resetBoard();
				}
				// 6. Switch Turn
				else {
					currentPlayer = (currentPlayer.equals("X")) ? "O" : "X";
				}
			}
		}
	}

	// Helper method to send message to a specific client safely
	private void sendToClient(ConnectionToClient client, Object msg) {
		try {
			client.sendToClient(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void clientConnected(ConnectionToClient client) {

		System.out.println("Client connected: " + client);

		if (playerX == null) {
			playerX = client;
			sendToClient(client, new TicTacToeMessage("START", "Waiting for opponent..."));
		} else if (playerO == null) {
			playerO = client;

            ConnectionToClient firstPlayer = playerX;
            ConnectionToClient secondPlayer = playerO;
            Random random = new Random();


            if(random.nextBoolean()) {
                playerX = secondPlayer;
                playerO = firstPlayer;
            }

			//sendToAllClients(new TicTacToeMessage("START", "Game Started! Player X goes first."));
            try {
                TimeUnit.SECONDS.sleep(1);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendToClient(playerX, new TicTacToeMessage("START", "You are player X. Your turn" ));
            sendToClient(playerO, new TicTacToeMessage("START", "You are player O. Wait for your opponent's turn"));
			currentPlayer = "X";
			resetBoard();
		}
	}

	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		if (client == playerX) playerX = null;
		if (client == playerO) playerO = null;
        if (playerO != null && playerX == null) {playerX = playerO; playerO = null;}
		sendToAllClients(new TicTacToeMessage("ERROR", "Opponent disconnected. Resetting game."));
		resetBoard();
		currentPlayer = "X";
	}

	// --- Game Logic Helpers ---

	private boolean checkWin(String player) {
		// Rows
		for (int i = 0; i < 3; i++) {
			if (board[i][0].equals(player) && board[i][1].equals(player) && board[i][2].equals(player)) return true;
		}
		// Columns
		for (int i = 0; i < 3; i++) {
			if (board[0][i].equals(player) && board[1][i].equals(player) && board[2][i].equals(player)) return true;
		}
		// Diagonals
		if (board[0][0].equals(player) && board[1][1].equals(player) && board[2][2].equals(player)) return true;
		if (board[0][2].equals(player) && board[1][1].equals(player) && board[2][0].equals(player)) return true;

		return false;
	}

	private boolean isBoardFull() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board[i][j].isEmpty()) return false;
			}
		}
		return true;
	}
}