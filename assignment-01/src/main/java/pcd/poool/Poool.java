package pcd.poool;

import pcd.poool.controller.ai.Bot;
import pcd.poool.controller.engine.EngineTimeoutEvent;
import pcd.poool.controller.engine.GameEngine;
import pcd.poool.controller.commands.CommandQueue;
import pcd.poool.controller.engine.GameEngineListener;
import pcd.poool.controller.engine.GameOverEvent;
import pcd.poool.model.board.Board;
import pcd.poool.model.board.MassiveBoardConf;
import pcd.poool.model.collision.*;
import pcd.poool.view.View;
import pcd.poool.view.ViewModel;

public class Poool {

	public static final int UNLIMITED_SIMULATION_TIME = 0;

	public static void main(String[] args) {
		/*
		 * Different board configs to try:
		 * - minimal: 2 small balls
		 * - large: 400 small balls
		 * - massive: 4500 small balls
		 */
		Board board = new Board();
		CollisionResolver resolver = new ExecutorAccumulatorBasedCollisionResolver();
		board.init(new MassiveBoardConf(), resolver);
		CommandQueue commandQueue = new CommandQueue();
		View view = new View(new ViewModel(), 1200, 800, commandQueue);

		Bot bot = new Bot(board, commandQueue);
		Thread botThread = new Thread(bot);
		botThread.setName("BotThread");

		GameEngine gameEngine = new GameEngine(commandQueue, board, view, UNLIMITED_SIMULATION_TIME);
		gameEngine.addListener(bot);
		gameEngine.addListener(view);
		gameEngine.addListener(new GameEngineListener() {
			@Override
			public void onEngineTimeout(EngineTimeoutEvent event) {
				shutdown();
			}

			@Override
			public void onGameOver(GameOverEvent event) {
				shutdown();
			}

			private void shutdown() {
				if (resolver instanceof AutoCloseable closeable) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
			}
		});

		Thread gameEngineThread = new Thread(gameEngine);
		gameEngineThread.setName("GameEngineThread");

		gameEngineThread.start();
		botThread.start();
	}
}
