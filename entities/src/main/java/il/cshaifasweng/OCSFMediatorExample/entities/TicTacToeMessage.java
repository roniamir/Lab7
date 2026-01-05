package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class TicTacToeMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String player; // "X" or "O"
    private int row;
    private int col;
    private String action; // "MOVE", "GAME_OVER", "START", "ERROR"
    private String content; // Free text (e.g. "X Won!", "Draw")

    // Empty constructor
    public TicTacToeMessage() {}

    // Constructor for a move
    public TicTacToeMessage(String player, int row, int col) {
        this.player = player;
        this.row = row;
        this.col = col;
        this.action = "MOVE";
    }

    // Constructor for general updates (Start, Game Over)
    public TicTacToeMessage(String action, String content) {
        this.action = action;
        this.content = content;
    }

    // Getters and Setters
    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}