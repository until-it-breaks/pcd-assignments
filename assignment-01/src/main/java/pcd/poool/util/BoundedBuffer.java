package pcd.poool.util;

/**
 * A generic, thread-safe collection with a fixed capacity.
 * <p>
 * This interface defines a monitor-style buffer that blocks on retrieval
 * when empty and blocks on insertion when full.
 * </p>
 *
 * @param <Item> the type of elements held in this buffer
 */
public interface BoundedBuffer<Item> {
    /**
     * Inserts the specified element into this buffer, waiting if necessary
     * for space to become available.
     * @param item the element to add
     * @throws InterruptedException if interrupted while waiting
     */
    void put(Item item) throws InterruptedException;

    /**
     * Retrieves and removes the head of this buffer, waiting if necessary
     * until an element becomes available.
     * @return the head of this buffer
     * @throws InterruptedException if interrupted while waiting
     */
    Item get() throws InterruptedException;

    /**
     * Retrieves and removes the head of this buffer, or returns {@code null}
     * if this buffer is empty, without waiting.
     * @return the head of this buffer, or {@code null} if empty
     */
    Item poll();
}
