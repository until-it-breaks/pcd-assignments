package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorBasedCollisionResolver implements CollisionResolver, AutoCloseable {

    private final int threadCount = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    @Override
    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    for (int j = threadIndex; j < n / 2; j += threadCount) {
                        processRow(j, balls);           // Heavy row
                        processRow(n - 1 - j, balls);   // Light row
                    }
                    // Handle middle row for odd counts
                    if (n % 2 != 0 && threadIndex == 0) {
                        processRow(n / 2, balls);
                    }
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
