package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;

import java.util.HashSet;
import java.util.Set;

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