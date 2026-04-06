package pcd.poool.controller.ai;

import pcd.poool.controller.commands.BotMoveCommand;
import pcd.poool.controller.commands.CommandQueue;
import pcd.poool.controller.engine.GameEngineListener;
import pcd.poool.controller.engine.GameOverEvent;
import pcd.poool.controller.engine.EngineTimeoutEvent;
import pcd.poool.model.board.Board;
import pcd.poool.model.common.V2d;

import java.util.Random;

public class Bot implements Runnable, GameEngineListener {
    private final Board board;
    private final CommandQueue commandQueue;
    private final Random random = new Random();
    private volatile boolean running = true;

    public Bot(Board board, CommandQueue commandQueue) {
        this.board = board;
        this.commandQueue = commandQueue;
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

    @Override
    public void onEngineTimeout(EngineTimeoutEvent event) {
        running = false;
    }

    @Override
    public void onGameOver(GameOverEvent event) {
        running = false;
    }
}
