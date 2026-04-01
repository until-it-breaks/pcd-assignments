package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;

import java.util.ArrayList;
import java.util.List;

public class RawThreadedCollisionResolver implements CollisionResolver {

    private final int threadCount = Runtime.getRuntime().availableProcessors();

    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();
        List<Thread> threads = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads.add(new Thread(() -> {
                for (int j = threadIndex; j < n / 2; j += threadCount) {
                    processRow(j, balls);           // Heavy row
                    processRow(n - 1 - j, balls);   // Light row
                }
                // Handle middle row for odd counts
                if (n % 2 != 0 && threadIndex == 0) {
                    processRow(n / 2, balls);
                }
            }));
            threads.get(i).start();
        }
        for (Thread t : threads) t.join();
    }

    private void processRow(int i, List<Ball> balls) {
        Ball ball = balls.get(i);
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.resolveCollisionSynchronized(ball, balls.get(j));
        }
    }
}
