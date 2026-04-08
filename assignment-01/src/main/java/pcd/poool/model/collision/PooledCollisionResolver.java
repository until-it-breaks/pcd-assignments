package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;
import pcd.poool.util.Latch;
import pcd.poool.util.LatchImplementation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A multithreaded {@link CollisionResolver} utilizing a fixed thread pool.
 * <p>
 * This class is an {@link ExecutorService}-based variant of the {@link ThreadedCollisionResolver}.
 * While the previous version used a stride to interleave row assignments, this implementation
 * <b>batches</b> each row with its mirror (row {@code i} and {@code n - 1 - i})
 * into a single task.
 * </p>
 * <p>
 * This strategy folds the O(n²) triangular workload into N/2 balanced tasks,
 * which are then distributed across a persistent thread pool to eliminate the overhead
 * of thread creation and destruction.
 * </p>
 */
public class PooledCollisionResolver implements CollisionResolver, AutoCloseable {
    private final ExecutorService executor;

    public PooledCollisionResolver() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public PooledCollisionResolver(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    public void resolve(List<Ball> balls) throws InterruptedException {
        int ballCount = balls.size();
        int taskCount = ballCount / 2;
        Latch latch = new LatchImplementation(taskCount + (ballCount % 2));
        for (int i = 0; i < ballCount / 2; i++) {
            final int row = i;
            executor.execute(() -> {
                try {
                    processRow(row, balls);                     // Frontmost available row
                    processRow(ballCount - 1 - row, balls);     // Backmost available row
                } finally {
                    latch.countDown();
                }
            });
        }
        // Handle middle row for odd counts. This task is treated as a row without its mirror.
        if (ballCount % 2 != 0) {
            executor.execute(() -> {
                try {
                    processRow(ballCount / 2, balls);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }

    private void processRow(int i, List<Ball> balls) {
        Ball ball = balls.get(i);
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.resolveCollisionSynchronized(ball, balls.get(j));
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
