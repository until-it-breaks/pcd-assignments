package pcd.poool.controller;

import pcd.poool.model.Board;
import pcd.poool.model.V2d;

public class BotMoveCommand implements Command {
    private final V2d velocity;

    public BotMoveCommand(V2d velocity) {
        this.velocity = velocity;
    }

    @Override
    public void execute(Board board) {
        board.getBotBall().kick(velocity);
    }
}
