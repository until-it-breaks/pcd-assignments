package pcd.poool.controller.engine;

public interface GameEngineListener {
    void onEngineTimeout(EngineTimeoutEvent event);

    void onGameOver(GameOverEvent event);
}
