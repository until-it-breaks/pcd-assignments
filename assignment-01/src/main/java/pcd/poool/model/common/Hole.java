package pcd.poool.model.common;

public class Hole {
    private final P2d pos;
    private final double radius;

    public Hole(P2d pos, double radius) {
        this.pos = pos;
        this.radius = radius;
    }

    public P2d pos() {
        return this.pos;
    }

    public double radius() {
        return this.radius;
    }
}
