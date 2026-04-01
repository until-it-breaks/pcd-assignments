package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;

import java.util.ArrayList;
import java.util.List;

public class RawThreadedAccumulatorBasedCollisionResolver implements CollisionResolver {
    @Override
    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();
        int threadCount = Runtime.getRuntime().availableProcessors();

        List<List<CollisionAccumulator>> accumulatorMatrix = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            List<CollisionAccumulator> accumulatorRow = new ArrayList<>(n);
            for (int j = 0; j < n; j++) {
                accumulatorRow.add(new CollisionAccumulator());
            }
            accumulatorMatrix.add(accumulatorRow);
        }

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads.add(new Thread(() -> {
                List<CollisionAccumulator> accumulatorRow = accumulatorMatrix.get(index);
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
        for (Thread t : threads) t.join();

        for (int i = 0; i < n; i++) {
            double dx = 0, dy = 0, dvx = 0, dvy = 0;
            for (int j = 0; j < threadCount; j++) {
                CollisionAccumulator acc = accumulatorMatrix.get(j).get(i);
                dx += acc.getDeltaX();
                dy += acc.getDeltaY();
                dvx += acc.getDeltaVX();
                dvy += acc.getDeltaVY();
            }
            Ball ball = balls.get(i);
            ball.setPos(new P2d(ball.getPos().x() + dx, ball.getPos().y() + dy));
            ball.setVel(new V2d(ball.getVel().x() + dvx, ball.getVel().y() + dvy));
        }
    }

    private void processRow(int i, List<Ball> balls, List<CollisionAccumulator> acc) {
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.calculateCollision(i, j, balls, acc);
        }
    }
}