package pcd.poool.model.ball;

import pcd.poool.model.board.Board;
import pcd.poool.model.common.Boundary;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BallImplementation implements Ball {
    private final int id;
    private P2d pos;
    private V2d vel;
    private final double radius;
    private final double mass;
    private Set<Ball> lastColliders;
    private static final double FRICTION_FACTOR = 0.25; 	/* 0 minimum */

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    public BallImplementation(P2d pos, double radius, double mass, V2d vel){
        this.pos = pos;
        this.radius = radius;
        this.mass = mass;
        this.vel = vel;
        this.lastColliders = new HashSet<>();
        this.id = ID_GENERATOR.getAndIncrement();
    }

    @Override
    public void updateState(long dt, Board board){
        double speed = vel.abs();
        double dt_scaled = dt * 0.001;
    	if (speed > 0.001) {
            double dec = FRICTION_FACTOR * dt_scaled;
            double factor = Math.max(0, speed - dec) / speed;
            vel = vel.mul(factor);
        } else {
        	vel = new V2d(0,0);
        }
        pos = pos.sum(vel.mul(dt_scaled));
     	applyBoundaryConstraints(board);
    }
    
    @Override
    public void kick(V2d vel) {
    	this.vel = vel;
    }

    /**
     * Keep the ball inside the boundaries, updating the velocity in the case of bounces
     */
    private void applyBoundaryConstraints(Board board){
        Boundary bounds = board.getBounds();
        if (pos.x() + radius > bounds.x1()){
            pos = new P2d(bounds.x1() - radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.x() - radius < bounds.x0()){
            pos = new P2d(bounds.x0() + radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.y() + radius > bounds.y1()){
            pos = new P2d(pos.x(), bounds.y1() - radius);
            vel = vel.getSwappedY();
        } else if (pos.y() - radius < bounds.y0()){
            pos = new P2d(pos.x(), bounds.y0() + radius);
            vel = vel.getSwappedY();
        }
    }

    @Override
    public P2d getPos() {
        return pos;
    }

    @Override
    public void setPos(P2d pos) {
        this.pos = pos;
    }

    @Override
    public double getMass() {
        return mass;
    }

    @Override
    public V2d getVel() {
        return vel;
    }

    @Override
    public void setVel(V2d vel) {
        this.vel = vel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public Set<Ball> getLatestColliders() {
        return lastColliders;
    }

    @Override
    public void setLatestColliders(Set<Ball> lastColliders) {
        this.lastColliders = lastColliders;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BallImplementation that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "BallImplementation{" + "id=" + id + '}';
    }
}
