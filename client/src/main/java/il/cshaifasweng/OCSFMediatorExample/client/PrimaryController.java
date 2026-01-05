package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.TicTacToeMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class PrimaryController {

	@FXML
	private GridPane gridBoard;

	@FXML
	private Label statusLabel;

	@FXML
	void initialize() {
		// Register to EventBus to receive messages from the server
		EventBus.getDefault().register(this);
	}

	@FXML
	void onSquareClick(ActionEvent event) {
		// Identify the clicked button
		Button clickedButton = (Button) event.getSource();

		// If the button is already taken (has text), do nothing
		if (!clickedButton.getText().isEmpty()) {
			return;
		}

		// Calculate row and column indices based on GridPane position
		Integer rowIndex = GridPane.getRowIndex(clickedButton);
		Integer colIndex = GridPane.getColumnIndex(clickedButton);

		// Handle null values (sometimes happens for row/col 0 in JavaFX)
		int row = (rowIndex == null) ? 0 : rowIndex;
		int col = (colIndex == null) ? 0 : colIndex;

		System.out.println("Clicked on: " + row + ", " + col);

		// Send the move to the server
		try {
			// We send "UNKNOWN" as player; the server will decide whose turn it is
			TicTacToeMessage moveMsg = new TicTacToeMessage("UNKNOWN", row, col);
			SimpleClient.getClient().sendToServer(moveMsg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// This method is called when a TicTacToeMessage is received via EventBus
	@Subscribe
	public void onGameEvent(TicTacToeMessage message) {
        System.out.println("Got message: " + message.getAction() + " " + message.getContent());
		Platform.runLater(() -> {
			if (message.getAction().equals("MOVE")) {
				updateBoard(message.getRow(), message.getCol(), message.getPlayer());
			} else if (message.getAction().equals("GAME_OVER")) {
				statusLabel.setText(message.getContent());
				disableAllButtons();
			} else if (message.getAction().equals("START")) {
				statusLabel.setText(message.getContent());
			} else if (message.getAction().equals("ERROR")) {
				statusLabel.setText(message.getContent());
			} else if (message.getAction().equals("MESSAGE")) {
                statusLabel.setText(message.getContent());
            } else if (message.getAction().equals("RESET")){
                for (javafx.scene.Node node : gridBoard.getChildren()) {
                    if (node instanceof Button) {
                        Button btn = (Button) node;
                        btn.setText("");
                        btn.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-border-color: gray;");
                        btn.setDisable(false);
                    }
                }
            }
		});
	}

	private void updateBoard(int row, int col, String player) {
		// Find the specific button in the grid and update its text
		for (javafx.scene.Node node : gridBoard.getChildren()) {
			if (node instanceof Button) {
				Integer r = GridPane.getRowIndex(node);
				Integer c = GridPane.getColumnIndex(node);
				int nodeRow = (r == null) ? 0 : r;
				int nodeCol = (c == null) ? 0 : c;

				if (nodeRow == row && nodeCol == col) {
					Button btn = (Button) node;
					btn.setText(player);
					btn.setDisable(true); // Disable button after move

					String baseStyle = "-fx-font-size: 40px; -fx-font-weight: bold; -fx-border-color: gray;";
					if (player.equals("X")) {
						// LightPink for X
						btn.setStyle("-fx-background-color: #FFB6C1; " + baseStyle);
					} else {
						// LemonChiffon for O
						btn.setStyle("-fx-background-color: #FFFACD; " + baseStyle);
					}
					break;
				}
			}
		}
	}

	private void disableAllButtons() {
		gridBoard.getChildren().forEach(node -> node.setDisable(true));
	}
}