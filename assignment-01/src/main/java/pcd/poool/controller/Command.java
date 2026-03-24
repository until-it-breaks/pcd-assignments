package pcd.poool.controller;

import pcd.poool.model.Board;

public interface Command {
	
	void execute(Board board);
}
