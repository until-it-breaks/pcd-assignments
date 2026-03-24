package pcd.poool.controller;

import pcd.poool.model.Board;
import pcd.poool.model.V2d;

public class CombinedMoveCommand implements Command {
    private final V2d velocity;

    public CombinedMoveCommand(V2d velocity) {
        this.velocity = velocity;
    }

    @Override
    public void execute(Board board) {
        board.getPlayerBall().kick(velocity);
    }
}