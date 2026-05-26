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

    public void startGame() {
        session.stopPolling();
        boardPanel.reset();
        statusLabel.setText(" ");
        hasPromptedReplay = false;
        try {
            String gameId = promptForGameId();
            session.join(gameId);
            session.startPolling(this::updateUIState);
        } catch (Exception e) {
            System.out.println("Matchmaking Error: " + e.getMessage());
        }
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
                        new Thread(this::promptReplay).start();
                    }
                }
            }
        });
    }

    private void handleCellClick(int row, int col) {
        if (!session.isMyTurn()) return;
        session.makeMove(row, col);
    }

    private String promptForGameId() throws Exception {
        String[] options = { "Create Game", "Join Game" };
        String gameId = null;
        while (gameId == null) {
            int choice = JOptionPane.showOptionDialog(this, "Select Action:", "TicTacToe",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]
            );
            if (choice == JOptionPane.CLOSED_OPTION) {
                System.exit(0);
            }
            gameId = JOptionPane.showInputDialog(this, "Enter Room Name:");
            if (choice == 0 && gameId != null) {
                this.session.createGame(gameId);
            }
        }
        return gameId;
    }

    private void promptReplay() {
        int replayChoice = JOptionPane.showConfirmDialog(
                this, "The match has concluded. Would you like to play again?",
                "Play Again?", JOptionPane.YES_NO_OPTION);
        if (replayChoice == JOptionPane.YES_OPTION) {
            startGame();
        } else {
            System.exit(0);
        }
    }
}