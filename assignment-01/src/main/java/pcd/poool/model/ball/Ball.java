package pcd.poool.model.ball;

import pcd.poool.model.board.Board;
import pcd.poool.model.common.P2d;
import pcd.poool.model.common.V2d;

import java.util.Set;

public interface Ball {
    int getId();

    P2d getPos();

    void setPos(P2d pos);

    V2d getVel();

    void setVel(V2d vel);

    double getRadius();

    double getMass();

    Set<Ball> getLastColliders();

    void setLastColliders(Set<Ball> lastColliders);

    void kick(V2d vel);

    void updateState(long dt, Board board);
}
