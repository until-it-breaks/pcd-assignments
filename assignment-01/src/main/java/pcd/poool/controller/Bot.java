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

    public Bot(Board board, CommandProcessor commandProcessor) {
        this.board = board;
        this.commandProcessor = commandProcessor;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (true) {
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
}
