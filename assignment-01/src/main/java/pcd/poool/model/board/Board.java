package pcd.poool.model.board;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.collision.CollisionResolver;
import pcd.poool.model.common.Boundary;
import pcd.poool.model.common.Hole;
import pcd.poool.model.common.P2d;
import pcd.poool.model.core.GameOverDetails;
import pcd.poool.model.core.GameOverReason;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Board {
    private List<Ball> balls;
    private List<Hole> holes;
    private Ball playerBall;
    private Ball botBall;
    private Boundary bounds;
    private int playerScore = 0;
    private int botScore = 0;
    private GameOverDetails gameOverDetails;
    private CollisionResolver collisionResolver;
    
    public void init(BoardConf conf, CollisionResolver resolver) {
    	this.balls = conf.getSmallBalls();
        this.playerBall = conf.getPlayerBall();
        this.botBall = conf.getBotBall();
        this.bounds = conf.getBoardBoundary();
        this.holes = conf.getHoles();
        this.collisionResolver = resolver;
    }

    public void updateState(long dt) {
        updateBalls(dt);
        checkCollisions();
        checkWinConditions();
    }

    private void updateBalls(long dt) {
        if (playerBall != null) {
            playerBall.updateState(dt, this);
        }
        if (botBall != null) {
            botBall.updateState(dt, this);
        }
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            ball.updateState(dt, this);
            if (isBallInHole(ball)) {
                handleScoring(ball);
                balls.remove(i);
                i--;
            }
        }
    }

    private void handleScoring(Ball ball) {
        Set<Ball> lastColliders = ball.getLatestColliders();
        if (lastColliders.contains(playerBall)) {
            playerScore++;
        }
        if (lastColliders.contains(botBall)) {
            botScore++;
        }
    }

    private boolean isBallInHole(Ball ball) {
        if (ball == null) {
            return false;
        }
        P2d pos = ball.getPos();
        for (Hole hole: holes) {
            double dx = pos.x() - hole.pos().x();
            double dy = pos.y() - hole.pos().y();
            if (Math.hypot(dx, dy) < hole.radius()) {
                return true;
            }
        }
        return false;
    }

    private void checkCollisions() {
        List<Ball> collisionGroup = new ArrayList<>();
        collisionGroup.add(playerBall);
        collisionGroup.add(botBall);
        collisionGroup.addAll(balls);
        try {
            collisionResolver.resolve(collisionGroup);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkWinConditions() {
        if (isBallInHole(playerBall)) {
            playerBall = null;
            setGameOverDetails(GameOverReason.PLAYER_FOUL);
        }
        if (isBallInHole(botBall)) {
            botBall = null;
            setGameOverDetails(GameOverReason.BOT_FOUL);
        }
        if (balls.isEmpty()) {
            setGameOverDetails(GameOverReason.NO_BALLS_LEFT);
        }
    }

    private void setGameOverDetails(GameOverReason reason) {
        gameOverDetails = new GameOverDetails(reason, playerScore, botScore);
    }
    
    public List<Ball> getBalls(){
    	return balls;
    }

    public List<Hole> getHoles() {
        return holes;
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

    public int getPlayerScore() {
        return playerScore;
    }

    public int getBotScore() {
        return botScore;
    }

    public boolean isGameOver() {
        return gameOverDetails != null;
    }

    public GameOverDetails getGameOverDetails() {
        return gameOverDetails;
    }
}
