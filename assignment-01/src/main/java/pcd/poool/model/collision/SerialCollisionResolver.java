package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;

import java.util.List;

/**
 * A single-threaded implementation of {@link CollisionResolver}.
 * <p>
 * This resolver uses a nested loop approach with a complexity of O(n²).
 * It is serviceable for small collections because it
 * avoids the overhead of context switching and synchronization.
 */
public class SerialCollisionResolver implements CollisionResolver {

    @Override
    public void resolve(List<Ball> balls) {
        int n = balls.size();
        for (int i = 0; i < n; i++) {
            Ball ball = balls.get(i);
            for (int j = i + 1; j < n; j++) {
                Balls.resolveCollisionUnsafe(ball, balls.get(j));
            }
        }
    }
}
