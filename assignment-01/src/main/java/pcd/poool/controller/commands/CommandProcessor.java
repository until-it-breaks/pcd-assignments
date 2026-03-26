package pcd.poool.controller.commands;

import pcd.poool.util.BoundedBuffer;
import pcd.poool.util.BoundedBufferImpl;
import pcd.poool.model.Board;

public class CommandProcessor extends Thread {

	private final BoundedBuffer<Command> commandBuffer;
	private final Board board;
	private volatile boolean running;
	
	public CommandProcessor(Board board) {
		super("CommandProcessorThread");
		this.commandBuffer = new BoundedBufferImpl<>(100);
		this.board = board;
		this.running = true;
	}
	
	public void run() {
		log("Started");
		while (running) {
			try {
				log("Waiting for commands");
				var command = commandBuffer.get();
				log("New command fetched: " + command.getClass().getSimpleName());
				command.execute(board);
			} catch (InterruptedException e) {
				// Thread was interrupted: exit cleanly if running is false
				if (!running) {
					log("Processor stopped.");
					break;
				}
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

	public void stopProcessor() {
		this.running = false;
		this.interrupt();
	}
	
	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + "][ " + this.getClass().getSimpleName() + " ] " + msg);
	}
}
