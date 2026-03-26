package pcd.poool.controller.commands;

import pcd.poool.model.Ball;
import pcd.poool.model.Board;
import pcd.poool.model.V2d;

public class BotMoveCommand implements Command {
    private final V2d velocity;

    public BotMoveCommand(V2d velocity) {
        this.velocity = velocity;
    }

    @Override
    public void execute(Board board) {
        Ball botBall = board.getBotBall();
        if (botBall != null) {
            botBall.kick(velocity);
        }
    }
}
