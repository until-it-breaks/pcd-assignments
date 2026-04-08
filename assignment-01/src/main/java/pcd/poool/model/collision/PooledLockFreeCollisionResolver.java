package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;
import pcd.poool.util.BoundedBuffer;
import pcd.poool.util.BoundedBufferImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * A thread-pooled variant of {@link ThreadedLockFreeCollisionResolver}.
 * <p>
 * This implementation improves the MapReduce pattern by utilizing an {@link ExecutorService}
 * for task management.
 * </p>
 * Transitioning to a task-based model presented a significant
 * memory challenge. In the raw threaded version, each thread owned exactly one row-buffer.
 * Since the number of tasks in the pooled version ({@code n/2}) is much
 * larger than the thread count, allocating a unique buffer per task would lead to
 * unsustainable memory pressure.
 * </p>
 * <p>In this implementation:
 * <ul>
 * <li>The resolver maintains a fixed-size pool of {@code accumulatorCount} rows.</li>
 * <li>Map Phase:< Tasks utilize a "lease-and-release" mechanism via a
 * {@link BoundedBuffer}. Each task borrows a row, populates it using the
 * mirrored row distribution strategy, and returns it to the pool
 * for reuse by other tasks.</li>
 * <li>Reduce Phase: Aggregates results using strided column-wise merging,
 * ensuring each ball is processed by exactly one task.</li>
 * </ul>
 * <p/>
 */
public class PooledLockFreeCollisionResolver implements CollisionResolver, AutoCloseable {
    private final ExecutorService executor;
    private final int accumulatorCount;

    public PooledLockFreeCollisionResolver() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public PooledLockFreeCollisionResolver(int threadCount) {
        this(threadCount, threadCount);
    }

    public PooledLockFreeCollisionResolver(int threadCount, int accumulatorCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.accumulatorCount = accumulatorCount;
    }

    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();
        BoundedBuffer<CollisionAccumulator[]> accumulatorPool = new BoundedBufferImpl<>(accumulatorCount);
        List<CollisionAccumulator[]> accumulatorTable = new ArrayList<>();
        for (int i = 0; i < accumulatorCount; i++) {
            CollisionAccumulator[] accumulatorRow = new CollisionAccumulator[n];
            for (int j = 0; j < n; j++) {
                accumulatorRow[j] = new CollisionAccumulator();
            }
            accumulatorPool.put(accumulatorRow);
            accumulatorTable.add(accumulatorRow);
        }

        // Map Phase
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < n / 2; i++) {
            final int ballIndex = i;
            tasks.add(() -> {
                CollisionAccumulator[] accumulatorRow = null;
                try {
                    accumulatorRow = accumulatorPool.get();
                    processRow(ballIndex, balls, accumulatorRow);
                    processRow(n - 1 - ballIndex, balls, accumulatorRow);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (accumulatorRow != null) {
                        accumulatorPool.put(accumulatorRow);
                    }
                }
                return null;
            });
        }
        if (n % 2 != 0) {
            tasks.add(() -> {
                CollisionAccumulator[] accumulatorRow = null;
                try {
                    accumulatorRow = accumulatorPool.get();
                    processRow(n / 2, balls, accumulatorRow);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (accumulatorRow != null) {
                        accumulatorPool.put(accumulatorRow);
                    }
                }
                return null;
            });
        }
        executor.invokeAll(tasks);

        // Reduction Phase
        List<Callable<Void>> reductionTasks = new ArrayList<>();
        for (int i = 0; i < accumulatorCount; i++) {
            final int accumulatorIndex = i;
            reductionTasks.add(() -> {
                for (int ballIndex = accumulatorIndex; ballIndex < n; ballIndex += accumulatorCount) {
                    reduceBall(ballIndex, balls, accumulatorTable);
                }
                return null;
            });
        }
        executor.invokeAll(reductionTasks);
    }

    private void processRow(int i, List<Ball> balls, CollisionAccumulator[] accumulators) {
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.resolveCollisionWithAccumulators(i, j, balls, accumulators);
        }
    }

    private void reduceBall(int ballIndex, List<Ball> balls, List<CollisionAccumulator[]> accumulatorTable) {
        double dx = 0, dy = 0, dvx = 0, dvy = 0;
        Set<Ball> colliders = new HashSet<>();
        for (CollisionAccumulator[] accumulatorRow : accumulatorTable) {
            CollisionAccumulator accumulator = accumulatorRow[ballIndex];
            dx += accumulator.getDeltaX();
            dy += accumulator.getDeltaY();
            dvx += accumulator.getDeltaVX();
            dvy += accumulator.getDeltaVY();
            if (!accumulator.getColliders().isEmpty()) {
                colliders.addAll(accumulator.getColliders());
            }
        }
        Ball ball = balls.get(ballIndex);
        ball.setPos(new P2d(ball.getPos().x() + dx, ball.getPos().y() + dy));
        ball.setVel(new V2d(ball.getVel().x() + dvx, ball.getVel().y() + dvy));
        if (!colliders.isEmpty()) {
            ball.setLatestColliders(colliders);
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}