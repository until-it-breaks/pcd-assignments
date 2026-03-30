package pcd.poool.controller;

import pcd.poool.controller.commands.BotMoveCommand;
import pcd.poool.controller.commands.CommandQueue;
import pcd.poool.model.board.Board;
import pcd.poool.model.V2d;

import java.util.Random;

public class Bot implements Runnable {
    private final Board board;
    private final CommandQueue commandQueue;
    private final Random random;
    private volatile boolean running;

    public Bot(Board board, CommandQueue commandQueue) {
        this.board = board;
        this.commandQueue = commandQueue;
        this.random = new Random();
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            var botBall = board.getBotBall();
            if (botBall != null) {
                double angle = random.nextDouble() * Math.PI * 2;
                V2d velocity = new V2d(Math.cos(angle), Math.sin(angle)).mul(1.5);
                commandQueue.notifyNewCommand(new BotMoveCommand(velocity));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void stopBot() {
        running = false;
    }
}
