package pcd.poool.controller;

import pcd.poool.controller.commands.Command;
import pcd.poool.model.Board;
import pcd.poool.model.V2d;

public class MoveCommand implements Command {
    private final V2d velocity;

    public MoveCommand(V2d velocity) {
        this.velocity = velocity;
    }

    @Override
    public void execute(Board board) {
        board.getPlayerBall().kick(velocity);
    }
}