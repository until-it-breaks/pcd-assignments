package pcd.poool.controller;

import pcd.poool.util.BoundedBuffer;
import pcd.poool.util.BoundedBufferImpl;
import pcd.poool.model.Board;

public class ActiveController extends Thread {

	private final BoundedBuffer<Command> cmdBuffer;
	private final Board board;
	
	public ActiveController(Board board) {
		this.cmdBuffer = new BoundedBufferImpl<>(100);
		this.board = board;
	}
	
	public void run() {
		log("started.");
		while (true) {
			try {
				log("Waiting for cmds ");
				var cmd = cmdBuffer.get();
				log("new cmd fetched: " + cmd);
				cmd.execute(board);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void notifyNewCmd(Command command) {
		try {
			cmdBuffer.put(command);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + "][ Controller ] " + msg);
	}
}
