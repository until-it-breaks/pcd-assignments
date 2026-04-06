package pcd.poool.controller.commands;

import pcd.poool.util.BoundedBuffer;
import pcd.poool.util.BoundedBufferImpl;

import java.util.ArrayList;
import java.util.List;

public class CommandQueue {
	public static final int COMMAND_LIMIT = 100;
	private final BoundedBuffer<Command> buffer = new BoundedBufferImpl<>(COMMAND_LIMIT);
	
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
