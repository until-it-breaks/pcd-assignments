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
 * This implementation achieves thread-safety without locks by providing each thread with
 * an isolated {@link CollisionAccumulator} row, thus eliminating contention
 * during the computation phase.
 * </p>
 * <p><b>Algorithm Phases:</b></p>
 * <ol>
 * <li>
 * <b>Map Phase:</b> Uses a mirrored strided distribution to balance the O(n²) workload.
 * Instead of applying changes directly to shared ball states, each thread
 * accumulates spatial and velocity deltas as well as the possible colliders into its own private row
 * within the accumulator table.
 * </li>
 * <li>
 * <b>Reduce Phase:</b> Merges the results in a strided column-wise fashion.
 * Each reduction thread is responsible for a specific subset of balls, aggregating
 * the deltas from all corresponding rows in the accumulator table to compute the
 * final state of each ball.
 * </li>
 * </ol>
 * <p>
 * This approach trades memory O(threadCount * ballCount) for CPU throughput.
 * </p>
 */
public class ThreadedLockFreeCollisionResolver implements CollisionResolver {
    private final int threadCount;

    public ThreadedLockFreeCollisionResolver() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public ThreadedLockFreeCollisionResolver(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();
        CollisionAccumulator[][] accumulatorTable = new CollisionAccumulator[threadCount][n];
        for (int i = 0; i < threadCount; i++) {
            for (int j = 0; j < n; j++) {
                accumulatorTable[i][j] = new CollisionAccumulator();
            }
        }

        // Map Phase
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads.add(new Thread(() -> {
                CollisionAccumulator[] accumulatorRow = accumulatorTable[threadIndex];
                for (int j = threadIndex; j < n / 2; j += threadCount) {
                    processRow(j, balls, accumulatorRow);
                    processRow(n - 1 - j, balls, accumulatorRow);
                }
                if (n % 2 != 0 && threadIndex == 0) {
                    processRow(n / 2, balls, accumulatorRow);
                }
            }));
            threads.get(i).start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        // Reduction Phase
        List<Thread> reductionThreads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int ballIndex = i;
            reductionThreads.add(new Thread(() -> {
                for (int j = ballIndex; j < n; j += threadCount) {
                    double dx = 0, dy = 0, dvx = 0, dvy = 0;
                    Set<Ball> colliders = new HashSet<>();
                    for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
                        CollisionAccumulator acc = accumulatorTable[threadIndex][j];
                        dx += acc.getDeltaX();
                        dy += acc.getDeltaY();
                        dvx += acc.getDeltaVX();
                        dvy += acc.getDeltaVY();
                        colliders.addAll(acc.getColliders());
                    }
                    Ball ball = balls.get(j);
                    ball.setPos(new P2d(ball.getPos().x() + dx, ball.getPos().y() + dy));
                    ball.setVel(new V2d(ball.getVel().x() + dvx, ball.getVel().y() + dvy));
                    if (!colliders.isEmpty()) {
                        ball.setLatestColliders(colliders);
                    }
                }
            }));
            reductionThreads.get(i).start();
        }
        for (Thread thread : reductionThreads) {
            thread.join();
        }
    }

    private void processRow(int i, List<Ball> balls, CollisionAccumulator[] accumulators) {
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.resolveCollisionWithAccumulators(i, j, balls, accumulators);
        }
    }
}