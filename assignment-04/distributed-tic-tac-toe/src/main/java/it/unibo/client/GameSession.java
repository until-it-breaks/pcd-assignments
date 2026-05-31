package it.unibo.client;

import it.unibo.server.Game;
import it.unibo.server.GameManager;
import it.unibo.shared.Board;
import it.unibo.shared.GameStatus;

import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameSession {
    private final GameManager manager;
    private final UUID localPlayerId;
    private Game currentGame;
    private ScheduledExecutorService scheduler;

    public GameSession(GameManager manager, UUID localPlayerId) {
        this.manager = manager;
        this.localPlayerId = localPlayerId;
    }

    public void joinGame(String gameId) throws Exception {
        currentGame = manager.joinGame(gameId, localPlayerId);
    }

    public void createGame(String gameId) throws Exception {
        manager.createGame(gameId);
    }

    public void startPolling(GameUpdateListener gameUpdateListener) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Board board = currentGame.getBoard();
                GameStatus status = currentGame.getGameStatus();
                gameUpdateListener.onUpdate(board, status);
            } catch (RemoteException ex) {
                stopPolling();
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void stopPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
    public void makeMove(int row, int col) {
        try {
            currentGame.makeMove(localPlayerId, row, col);
        } catch (RemoteException e) {
            System.out.println("Network Error: Failed to transmit move");
        }
    }

    public boolean isMyTurn() {
        try {
            if (currentGame != null) {
                UUID active = currentGame.getCurrentTurnPlayerId();
                return active != null && active.equals(localPlayerId);
            }
        } catch (RemoteException e) {
            System.out.println("Error checking whose turn it is: " + e.getMessage());
        }
        return false;
    }
}