package pcd.poool;

import pcd.poool.controller.BotMoveCommand;
import pcd.poool.model.MassiveBoardConf;
import pcd.poool.model.MinimalBoardConf;
import pcd.poool.model.V2d;
import pcd.poool.controller.ActiveController;
import pcd.poool.model.Board;
import pcd.poool.view.View;
import pcd.poool.view.ViewModel;

import java.util.Random;

public class Sketch03 {
	public static void main(String[] argv) {

		/* 
		 * Different board configs to try:
		 * - minimal: 2 small balls
		 * - large: 400 small balls
		 * - massive: 4500 small balls 
		 */
		
		// var boardConf = new MinimalBoardConf();
		// var boardConf = new LargeBoardConf();
		var boardConf = new MassiveBoardConf();
		
		Board board = new Board();
		board.init(boardConf);

		ActiveController activeController = new ActiveController(board);
		activeController.start();
		
		ViewModel viewModel = new ViewModel();
		View view = new View(viewModel, 1200, 800, activeController);
						
		viewModel.update(board, 0);			
		view.render();
		waitAbit();

		int nFrames = 0;
		long t0 = System.currentTimeMillis();
		long lastUpdateTime = System.currentTimeMillis();


		var botBall = board.getBotBall();
		var rand = new Random(2);
		var lastKickTime = t0;

		/* main simulation loop */
		
		while (true) {
			if (botBall.getVel().abs() < 0.05 && System.currentTimeMillis() - lastKickTime > 2000) {
				var angle = rand.nextDouble()*Math.PI*0.25;
				var v = new V2d(Math.cos(angle),Math.sin(angle)).mul(1.5);
				activeController.notifyNewCmd(new BotMoveCommand(v));
				lastKickTime = System.currentTimeMillis();
			}

			/* update board state */
			
			long elapsed = System.currentTimeMillis() - lastUpdateTime;
			lastUpdateTime = System.currentTimeMillis();			
			board.updateState(elapsed);
			
			/* render */
			
			nFrames++;
			int framePerSec = 0;
			long dt = (System.currentTimeMillis() - t0);
			if (dt > 0) {
				framePerSec = (int)(nFrames * 1000 / dt);
			}

			viewModel.update(board, framePerSec);			
			view.render();
			
		}
	}
	
	private static void waitAbit() {
		try {
			Thread.sleep(2000);
		} catch (Exception ex) {}
	}
	
}
