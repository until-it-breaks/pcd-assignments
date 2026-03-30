package pcd.poool.model.board;

import pcd.poool.model.common.Boundary;
import pcd.poool.model.common.Hole;
import pcd.poool.model.ball.Ball;

import java.util.List;

public interface BoardConf {

	Boundary getBoardBoundary();

	Ball getPlayerBall();

	Ball getBotBall();
	
	List<Ball> getSmallBalls();

	List<Hole> getHoles();
}
