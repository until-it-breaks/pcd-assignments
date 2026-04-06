package pcd.poool.view;

import pcd.poool.controller.engine.GameEngineListener;
import pcd.poool.controller.engine.GameOverEvent;
import pcd.poool.controller.engine.EngineTimeoutEvent;
import pcd.poool.controller.commands.CommandQueue;

import javax.swing.*;

public class View implements GameEngineListener {

	private final ViewFrame frame;
	private final ViewModel viewModel;
	
	public View(ViewModel model, int width, int height, CommandQueue controller) {
		this.frame = new ViewFrame(model, width, height, controller);
		this.frame.setVisible(true);
		this.viewModel = model;
	}
		
	public void render() {
		frame.render();
	}
	
	public ViewModel getViewModel() {
		return viewModel;
	}

	@Override
	public void onEngineTimeout(EngineTimeoutEvent event) {
		SwingUtilities.invokeLater(frame::dispose);
	}

	@Override
	public void onGameOver(GameOverEvent event) {
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(frame, event.gameOverDetails().getMessage());
			frame.dispose();
		});
	}
}
