package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;

import java.util.*;
import java.util.concurrent.*;

/**
 * A thread-pooled and task-based variant of {@link ThreadedLockFreeCollisionResolver}.
 *
 * <p>
 * This implementation preserves the same MapReduce-style parallel collision pipeline,
 * but replaces manual thread management with an {@link ExecutorService}-based task system.
 * </p>
 *
 * <p>
 * Thread-safe parallelism is achieved by ensuring that each task operates on an isolated
 * {@link CollisionAccumulator} map during the computation phase. No shared mutation occurs
 * during the map phase.
 * </p>
 *
 * <ol>
 * <li>
 * Map Phase:
 * The O(n²) collision space is partitioned using the same mirrored strided distribution
 * strategy as the threaded implementation. Each task processes a subset of collision pairs
 * and accumulates position and velocity deltas, along with collider information, into its
 * own private accumulator map.
 * </li>
 *
 * <li>
 * Reduction Phase:
 * After all tasks complete, their accumulator maps are merged sequentially.
 * Each ball is updated exactly once by aggregating contributions from all task-local maps.
 * </li>
 * </ol>
 *
 * <p>
 * Compared to the threaded version, this implementation improves scheduling flexibility
 * and simplifies execution management, while preserving the same memory and computational
 * characteristics.
 * </p>
 *
 * <p>
 * This design trades additional memory usage (O(taskCount × numberOfBalls)) for
 * improved CPU throughput and eliminates synchronization overhead in the map phase.
 * </p>
 */
public class PooledLockFreeCollisionResolver implements CollisionResolver, AutoCloseable {
    private final ExecutorService executor;
    private final int taskCount;

    public PooledLockFreeCollisionResolver() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public PooledLockFreeCollisionResolver(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.taskCount = threadCount;
    }

    @Override
    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();

        // Map Phase
        List<Future<Map<Ball, CollisionAccumulator>>> futures = new ArrayList<>();
        for (int t = 0; t < taskCount; t++) {
            final int taskId = t;
            futures.add(executor.submit(() -> {
                Map<Ball, CollisionAccumulator> accumulatorMap = new HashMap<>();
                for (int i = taskId; i < n / 2; i += taskCount) {
                    processRow(i, balls, accumulatorMap);
                    processRow(n - 1 - i, balls, accumulatorMap);
                }
                if (n % 2 != 0 && taskId == 0) {
                    processRow(n / 2, balls, accumulatorMap);
                }
                return accumulatorMap;
            }));
        }

        // Result collection
        List<Map<Ball, CollisionAccumulator>> results = new ArrayList<>(taskCount);
        for (Future<Map<Ball, CollisionAccumulator>> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        // Reduction Phase
        for (Ball ball : balls) {
            double dx = 0, dy = 0, dvx = 0, dvy = 0;
            Set<Ball> colliders = new HashSet<>();
            for (Map<Ball, CollisionAccumulator> result : results) {
                CollisionAccumulator accumulator = result.get(ball);
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

    @Override
    public void close() {
        executor.shutdown();
    }
}