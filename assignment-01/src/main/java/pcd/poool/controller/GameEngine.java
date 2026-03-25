package pcd.poool.controller;

import pcd.poool.model.Board;
import pcd.poool.view.View;
import pcd.poool.view.ViewModel;

public class GameEngine extends Thread {
    private final Board board;
    private final ViewModel viewModel;
    private final View view;

    public GameEngine(Board board, ViewModel viewModel, View view) {
        this.board = board;
        this.viewModel = viewModel;
        this.view = view;
    }

    @Override
    public void run() {
        int numberOfFrames = 0;
        long startTime = System.currentTimeMillis();
        long lastUpdateTime = System.currentTimeMillis();

        while (true) {
            long timeElapsed = System.currentTimeMillis() - lastUpdateTime;
            lastUpdateTime = System.currentTimeMillis();
            board.updateState(timeElapsed);
            numberOfFrames++;
            int framesPerSecond = 0;
            long totalTimeRunning = System.currentTimeMillis() - startTime;
            if (totalTimeRunning > 0) {
                framesPerSecond = (int) (numberOfFrames * 1000 / totalTimeRunning);
            }
            viewModel.update(board, framesPerSecond);
            view.render();
        }
    }
}
