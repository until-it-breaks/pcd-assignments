package pcd.poool.model.ball;

import pcd.poool.model.board.Board;
import pcd.poool.model.P2d;
import pcd.poool.model.V2d;

public interface Ball {
    int getId();

    P2d getPos();

    void setPos(P2d pos);

    V2d getVel();

    void setVel(V2d vel);

    double getRadius();

    double getMass();

    Ball getLastCollider();

    void setLastCollider(Ball lastCollider);

    void kick(V2d vel);

    void updateState(long dt, Board board);
}
