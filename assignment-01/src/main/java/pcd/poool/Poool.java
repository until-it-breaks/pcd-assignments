package pcd.poool;

import pcd.poool.controller.Bot;
import pcd.poool.controller.GameEngine;
import pcd.poool.controller.commands.CommandQueue;
import pcd.poool.model.board.Board;
import pcd.poool.model.board.MassiveBoardConf;
import pcd.poool.model.collision.ExecutorCollisionResolver;
import pcd.poool.view.View;
import pcd.poool.view.ViewModel;

public class Poool {
	public static void main(String[] args) {
		/*
		 * Different board configs to try:
		 * - minimal: 2 small balls
		 * - large: 400 small balls
		 * - massive: 4500 small balls
		 */
		Board board = new Board();
		board.init(new MassiveBoardConf(), new ExecutorCollisionResolver());
		CommandQueue commandQueue = new CommandQueue();
		View view = new View(new ViewModel(), 1200, 800, commandQueue, board);

		Bot bot = new Bot(board, commandQueue);
		Thread botThread = new Thread(bot);
		botThread.setName("BotThread");

		GameEngine gameEngine = new GameEngine(commandQueue, board, view);
		Thread gameEngineThread = new Thread(gameEngine);
		gameEngineThread.setName("GameEngineThread");

		gameEngineThread.start();
		botThread.start();

		board.addListener(gameOver -> {
			gameEngine.stopEngine();
			bot.stopBot();
		});
	}
}
