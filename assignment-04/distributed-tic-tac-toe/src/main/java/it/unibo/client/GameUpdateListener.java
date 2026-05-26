package it.unibo.client;

import it.unibo.shared.Board;
import it.unibo.shared.GameStatus;

public interface GameUpdateListener {
    void onUpdate(Board board, GameStatus status);
}
