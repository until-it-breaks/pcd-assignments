package pcd.poool.util;

/**
 * A synchronization barrier that blocks threads until a set number of signals are received.
 * <p>
 * Implementations provide a "gate" mechanism where threads can wait for a
 * countdown to reach zero before proceeding.
 * </p>
 */
public interface Latch {

    /**
     * Decrements the latch count. If the count reaches zero, all waiting threads are released.
     */
    void countDown();

    /**
     * Blocks the current thread until the latch count reaches zero.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    void await() throws InterruptedException;
}