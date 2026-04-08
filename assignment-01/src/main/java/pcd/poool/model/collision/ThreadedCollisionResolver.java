package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;

import java.util.ArrayList;
import java.util.List;

/**
 * A multithreaded {@link CollisionResolver} that manages raw {@link Thread} lifecycles manually.
 * <p>
 * This resolver uses a "Heavy/Light" row distribution strategy to balance the
 * O(n²) workload across a fixed number of threads. It relies on
 * {@code Balls.resolveCollisionSynchronized} to ensure thread safety during
 * concurrent access to ball states.
 * </p>
 * <p>
 * <b>Note:</b> Because this implementation creates and joins new threads on every
 * call to {@code resolve}, it may incur significant performance penalties in
 * high-frequency simulation loops.
 * </p>
 */
public class ThreadedCollisionResolver implements CollisionResolver {
    private final int threadCount;

    public ThreadedCollisionResolver() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public ThreadedCollisionResolver(int threadCount) {
        this.threadCount = threadCount;
    }

    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();
        List<Thread> threads = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads.add(new Thread(() -> {
                for (int j = threadIndex; j < n / 2; j += threadCount) {
                    processRow(j, balls);           // Heavy row
                    processRow(n - 1 - j, balls);   // Light row
                }
                // Handle middle row for odd counts
                if (n % 2 != 0 && threadIndex == 0) {
                    processRow(n / 2, balls);
                }
            }));
            threads.get(i).start();
        }
        for (Thread t : threads) t.join();
    }

    protected void processRow(int i, List<Ball> balls) {
        Ball ball = balls.get(i);
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.resolveCollisionSynchronized(ball, balls.get(j));
        }
    }
}
