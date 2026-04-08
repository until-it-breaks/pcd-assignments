package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;
import pcd.poool.util.Latch;
import pcd.poool.util.LatchImplementation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A high-performance {@link CollisionResolver} utilizing a fixed thread pool.
 * <p>
 * This implementation improves upon raw threading by reusing a persistent
 * {@link ExecutorService}, significantly reducing the overhead of thread creation.
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
                    processRow(row, balls);                     // Heavy
                    processRow(ballCount - 1 - row, balls);     // Light
                } finally {
                    latch.countDown();
                }
            });
        }
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
