package pcd.poool.model.board;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.common.Boundary;
import pcd.poool.model.common.Hole;

import java.util.List;

public class HolelessBoard implements BoardConf {
    private final BoardConf boardConf;

    public HolelessBoard(BoardConf boardConf) {
        this.boardConf = boardConf;
    }

    @Override
    public Boundary getBoardBoundary() {
        return boardConf.getBoardBoundary();
    }

    @Override
    public Ball getPlayerBall() {
        return boardConf.getPlayerBall();
    }

    @Override
    public Ball getBotBall() {
        return boardConf.getBotBall();
    }

    @Override
    public List<Ball> getSmallBalls() {
        return boardConf.getSmallBalls();
    }

    @Override
    public List<Hole> getHoles() {
        return List.of();
    }
}
