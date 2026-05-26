package it.unibo.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameManagerImpl extends UnicastRemoteObject implements GameManager {

    private final Map<String, Game> games;

    protected GameManagerImpl() throws RemoteException {
        super();
        this.games = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void createGame(String gameId) throws RemoteException {
        if (games.containsKey(gameId)) {
            throw new RemoteException("A game named [" + gameId + "] already exists");
        }
        games.put(gameId, new GameImpl(gameId));
    }

    @Override
    public synchronized Game joinGame(String gameId, UUID playerId) throws RemoteException {
        Game game = games.get(gameId);
        if (game == null) {
            throw new RemoteException("Game not found: [" + gameId + "]");
        }
        game.addPlayer(playerId);
        return game;
    }
}
