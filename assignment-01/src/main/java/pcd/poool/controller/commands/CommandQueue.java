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

	/**
	 * Drains all currently available commands from the queue.
	 * <p>
	 * This method returns immediately and does not wait for new commands to arrive.
	 * It performs a thread-safe "drain" of the buffer, retrieving all commands
	 * currently residing in it at the moment of the call.
	 * </p>
	 * <p>
	 * While this method does not sleep, it is synchronized via the
	 * underlying buffer. If another thread is currently adding a command, this
	 * thread will briefly block until the lock is released.
	 * </p>
	 * @return A list of all pending {@link Command} objects.
	 */
	public List<Command> fetchAllCommands() {
		List<Command> commands = new ArrayList<>();
        Command command;
        while ((command = buffer.poll()) != null) {
            commands.add(command);
        }
        return commands;
    }
}
