package it.unibo.server;

import it.unibo.shared.Board;
import it.unibo.shared.GameStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface Game extends Remote {
    void addPlayer(UUID playerId) throws RemoteException;

    void makeMove(UUID playerId, int row, int col) throws RemoteException;

    GameStatus getGameStatus() throws RemoteException;

    Board getBoard() throws RemoteException;

    UUID getActivePlayerId() throws RemoteException;

    String getGameId() throws RemoteException;
}