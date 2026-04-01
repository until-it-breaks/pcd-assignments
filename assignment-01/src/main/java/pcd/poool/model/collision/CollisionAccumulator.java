package pcd.poool.model.collision;

public class CollisionAccumulator {
    private double dx;
    private double dy;
    private double dvx;
    private double dvy;

    public synchronized void add(double dx, double dy, double dvx, double dvy) {
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
}