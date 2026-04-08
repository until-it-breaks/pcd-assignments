package pcd.poool.model.common;

public class P2d {
    private final double x;
    private final double y;

    public P2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public P2d sum(V2d v){
        return new P2d(x + v.x(), y + v.y());
    }

    public V2d sub(P2d v){
        return new V2d(x - v.x(), y - v.y());
    }
    
    public String toString(){
        return "P2d(" + x + "," + y + ")";
    }

    public double x() {
    	return x;
    }

    public double y() {
    	return y;
    }
}
