package pcd.poool.controller.commands;

import pcd.poool.model.board.Board;

public interface Command {
	void execute(Board board);
}
