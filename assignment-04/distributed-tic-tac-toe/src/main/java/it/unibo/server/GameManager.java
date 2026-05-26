package it.unibo.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface GameManager extends Remote {
    void createGame(String gameName) throws RemoteException;

    Game joinGame(String gameName, UUID playerId) throws RemoteException;
}
