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

public class ExecutorAccumulatorBasedCollisionResolver implements CollisionResolver, AutoCloseable {

    private static final int ACCUMULATOR_COUNT = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(ACCUMULATOR_COUNT);

    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();
        BoundedBuffer<CollisionAccumulator[]> accumulatorsPool = new BoundedBufferImpl<>(ACCUMULATOR_COUNT);
        List<CollisionAccumulator[]> accumulatorMatrix = new ArrayList<>();
        for (int i = 0; i < ACCUMULATOR_COUNT; i++) {
            CollisionAccumulator[] row = new CollisionAccumulator[n];
            for (int j = 0; j < n; j++) row[j] = new CollisionAccumulator();
            accumulatorsPool.put(row);
            accumulatorMatrix.add(row);
        }
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < n / 2; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                CollisionAccumulator[] accumulatorRow = null;
                try {
                    accumulatorRow = accumulatorsPool.get();
                    processRow(rowIndex, balls, accumulatorRow);
                    processRow(n - 1 - rowIndex, balls, accumulatorRow);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (accumulatorRow != null) {
                        accumulatorsPool.put(accumulatorRow);
                    }
                }
                return null;
            });
        }
        if (n % 2 != 0) {
            tasks.add(() -> {
                CollisionAccumulator[] accumulatorRow = null;
                try {
                    accumulatorRow = accumulatorsPool.get();
                    processRow(n / 2, balls, accumulatorRow);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (accumulatorRow != null) {
                        accumulatorsPool.put(accumulatorRow);
                    }
                }
                return null;
            });
        }
        executor.invokeAll(tasks);
        List<Callable<Void>> reductionTasks = new ArrayList<>();
        for (int i = 0; i < ACCUMULATOR_COUNT; i++) {
            final int accumulatorIndex = i;
            reductionTasks.add(() -> {
                for (int ballIndex = accumulatorIndex; ballIndex < n; ballIndex += ACCUMULATOR_COUNT) {
                    reduceBall(ballIndex, balls, accumulatorMatrix);
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

    private void reduceBall(int ballIndex, List<Ball> balls, List<CollisionAccumulator[]> accumulatorMatrix) {
        double dx = 0, dy = 0, dvx = 0, dvy = 0;
        Set<Ball> colliders = new HashSet<>();
        for (CollisionAccumulator[] row : accumulatorMatrix) {
            CollisionAccumulator accumulator = row[ballIndex];
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