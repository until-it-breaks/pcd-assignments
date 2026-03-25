package pcd.poool.view;

import pcd.poool.controller.commands.CommandProcessor;

public class View {

	private final ViewFrame frame;
	private final ViewModel viewModel;
	
	public View(ViewModel model, int w, int h, CommandProcessor controller) {
		frame = new ViewFrame(model, w, h, controller);
		frame.setVisible(true);
		this.viewModel = model;
	}
		
	public void render() {
		frame.render();
	}
	
	public ViewModel getViewModel() {
		return viewModel;
	}
}
