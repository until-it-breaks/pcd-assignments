package pcd.poool.model.common;

public class Boundary {
    private final double x0;
    private final double y0;
    private final double x1;
    private final double y1;

    public Boundary(double x0, double y0, double x1, double y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public double x0() {
        return x0;
    }

    public double y0() {
        return y0;
    }

    public double x1() {
        return x1;
    }

    public double y1() {
        return y1;
    }
}
