package it.unibo.client;

import it.unibo.server.GameManager;
import it.unibo.shared.Board;
import it.unibo.shared.GameStatus;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class ClientGUI extends JFrame {
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final BoardPanel boardPanel;
    private final GameSession session;
    private boolean hasPromptedReplay = false;

    public ClientGUI(GameManager manager) {
        this.session = new GameSession(manager, UUID.randomUUID());
        this.boardPanel = new BoardPanel(this::handleCellClick);
        this.setTitle("TicTacToe");
        this.setSize(400, 550);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(null);
        this.add(statusLabel, BorderLayout.NORTH);
        this.add(boardPanel, BorderLayout.CENTER);
        this.setVisible(true);
    }

    public void start() {
        session.stopPolling();
        boardPanel.reset();
        hasPromptedReplay = false;
        int choice = JOptionPane.showOptionDialog(this, "Select Action:", "TicTacToe", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{ "Create Game", "Join Game" }, "Create Game");
        if (choice == JOptionPane.CLOSED_OPTION) System.exit(0);
        String gameId = JOptionPane.showInputDialog(this, "Enter Game Name:");
        if (gameId == null) return;
        boolean shouldCreate = choice == 0;
        statusLabel.setText("Connecting...");
        runAsync(() -> {
            if (shouldCreate) session.createGame(gameId);
            session.join(gameId);
            session.startPolling(this::updateUIState);
        });
    }

    private void updateUIState(Board board, GameStatus status) {
        SwingUtilities.invokeLater(() -> {
            boardPanel.refresh(board.getCells());
            switch (status) {
                case WAITING_FOR_PLAYERS -> statusLabel.setText("Waiting for opponent...");
                case ONGOING -> statusLabel.setText(session.isMyTurn() ? "Your Turn!" : "Opponent's Turn...");
                default -> {
                    statusLabel.setText("Match Finished: " + status);
                    if (!hasPromptedReplay) {
                        hasPromptedReplay = true;
                        promptReplay();
                    }
                }
            }
        });
    }

    private void handleCellClick(int row, int col) {
        runAsync(() -> {
            if (!session.isMyTurn()) return;
            session.makeMove(row, col);
        });
    }

    private void promptReplay() {
        int choice = JOptionPane.showConfirmDialog(this, "The match has ended. Would you like to play again?", "Play Again?", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            start();
        } else {
            System.exit(0);
        }
    }

    private void runAsync(RunnableWithException task) {
        new Thread(() -> {
            try {
                task.run();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showError(e));
            }
        }).start();
    }

    private void showError(Exception e) {
        statusLabel.setText("Error");
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }
}