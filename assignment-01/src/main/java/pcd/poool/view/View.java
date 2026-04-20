package pcd.poool.view;

import pcd.poool.controller.engine.GameEngineListener;
import pcd.poool.controller.engine.GameOverEvent;
import pcd.poool.controller.engine.EngineTimeoutEvent;
import pcd.poool.controller.commands.CommandQueue;

import javax.swing.*;

public class View implements GameEngineListener {
	private final ViewModel viewModel;
	private ViewFrame frame;
	
	public View(ViewModel model, int width, int height, CommandQueue controller) {
		this.viewModel = model;
		SwingUtilities.invokeLater(() -> {
			frame = new ViewFrame(model, width, height, controller);
			frame.setVisible(true);
		});
	}

	public void render() {
		if (frame != null) {
			frame.render();
		}
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
