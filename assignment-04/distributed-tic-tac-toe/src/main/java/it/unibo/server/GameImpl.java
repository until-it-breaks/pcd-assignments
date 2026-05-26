package it.unibo.server;

import it.unibo.shared.Board;
import it.unibo.shared.CellState;
import it.unibo.shared.GameStatus;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class GameImpl extends UnicastRemoteObject implements Game {
    private final String gameId;
    private final Board board;
    private UUID firstPlayerId;
    private UUID secondPlayerId;
    private UUID currentTurnPlayerId;
    private GameStatus gameStatus = GameStatus.WAITING_FOR_PLAYERS;

    public GameImpl(String gameId) throws RemoteException {
        super();
        this.gameId = gameId;
        this.board = new Board();
        log("New game instance created [" + gameId + "]");
    }

    @Override
    public synchronized void addPlayer(UUID playerId) throws RemoteException {
        if (firstPlayerId == null) {
            firstPlayerId = playerId;
            log("Player 1 joined: " + playerId);
        } else if (secondPlayerId == null) {
            secondPlayerId = playerId;
            currentTurnPlayerId = firstPlayerId;
            gameStatus = GameStatus.ONGOING;
            log("Player 2 joined: " + playerId + ". . First turn: " + currentTurnPlayerId);
        } else {
            log("Player " + playerId + " attempted to join, but room is full.");
        }
    }

    @Override
    public synchronized void makeMove(UUID playerId, int row, int col) throws RemoteException {
        if (gameStatus != GameStatus.ONGOING) {
            log("Move rejected from " + playerId + ": Game is not active (Status: " + gameStatus + ")");
            return;
        }
        if (!playerId.equals(currentTurnPlayerId)) {
            log("Move rejected: Player " + playerId + " attempted to play out of turn. Active turn is: " + currentTurnPlayerId);
            return;
        }
        CellState symbol = playerId.equals(firstPlayerId) ? CellState.X : CellState.O;
        if (board.makeMove(row, col, symbol)) {
            log("Player " + symbol + " (" + playerId + ") made a valid move at [" + row + "," + col + "]");
            if (board.checkWin(symbol)) {
                gameStatus = (symbol == CellState.X) ? GameStatus.X_WON : GameStatus.O_WON;
                log("Match concluded. Result: " + gameStatus);
            } else if (board.isFull()) {
                gameStatus = GameStatus.DRAW;
                log("Match concluded. Result: DRAW");
            } else {
                currentTurnPlayerId = currentTurnPlayerId.equals(firstPlayerId) ? secondPlayerId : firstPlayerId;
                log("Turn advanced. Next up: " + currentTurnPlayerId);
            }
        } else {
            log("Move rejected from " + playerId + ": Cell [" + row + "," + col + "] is already occupied.");
        }
    }

    @Override
    public synchronized GameStatus getGameStatus() throws RemoteException {
        return gameStatus;
    }

    @Override
    public synchronized Board getBoard() throws RemoteException {
        return board;
    }

    @Override
    public synchronized UUID getActivePlayerId() throws RemoteException {
        return this.currentTurnPlayerId;
    }

    @Override
    public String getGameId() throws RemoteException {
        return gameId;
    }

    private void log(String message) {
        System.out.printf("[%s] %s%n", gameId, message);
    }
}
