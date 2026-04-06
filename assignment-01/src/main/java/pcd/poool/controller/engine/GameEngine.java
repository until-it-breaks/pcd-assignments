package pcd.poool.controller.engine;

import pcd.poool.controller.commands.Command;
import pcd.poool.controller.commands.CommandQueue;
import pcd.poool.model.board.Board;
import pcd.poool.model.core.GameOverDetails;
import pcd.poool.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameEngine implements Runnable {
    private static final int SECOND_IN_MILLIS = 1000;
    public static final int UNLIMITED_SIMULATION_TIME = 0;
    private final CommandQueue commandQueue;
    private final Board board;
    private final View view;
    private volatile boolean running = true;
    private long totalTimeRunningInMillis;
    private long numberOfFrames = 0;
    private final long maxSimulationTime;
    private GameOverDetails gameOverDetails;
    private final List<GameEngineListener> listeners = new ArrayList<>();

    public GameEngine(CommandQueue commandQueue, Board board, View view, long maxSimulationTimeInSeconds) {
        this.commandQueue = commandQueue;
        this.board = board;
        this.view = view;
        this.maxSimulationTime = maxSimulationTimeInSeconds * SECOND_IN_MILLIS;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long lastUpdateTime = System.currentTimeMillis();
        while (running) {
            long timeElapsed = System.currentTimeMillis() - lastUpdateTime;
            lastUpdateTime = System.currentTimeMillis();
            processInputs();
            board.updateState(timeElapsed);
            numberOfFrames++;
            int framesPerSecond = 0;
            totalTimeRunningInMillis = System.currentTimeMillis() - startTime;
            if (totalTimeRunningInMillis > 0) {
                framesPerSecond = (int) (numberOfFrames * SECOND_IN_MILLIS / totalTimeRunningInMillis);
            }
            view.getViewModel().update(board, framesPerSecond);
            view.render();
            checkEndOfSimulation();
        }
        log("Simulation is over. Total frames: " + this.numberOfFrames);
    }

    private void processInputs() {
        for (Command command: commandQueue.fetchAllCommands()) {
            command.execute(board);
        }
    }

    private void checkEndOfSimulation() {
        if (board.isGameOver()) {
            this.gameOverDetails = board.getGameOverDetails();
            this.running = false;
            notifyListeners();
        }
        if (maxSimulationTime != UNLIMITED_SIMULATION_TIME && totalTimeRunningInMillis > maxSimulationTime) {
            this.running = false;
            notifyListeners();
        }
    }

    public void addListener(GameEngineListener listener) {
        this.listeners.add(listener);
    }

    private void notifyListeners() {
        for (GameEngineListener listener: listeners) {
            if (gameOverDetails != null) {
                listener.onGameOver(new GameOverEvent(gameOverDetails));
            } else {
                listener.onEngineTimeout(new EngineTimeoutEvent());
            }
        }
    }

    private void log(String msg) {
        System.out.println("[" + new Date(System.currentTimeMillis()) + "][" + this.getClass().getSimpleName() + "] " + msg);
    }
}
