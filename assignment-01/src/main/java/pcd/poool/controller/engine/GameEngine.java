package pcd.poool.controller.engine;

import pcd.poool.controller.commands.Command;
import pcd.poool.controller.commands.CommandQueue;
import pcd.poool.model.board.Board;
import pcd.poool.model.core.GameOver;
import pcd.poool.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameEngine implements Runnable {
    private final CommandQueue commandQueue;

    private final Board board;
    private final View view;
    private volatile boolean running;

    private long totalTimeRunning;
    private long numberOfFrames;
    private final long maxSimulationTime;

    private final List<GameEngineListener> listeners;

    private GameOver gameOverDetails;

    public GameEngine(CommandQueue commandQueue, Board board, View view, long maxSimulationTime) {
        this.commandQueue = commandQueue;
        this.board = board;
        this.view = view;
        this.running = true;
        this.numberOfFrames = 0;
        this.maxSimulationTime = maxSimulationTime * 1000;
        this.listeners = new ArrayList<>();
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
            totalTimeRunning = System.currentTimeMillis() - startTime;
            if (totalTimeRunning > 0) {
                framesPerSecond = (int) (numberOfFrames * 1000 / totalTimeRunning);
            }
            view.getViewModel().update(board, framesPerSecond);
            view.render();
            checkEndOfSimulation();
        }
        notifyListeners();
        log("Simulation is over. Total frames: " + this.numberOfFrames);
    }

    private void checkEndOfSimulation() {
        if (board.isGameOver()) {
            this.gameOverDetails = board.getGameOver();
            this.running = false;
        }
        if (maxSimulationTime != 0 && totalTimeRunning > maxSimulationTime) {
            this.running = false;
        }
    }

    private void processInputs() {
        for (Command command: commandQueue.fetchAllCommands()) {
            command.execute(board);
        }
    }

    public void addListener(GameEngineListener listener) {
        this.listeners.add(listener);
    }

    public void notifyListeners() {
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
