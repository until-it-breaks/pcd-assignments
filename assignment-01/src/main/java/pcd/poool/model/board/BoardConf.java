package pcd.poool.model.board;

import pcd.poool.model.Hole;
import pcd.poool.model.ball.Ball;

import java.util.List;

public interface BoardConf {

	Boundary getBoardBoundary();

	Ball getPlayerBall();

	Ball getBotBall();
	
	List<Ball> getSmallBalls();

	List<Hole> getHoles();
}
