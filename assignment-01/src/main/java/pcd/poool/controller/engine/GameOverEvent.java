package pcd.poool.controller.engine;

import pcd.poool.model.core.GameOverDetails;

public class GameOverEvent {
    private final GameOverDetails details;

    public GameOverEvent(GameOverDetails details) {
        this.details = details;
    }

    public GameOverDetails gameOverDetails() {
        return this.details;
    }
}