package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;

import java.util.ArrayList;
import java.util.List;

public class RawThreadedCollisionResolver implements CollisionResolver {
    @Override
    public void resolve(List<Ball> balls, Ball player, Ball bot) throws InterruptedException {
        int threadCount = Runtime.getRuntime().availableProcessors() + 1;
        List<Thread> threads = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int offset = i;
            threads.add(i, new Thread(() -> {
                for (int j = offset; j < balls.size() - 1; j += threadCount) {
                    Ball b1 = balls.get(j);
                    for (int k = j + 1; k < balls.size(); k++) {
                        Balls.resolveCollisionsSafe(b1, balls.get(k));
                    }
                    if (player != null) {
                        Balls.resolveCollisionsSafe(player, b1);
                    }
                    if (bot != null) {
                        Balls.resolveCollisionsSafe(bot, b1);
                    }
                }
            }));
            threads.get(i).start();
        }
        for (Thread thread: threads) {
            thread.join();
        }
        if (player != null && bot != null) {
            Balls.resolveCollisionFast(player, bot);
        }
    }
}
