package pcd.poool.model;

import java.util.Iterator;
import java.util.List;

public class Board {

    private List<Ball> balls;
    private Ball playerBall;
    private Ball botBall;
    private Boundary bounds;
    private List<Hole> holes;

    private int playerScore;
    private int botScore;
    
    public Board(){} 
    
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();
    	playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
    	bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        playerScore = 0;
        botScore = 0;
    }
    
    public void updateState(long dt) {
    	playerBall.updateState(dt, this);
        botBall.updateState(dt, this);
        Iterator<Ball> it = balls.iterator();
        while (it.hasNext()) {
            Ball ball = it.next();
            ball.updateState(dt, this);
            if (isInHole(ball)) {
                if (ball.getLastCollider() == playerBall) {
                    increasePlayerScore();
                } else if (ball.getLastCollider() == botBall) {
                    increaseBotScore();
                }
                it.remove();
            }
        }
    	
    	for (int i = 0; i < balls.size() - 1; i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball.resolveCollision(balls.get(i), balls.get(j));
            }
        }
    	for (var b: balls) {
    		Ball.resolveCollision(playerBall, b);
            Ball.resolveCollision(botBall, b);
    	}
        Ball.resolveCollision(playerBall, botBall);
    }
    
    public List<Ball> getBalls(){
    	return balls;
    }
    
    public Ball getPlayerBall() {
    	return playerBall;
    }

    public Ball getBotBall() {
        return botBall;
    }
    
    public Boundary getBounds(){
        return bounds;
    }

    public void increasePlayerScore() {
        playerScore++;
    }

    public void increaseBotScore() {
        botScore++;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getBotScore() {
        return botScore;
    }

    public List<Hole> getHoles() {
        return holes;
    }

    private boolean isInHole(Ball ball) {
        P2d pos = ball.getPos();
        for (var hole: holes) {
            if (isInside(pos, hole)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInside(P2d pos, Hole hole) {
        double dx = pos.x() - hole.pos().x();
        double dy = pos.y() - hole.pos().y();
        return Math.hypot(dx, dy) < hole.radius();
    }
}
