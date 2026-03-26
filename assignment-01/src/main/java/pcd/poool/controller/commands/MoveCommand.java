package pcd.poool.controller.commands;

import pcd.poool.model.Ball;
import pcd.poool.model.Board;
import pcd.poool.model.V2d;

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