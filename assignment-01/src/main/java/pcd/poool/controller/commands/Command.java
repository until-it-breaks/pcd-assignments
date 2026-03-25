package pcd.poool.controller.commands;

import pcd.poool.model.Board;

public interface Command {
	
	void execute(Board board);
}
