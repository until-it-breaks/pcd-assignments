package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;

import java.util.List;

public class SequentialCollisionResolver implements CollisionResolver {

    @Override
    public void resolve(List<Ball> balls, Ball player, Ball bot) {
        int n = balls.size();
        for (int i = 0; i < n; i++) {
            Ball b1 = balls.get(i);
            for (int j = i + 1; j < n; j++) {
                Balls.resolveCollisionFast(b1, balls.get(j));
            }
            if (player != null) {
                Balls.resolveCollisionFast(player, b1);
            }
            if (bot != null) {
                Balls.resolveCollisionFast(bot, b1);
            }
        }
        if (player != null && bot != null) {
            Balls.resolveCollisionFast(player, bot);
        }
    }
}
