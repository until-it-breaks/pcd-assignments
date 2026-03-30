package pcd.poool.model.core;

public record GameOver(GameOverReason reason, int playerScore, int botScore) {

    public String getMessage() {
        return switch (reason) {
            case NO_BALLS_LEFT -> String.format("Game over! Final score: %d - %d", playerScore, botScore);
            case PLAYER_FOUL -> "Bot wins! Player committed a foul.";
            case BOT_FOUL -> "Player wins! Bot committed a foul.";
        };
    }
}