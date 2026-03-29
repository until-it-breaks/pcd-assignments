package pcd.poool.view;

import pcd.poool.controller.commands.CommandProcessor;
import pcd.poool.model.board.Board;

import javax.swing.*;

public class View {

	private final ViewFrame frame;
	private final ViewModel viewModel;
	
	public View(ViewModel model, int width, int height, CommandProcessor controller, Board board) {
		this.frame = new ViewFrame(model, width, height, controller);
		this.frame.setVisible(true);
		this.viewModel = model;
		board.addListener(gameOver -> {
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(frame, gameOver.getMessage());
				frame.dispose();
			});
		});
	}
		
	public void render() {
		frame.render();
	}
	
	public ViewModel getViewModel() {
		return viewModel;
	}
}
