package it.unibo.shared;

public enum GameStatus {
    WAITING_FOR_PLAYERS("Waiting for an opponent to join..."),
    ONGOING("Match in progress!"),
    X_WON("Player X Wins!"),
    O_WON("Player O Wins!"),
    DRAW("Draw");

    private final String displayMessage;

    GameStatus(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    @Override
    public String toString() {
        return displayMessage;
    }
}