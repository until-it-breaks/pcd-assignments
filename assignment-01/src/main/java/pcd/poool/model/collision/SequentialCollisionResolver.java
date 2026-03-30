package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;

import java.util.List;

public class SequentialCollisionResolver implements CollisionResolver {

    @Override
    public void resolve(List<Ball> balls) {
        int n = balls.size();
        for (int i = 0; i < n; i++) {
            Ball ball = balls.get(i);
            for (int j = i + 1; j < n; j++) {
                Balls.resolveCollisionFast(ball, balls.get(j));
            }
        }
    }
}
