package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;

import java.util.List;

/**
 * Strategy interface for resolving collisions between a collection of {@link Ball} objects.
 * <p>
 * Implementations may vary in their approach to concurrency and performance.
 */
public interface CollisionResolver {
    void resolve(List<Ball> balls) throws InterruptedException;
}
