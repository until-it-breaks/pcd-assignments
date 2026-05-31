package it.unibo.client;

import it.unibo.server.Game;
import it.unibo.server.GameManager;

import javax.swing.*;
import java.rmi.RemoteException;
import java.util.UUID;

public class GameSession {
    private final GameManager manager;
    private final UUID localPlayerId;
    private Game currentGame;
    private Timer pollingTimer;

    public GameSession(GameManager manager, UUID localPlayerId) {
        this.manager = manager;
        this.localPlayerId = localPlayerId;
    }

    public Game join(String gameId) throws Exception {
        currentGame = manager.joinGame(gameId, localPlayerId);
        return currentGame;
    }

    public void createGame(String gameId) throws Exception {
        manager.createGame(gameId);
    }

    public void startPolling(GameUpdateListener gameUpdateListener) {
        pollingTimer = new Timer(500, e -> {
            try {
                gameUpdateListener.onUpdate(currentGame.getBoard(), currentGame.getGameStatus());
            } catch (RemoteException ex) {
                pollingTimer.stop();
            }
        });
        pollingTimer.start();
    }

    public void stopPolling() {
        if (pollingTimer != null && pollingTimer.isRunning()) pollingTimer.stop();
    }

    public void makeMove(int row, int col) {
        new Thread(() -> {
            try {
                currentGame.makeMove(localPlayerId, row, col);
            } catch (RemoteException e) {
                System.out.println("Network Error: Failed to transmit move");
            }
        }).start();
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