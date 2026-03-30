package pcd.poool.model.board;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.collision.CollisionResolver;
import pcd.poool.model.common.Boundary;
import pcd.poool.model.common.Hole;
import pcd.poool.model.common.P2d;
import pcd.poool.model.core.BoardListener;
import pcd.poool.model.core.GameOver;
import pcd.poool.model.core.GameOverReason;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private List<Ball> balls;
    private List<Hole> holes;
    private Ball playerBall;
    private Ball botBall;
    private Boundary bounds;
    private int playerScore;
    private int botScore;
    private GameOver gameOver;

    private CollisionResolver collisionResolver;
    private final List<Ball> collisionGroup = new ArrayList<>();

    private final List<BoardListener> listeners = new ArrayList<>();
    
    public void init(BoardConf conf, CollisionResolver resolver) {
        this.collisionResolver = resolver;
    	balls = conf.getSmallBalls();
    	playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
    	bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        playerScore = 0;
        botScore = 0;
        gameOver = null;
    }

    public void updateState(long dt) {
        updateMainBalls(dt);
        updateSmallBalls(dt);
        collisionGroup.clear();
        if (playerBall != null) collisionGroup.add(playerBall);
        if (botBall != null) collisionGroup.add(botBall);
        collisionGroup.addAll(balls);
        try {
            collisionResolver.resolve(collisionGroup);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        checkGameOverConditions();
    }

    private void updateMainBalls(long dt) {
        if (playerBall != null) playerBall.updateState(dt, this);
        if (botBall != null) botBall.updateState(dt, this);
    }

    private void updateSmallBalls(long dt) {
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            ball.updateState(dt, this);
            if (isInHole(ball)) {
                handleScoring(ball);
                balls.remove(i);
                i--;
            }
        }
    }

    private void handleScoring(Ball ball) {
        Ball last = ball.getLastCollider();
        if (last == playerBall) increasePlayerScore();
        else if (last == botBall) increaseBotScore();
    }

    private void checkGameOverConditions() {
        if (playerBall != null && isInHole(playerBall)) {
            playerBall = null;
            setGameOver(GameOverReason.PLAYER_FOUL);
        } else if (botBall != null && isInHole(botBall)) {
            botBall = null;
            setGameOver(GameOverReason.BOT_FOUL);
        } else if (balls.isEmpty()) {
            setGameOver(GameOverReason.NO_BALLS_LEFT);
        }
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

    public void increasePlayerScore() {
        playerScore++;
    }

    public int getBotScore() {
        return botScore;
    }

    public void increaseBotScore() {
        botScore++;
    }

    public boolean isGameOver() {
        return gameOver != null;
    }

    public void addListener(BoardListener listener) {
        this.listeners.add(listener);
    }

    private void setGameOver(GameOverReason reason) {
        gameOver = new GameOver(reason, playerScore, botScore);
        notifyGameOver(gameOver);
    }

    private void notifyGameOver(GameOver gameOver) {
        for (BoardListener listener: listeners) {
            listener.onGameOver(gameOver);
        }
    }
}
