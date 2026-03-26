package pcd.poool;

import pcd.poool.controller.Bot;
import pcd.poool.controller.GameEngine;
import pcd.poool.controller.commands.CommandProcessor;
import pcd.poool.model.Board;
import pcd.poool.model.MassiveBoardConf;
import pcd.poool.view.View;
import pcd.poool.view.ViewModel;

import javax.swing.*;

public class Poool {
	public static void main(String[] args) {
		/*
		 * Different board configs to try:
		 * - minimal: 2 small balls
		 * - large: 400 small balls
		 * - massive: 4500 small balls
		 */
		Board board = new Board();
		board.init(new MassiveBoardConf());
		ViewModel viewModel = new ViewModel();
		CommandProcessor commandProcessor = new CommandProcessor(board);
		View view = new View(viewModel, 1200, 800, commandProcessor, board);
		Bot bot = new Bot(board, commandProcessor);
		GameEngine engine = new GameEngine(board, viewModel, view);

		engine.start();
		commandProcessor.start();
		bot.start();

		board.addListener(gameOver -> {
			engine.stopEngine();
			commandProcessor.stopProcessor();
			bot.stopBot();
		});
	}
}
