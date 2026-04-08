package pcd.poool.model.core;

public class GameOverDetails {
    private final GameOverReason reason;
    private final int playerScore;
    private final int botScore;

    public GameOverDetails(GameOverReason reason, int playerScore, int botScore) {
        this.reason = reason;
        this.playerScore = playerScore;
        this.botScore = botScore;
    }

    public GameOverReason getReason() {
        return reason;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getBotScore() {
        return botScore;
    }

    public String getMessage() {
        switch (reason) {
            case NO_BALLS_LEFT:
                return String.format("Game over! Final score: %d - %d", playerScore, botScore);
            case PLAYER_FOUL:
                return "Bot wins! Player committed a foul.";
            case BOT_FOUL:
                return "Player wins! Bot committed a foul.";
            default:
                throw new IllegalStateException("Unexpected value: " + reason);
        }
    }
}