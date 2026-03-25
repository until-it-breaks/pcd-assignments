package pcd.poool.view;

import pcd.poool.model.Ball;
import pcd.poool.model.Board;
import pcd.poool.model.Hole;

import java.util.ArrayList;
import java.util.List;

public class ViewModel {

	private final List<BallViewInfo> balls;
	private BallViewInfo player;
	private BallViewInfo bot;
	private int framePerSec;
	private int playerScore;
	private int botScore;
	private List<Hole> holes;
	
	public ViewModel() {
		balls = new ArrayList<>();
		holes = new ArrayList<>();
		framePerSec = 0;
		playerScore = 0;
		botScore = 0;
	}
	
	public synchronized void update(Board board, int framePerSec) {
		balls.clear();
		for (Ball b: board.getBalls()) {
			balls.add(new BallViewInfo(b.getPos(), b.getRadius()));
		}
		this.framePerSec = framePerSec;
		Ball player = board.getPlayerBall();
		this.player = new BallViewInfo(player.getPos(), player.getRadius());
		Ball bot = board.getBotBall();
		this.bot = new BallViewInfo(bot.getPos(), bot.getRadius());
		this.playerScore = board.getPlayerScore();
		this.botScore = board.getBotScore();
		this.holes = board.getHoles();
	}
	
	public synchronized List<BallViewInfo> getBalls(){
		return new ArrayList<>(balls);
	}

	public synchronized List<Hole> getHoles() {
		return new ArrayList<>(holes);
	}

	public synchronized int getFramePerSec() {
		return framePerSec;
	}

	public synchronized BallViewInfo getPlayerBall() {
		return player;
	}

	public synchronized BallViewInfo getBotBall() {
		return bot;
	}

	public synchronized int getPlayerScore() {
		return playerScore;
	}

	public synchronized int getBotScore() {
		return botScore;
	}
}
