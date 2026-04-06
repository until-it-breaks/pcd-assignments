package pcd.poool.model.ball;

import pcd.poool.model.collision.CollisionAccumulator;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;

import java.util.List;
import java.util.Set;

public class Balls {

    private static final double RESTITUTION_FACTOR = 0.5;

    public static void resolveCollisionUnsafe(Ball a, Ball b) {
        resolveCollision(a, b);
    }

    public static void resolveCollisionSynchronized(Ball a, Ball b) {
        Ball first = (a.getId() < b.getId()) ? a : b;
        Ball second = (a.getId() < b.getId()) ? b : a;
        synchronized(first) {
            synchronized(second) {
                resolveCollision(a, b);
            }
        }
    }

    /**
     * Resolving collision between 2 balls, updating their position and velocity
     */
    private static void resolveCollision(Ball a, Ball b) {
        /* check if there is a collision */
        /* compute dv = b.pos - a.pos vector */

        double dx = b.getPos().x() - a.getPos().x();
        double dy = b.getPos().y() - a.getPos().y();
        double dist = Math.hypot(dx, dy);
        double minD = a.getRadius() + b.getRadius();

        /*
         * There is a collision if the distance between the two balls is less than the sum of the radii
         *
         */
        if (dist < minD && dist > 1e-6) {
            a.setLatestColliders(Set.of(b));
            b.setLatestColliders(Set.of(a));

            /*
             * Collision case - what to do:
             *
             * 1) solve overlaps, moving balls
             * 2) update velocities
             *
             */

            /* dvn = V2d(nx,ny) = dv unit vector */

            double nx = dx / dist;
            double ny = dy / dist;

            /*
             *
             * Update positions to solve overlaps, moving balls along dvn
             * - the displacements is proportional to the mass
             *
             */
            double overlap = minD - dist;
            double totalM = a.getMass() + b.getMass();

            double a_factor = overlap * (b.getMass() / totalM);
            double a_deltax = nx * a_factor;
            double a_deltay = ny * a_factor;

            a.setPos(new P2d(a.getPos().x() - a_deltax, a.getPos().y() - a_deltay));

            double b_factor = overlap * (a.getMass() / totalM);
            double b_deltax = nx * b_factor;
            double b_deltay = ny * b_factor;

            b.setPos(new P2d(b.getPos().x() + b_deltax, b.getPos().y() + b_deltay));

            /* Update velocities  */

            /* relative speed along the normal vector*/

            double dvx = b.getVel().x() - a.getVel().x();
            double dvy = b.getVel().y() - a.getVel().y();
            double dvn = dvx * nx + dvy * ny;

            if (dvn <= 0) { /* if not already separating, update velocities */
                double imp = -(1 + RESTITUTION_FACTOR) * dvn / (1.0 / a.getMass() + 1.0 / b.getMass());
                a.setVel(new V2d(a.getVel().x() - (imp / a.getMass()) * nx, a.getVel().y() - (imp / a.getMass()) * ny));
                b.setVel(new V2d(b.getVel().x() + (imp / b.getMass()) * nx, b.getVel().y() + (imp / b.getMass()) * ny));
            }
        }
    }

    public static void resolveCollisionWithAccumulators(int indexA, int indexB, List<Ball> balls, CollisionAccumulator[] accumulators) {
        Ball a = balls.get(indexA);
        Ball b = balls.get(indexB);
        double dx = b.getPos().x() - a.getPos().x();
        double dy = b.getPos().y() - a.getPos().y();
        double dist = Math.hypot(dx, dy);
        double minD = a.getRadius() + b.getRadius();

        if (dist < minD && dist > 1e-6) {
            double nx = dx / dist;
            double ny = dy / dist;

            double overlap = minD - dist;
            double totalM = a.getMass() + b.getMass();

            double a_factor = overlap * (b.getMass() / totalM);
            double b_factor = overlap * (a.getMass() / totalM);

            double a_dx = -nx * a_factor;
            double a_dy = -ny * a_factor;
            double b_dx = nx * b_factor;
            double b_dy = ny * b_factor;

            double dvx = b.getVel().x() - a.getVel().x();
            double dvy = b.getVel().y() - a.getVel().y();
            double dvn = dvx * nx + dvy * ny;

            double a_dvx = 0, a_dvy = 0, b_dvx = 0, b_dvy = 0;

            if (dvn <= 0) {
                double imp = -(1 + RESTITUTION_FACTOR) * dvn / (1.0 / a.getMass() + 1.0 / b.getMass());
                a_dvx = -(imp / a.getMass()) * nx;
                a_dvy = -(imp / a.getMass()) * ny;
                b_dvx = (imp / b.getMass()) * nx;
                b_dvy = (imp / b.getMass()) * ny;
            }
            CollisionAccumulator accumulatorA = accumulators[indexA];
            CollisionAccumulator accumulatorB = accumulators[indexB];

            accumulatorA.add(a_dx, a_dy, a_dvx, a_dvy);
            accumulatorA.addCollider(b);
            accumulatorB.add(b_dx, b_dy, b_dvx, b_dvy);
            accumulatorB.addCollider(a);
        }
    }
}
