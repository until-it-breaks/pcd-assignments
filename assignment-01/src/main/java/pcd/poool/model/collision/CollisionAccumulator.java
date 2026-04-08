package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;

import java.util.HashSet;
import java.util.Set;

/**
 * A thread-local container used to accumulate physical deltas during the collision detection phase.
 * <p>
 * This class acts as a temporary buffer for changes in position (dx, dy) and
 * velocity (dvx, dvy), as well as a record of involved {@link Ball} entities.
 * By storing these changes in an accumulator rather than applying them directly to
 * the model, the simulation can perform parallel calculations without the need
 * for expensive synchronization or locking.
 * </p>
 * <p>
 * This class is NOT thread-safe. It is designed to be owned by a single
 * processing thread during the "Map" phase of a collision resolution cycle.
 * The accumulated values are intended to be read and reduced (merged) by a
 * single thread or a coordinated pool of threads during the "Reduce" phase.
 * </p>
 */
public class CollisionAccumulator {
    private double dx;
    private double dy;
    private double dvx;
    private double dvy;

    private final Set<Ball> colliders = new HashSet<>();

    public void add(double dx, double dy, double dvx, double dvy) {
        this.dx += dx;
        this.dy += dy;
        this.dvx += dvx;
        this.dvy += dvy;
    }

    public double getDeltaX() {
        return dx;
    }

    public double getDeltaY() {
        return dy;
    }

    public double getDeltaVX() {
        return dvx;
    }

    public double getDeltaVY() {
        return dvy;
    }

    public void addCollider(Ball ball) {
        colliders.add(ball);
    }

    public Set<Ball> getColliders() {
        return colliders;
    }
}