package pcd.poool.model.board;

import pcd.poool.model.common.Boundary;
import pcd.poool.model.common.Hole;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;
import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.BallImplementation;

import java.util.ArrayList;
import java.util.List;

public class MinimalBoardConf implements BoardConf {

	@Override
	public Ball getPlayerBall() {
		return new BallImplementation(new P2d(-1.0, -0.75), 0.05, 1.5, new V2d(0,1));
	}

	@Override
	public Ball getBotBall() {
		return new BallImplementation(new P2d(1.0, -0.75), 0.05, 1.5, new V2d(0,1));
	}

	@Override
	public List<Ball> getSmallBalls() {
        var balls = new ArrayList<Ball>();
    	var b1 = new BallImplementation(new P2d(0, 0.5), 0.05, 0.75, new V2d(0,0));
    	var b2 = new BallImplementation(new P2d(0.05, 0.55), 0.025, 0.25, new V2d(0,0));
    	balls.add(b1);
    	balls.add(b2);
    	return balls;
	}

	@Override
	public Boundary getBoardBoundary() {
        return new Boundary(-1.5,-1.0,1.5,1.0);
	}

	@Override
	public List<Hole> getHoles() {
		return List.of(new Hole(new P2d(-1.5, 1.0), 0.15), new Hole(new P2d(1.5, 1.0), 0.15));
	}
}
