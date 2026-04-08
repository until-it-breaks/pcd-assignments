package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;

import java.util.List;

public class UnsafeThreadedCollisionResolver extends ThreadedCollisionResolver {

    public UnsafeThreadedCollisionResolver() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public UnsafeThreadedCollisionResolver(int threadCount) {
        super(threadCount);
    }

    @Override
    protected void processRow(int i, List<Ball> balls) {
        Ball ball = balls.get(i);
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.resolveCollisionUnsafe(ball, balls.get(j));
        }
    }
}
