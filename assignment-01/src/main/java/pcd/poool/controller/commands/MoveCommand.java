package pcd.poool.controller.commands;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.board.Board;
import pcd.poool.model.common.V2d;

public class MoveCommand implements Command {
    private final V2d velocity;

    public MoveCommand(V2d velocity) {
        this.velocity = velocity;
    }

    @Override
    public void execute(Board board) {
        Ball playerBall = board.getPlayerBall();
        if (playerBall != null) {
            playerBall.kick(velocity);
        }
    }
}