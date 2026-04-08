package pcd.poool.jpf;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.BallImplementation;
import pcd.poool.model.collision.PooledCollisionResolver;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;

import java.util.ArrayList;
import java.util.List;

public class TestPooledCollisionResolver {
    public static void main(String[] args) throws InterruptedException {
        int numBalls = 4;
        List<Ball> balls = new ArrayList<>();
        for (int i = 0; i < numBalls; i++) {
            balls.add(new BallImplementation(new P2d(i * 0.1, 0), 0.15, 1.0, new V2d(0, 0)));
        }
        try (PooledCollisionResolver resolver = new PooledCollisionResolver(2)) {
            resolver.resolve(balls);
        }
    }
}
