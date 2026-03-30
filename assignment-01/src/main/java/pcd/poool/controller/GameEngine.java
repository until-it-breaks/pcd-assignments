package pcd.poool.controller;

import pcd.poool.controller.commands.Command;
import pcd.poool.controller.commands.CommandQueue;
import pcd.poool.model.board.Board;
import pcd.poool.view.View;

public class GameEngine implements Runnable {
    private final CommandQueue commandQueue;

    private final Board board;
    private final View view;
    private volatile boolean running;

    public GameEngine(CommandQueue commandQueue, Board board, View view) {
        this.commandQueue = commandQueue;
        this.board = board;
        this.view = view;
        this.running = true;
    }

    @Override
    public void run() {
        int numberOfFrames = 0;
        long startTime = System.currentTimeMillis();
        long lastUpdateTime = System.currentTimeMillis();

        while (running) {
            long timeElapsed = System.currentTimeMillis() - lastUpdateTime;
            lastUpdateTime = System.currentTimeMillis();
            processInputs();
            board.updateState(timeElapsed);
            numberOfFrames++;
            int framesPerSecond = 0;
            long totalTimeRunning = System.currentTimeMillis() - startTime;
            if (totalTimeRunning > 0) {
                framesPerSecond = (int) (numberOfFrames * 1000 / totalTimeRunning);
            }
            view.getViewModel().update(board, framesPerSecond);
            view.render();
        }
    }

    public void stopEngine() {
        running = false;
    }

    private void processInputs() {
        for (Command command: commandQueue.fetchAllCommands()) {
            command.execute(board);
        }
    }
}
