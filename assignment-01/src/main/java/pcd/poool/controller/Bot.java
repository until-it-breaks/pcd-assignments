package pcd.poool.controller;

import pcd.poool.controller.commands.BotMoveCommand;
import pcd.poool.controller.commands.CommandProcessor;
import pcd.poool.model.Board;
import pcd.poool.model.V2d;

import java.util.Random;

public class Bot extends Thread {
    private final Board board;
    private final CommandProcessor commandProcessor;
    private final Random random;
    private volatile boolean running;

    public Bot(Board board, CommandProcessor commandProcessor) {
        super("BotThread");
        this.board = board;
        this.commandProcessor = commandProcessor;
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
                commandProcessor.notifyNewCommand(new BotMoveCommand(velocity));
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
