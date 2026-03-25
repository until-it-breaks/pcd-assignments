package pcd.poool.controller.commands;

import pcd.poool.util.BoundedBuffer;
import pcd.poool.util.BoundedBufferImpl;
import pcd.poool.model.Board;

public class CommandProcessor extends Thread {

	private final BoundedBuffer<Command> commandBuffer;
	private final Board board;
	
	public CommandProcessor(Board board) {
		this.commandBuffer = new BoundedBufferImpl<>(100);
		this.board = board;
	}
	
	public void run() {
		log("Started");
		while (true) {
			try {
				log("Waiting for commands");
				var command = commandBuffer.get();
				log("New command fetched: " + command.getClass().getSimpleName());
				command.execute(board);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void notifyNewCommand(Command command) {
		try {
			commandBuffer.put(command);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + "][ " + this.getClass().getSimpleName() + " ] " + msg);
	}
}
