package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;

import java.util.*;

/**
 * A thread based {@link CollisionResolver} using a MapReduce-style parallel collision pipeline.
 *
 * <p>
 * This implementation achieves thread-safe parallelism without locks by ensuring that each
 * worker thread operates on an isolated {@link CollisionAccumulator} map during the
 * computation phase. No shared mutation occurs during the map phase.
 * </p>
 *
 * <ol>
 * <li>
 * Map Phase:
 * The O(n²) collision space is partitioned using a mirrored strided distribution.
 * Each thread processes a subset of collision pairs and accumulates position and velocity
 * deltas, along with collider information, into its own private accumulator map.
 * </li>
 *
 * <li>
 * Reduce Phase:
 * After all map tasks complete, results are merged sequentially.
 * For each ball, accumulated deltas from all thread-local maps are summed to compute
 * the final updated position, velocity, and collider set.
 * </li>
 * </ol>
 *
 * <p>
 * This design trades additional memory usage (O(threadCount × numberOfBalls)) for
 * improved CPU throughput and eliminates synchronization overhead in the map phase.
 * </p>
 *
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

        List<Map<Ball, CollisionAccumulator>> accumulatorMaps = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            accumulatorMaps.add(new HashMap<>());
        }

        // Map Phase
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads.add(new Thread(() -> {
                var accumulatorMap = accumulatorMaps.get(threadIndex);
                for (int j = threadIndex; j < n / 2; j += threadCount) {
                    processRow(j, balls, accumulatorMap);
                    processRow(n - 1 - j, balls, accumulatorMap);
                }
                if (n % 2 != 0 && threadIndex == 0) {
                    processRow(n / 2, balls, accumulatorMap);
                }
            }));
            threads.get(i).start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        // Reduction Phase
        for (Ball ball : balls) {
            double dx = 0, dy = 0, dvx = 0, dvy = 0;
            Set<Ball> colliders = new HashSet<>();
            for (Map<Ball, CollisionAccumulator> accumulatorMap : accumulatorMaps) {
                CollisionAccumulator accumulator = accumulatorMap.get(ball);
                if (accumulator != null) {
                    dx += accumulator.getDeltaX();
                    dy += accumulator.getDeltaY();
                    dvx += accumulator.getDeltaVX();
                    dvy += accumulator.getDeltaVY();
                    colliders.addAll(accumulator.getColliders());
                }
            }
            ball.setPos(new P2d(ball.getPos().x() + dx, ball.getPos().y() + dy));
            ball.setVel(new V2d(ball.getVel().x() + dvx, ball.getVel().y() + dvy));
            if (!colliders.isEmpty()) {
                ball.setLatestColliders(colliders);
            }
        }
    }

    private void processRow(int i, List<Ball> balls, Map<Ball, CollisionAccumulator> accumulatorMap) {
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.resolveCollisionWithAccumulators(i, j, balls, accumulatorMap);
        }
    }
}