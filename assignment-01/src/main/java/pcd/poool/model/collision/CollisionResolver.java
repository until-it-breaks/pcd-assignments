package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;

import java.util.List;

public interface CollisionResolver {
    void resolve(List<Ball> balls) throws InterruptedException;
}
