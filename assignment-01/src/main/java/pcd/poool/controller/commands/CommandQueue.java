package pcd.poool.controller.commands;

import pcd.poool.util.BoundedBuffer;
import pcd.poool.util.BoundedBufferImpl;

import java.util.ArrayList;
import java.util.List;

public class CommandQueue {

	private final BoundedBuffer<Command> buffer;
	
	public CommandQueue() {
		this.buffer = new BoundedBufferImpl<>(100);
	}
	
	public void notifyNewCommand(Command command) {
		try {
			buffer.put(command);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Command> fetchAllCommands() {
		List<Command> commands = new ArrayList<>();
		try {
			Command command;
			while ((command = buffer.poll()) != null) {
				commands.add(command);
			}
			return commands;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
