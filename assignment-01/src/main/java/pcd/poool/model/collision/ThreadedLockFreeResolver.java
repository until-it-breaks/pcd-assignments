package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A lock-free {@link CollisionResolver} using a MapReduce concurrency pattern.
 * <p>
 * This implementation eliminates lock contention by providing each thread with its
 * own {@link CollisionAccumulator} matrix. The process is split into two phases:
 * <ol>
 * <li><b>Map Phase:</b> Threads compute collision effects into isolated accumulators.</li>
 * <li><b>Reduce Phase:</b> Results from all accumulators are merged and applied to the balls.</li>
 * </ol>
 * </p>
 * <p>
 * Requires {@code O(threadCount * ballCount)} space.
 * </p>
 */
public class ThreadedLockFreeResolver implements CollisionResolver {
    private final int threadCount;

    public ThreadedLockFreeResolver() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public ThreadedLockFreeResolver(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();

        CollisionAccumulator[][] accumulatorMatrix = new CollisionAccumulator[threadCount][n];
        for (int i = 0; i < threadCount; i++) {
            for (int j = 0; j < n; j++) {
                accumulatorMatrix[i][j] = new CollisionAccumulator();
            }
        }

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads.add(new Thread(() -> {
                CollisionAccumulator[] accumulatorRow = accumulatorMatrix[index];
                for (int j = index; j < n / 2; j += threadCount) {
                    processRow(j, balls, accumulatorRow);
                    processRow(n - 1 - j, balls, accumulatorRow);
                }
                if (n % 2 != 0 && index == 0) {
                    processRow(n / 2, balls, accumulatorRow);
                }
            }));
            threads.get(i).start();
        }
        for (Thread thread : threads) thread.join();

        List<Thread> reductionThreads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            reductionThreads.add(new Thread(() -> {
                for (int ballIndex = index; ballIndex < n; ballIndex += threadCount) {
                    double dx = 0, dy = 0, dvx = 0, dvy = 0;
                    Set<Ball> colliders = new HashSet<>();
                    for (int accumulatorIndex = 0; accumulatorIndex < threadCount; accumulatorIndex++) {
                        CollisionAccumulator acc = accumulatorMatrix[accumulatorIndex][ballIndex];
                        dx += acc.getDeltaX();
                        dy += acc.getDeltaY();
                        dvx += acc.getDeltaVX();
                        dvy += acc.getDeltaVY();
                        colliders.addAll(acc.getColliders());
                    }
                    Ball ball = balls.get(ballIndex);
                    ball.setPos(new P2d(ball.getPos().x() + dx, ball.getPos().y() + dy));
                    ball.setVel(new V2d(ball.getVel().x() + dvx, ball.getVel().y() + dvy));
                    if (!colliders.isEmpty()) {
                        ball.setLatestColliders(colliders);
                    }
                }
            }));
            reductionThreads.get(i).start();
        }
        for (Thread thread : reductionThreads) thread.join();
    }

    private void processRow(int i, List<Ball> balls, CollisionAccumulator[] accumulators) {
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.resolveCollisionWithAccumulators(i, j, balls, accumulators);
        }
    }
}