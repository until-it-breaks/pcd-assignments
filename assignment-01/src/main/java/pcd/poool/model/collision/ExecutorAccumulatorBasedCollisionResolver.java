package pcd.poool.model.collision;

import pcd.poool.controller.engine.EngineTimeoutEvent;
import pcd.poool.controller.engine.GameEngineListener;
import pcd.poool.controller.engine.GameOverEvent;
import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorAccumulatorBasedCollisionResolver implements CollisionResolver, GameEngineListener {

    private final int threadCount = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    @Override
    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();

        CollisionAccumulator[][] accumulatorMatrix = new CollisionAccumulator[threadCount][n];
        for (int i = 0; i < threadCount; i++) {
            for (int j = 0; j < n; j++) {
                accumulatorMatrix[i][j] = new CollisionAccumulator();
            }
        }

        CountDownLatch calcLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    CollisionAccumulator[] myAccumulators = accumulatorMatrix[index];
                    for (int j = index; j < n / 2; j += threadCount) {
                        processRow(j, balls, myAccumulators);
                        processRow(n - 1 - j, balls, myAccumulators);
                    }
                    if (n % 2 != 0 && index == 0) {
                        processRow(n / 2, balls, myAccumulators);
                    }
                } finally {
                    calcLatch.countDown();
                }
            });
        }
        calcLatch.await();

        CountDownLatch reductionLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    for (int ballIndex = index; ballIndex < n; ballIndex += threadCount) {
                        reduceBall(ballIndex, balls, accumulatorMatrix);
                    }
                } finally {
                    reductionLatch.countDown();
                }
            });
        }
        reductionLatch.await();
    }

    private void processRow(int i, List<Ball> balls, CollisionAccumulator[] accumulators) {
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.calculateCollision(i, j, balls, accumulators);
        }
    }

    private void reduceBall(int ballIndex, List<Ball> balls, CollisionAccumulator[][] matrix) {
        double dx = 0, dy = 0, dvx = 0, dvy = 0;
        Set<Ball> colliders = new HashSet<>();
        for (int i = 0; i < threadCount; i++) {
            CollisionAccumulator acc = matrix[i][ballIndex];
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
            ball.setLastColliders(colliders);
        }
    }

    @Override
    public void onEngineTimeout(EngineTimeoutEvent event) {
        executor.shutdown();
    }

    @Override
    public void onGameOver(GameOverEvent event) {
        executor.shutdown();
    }
}