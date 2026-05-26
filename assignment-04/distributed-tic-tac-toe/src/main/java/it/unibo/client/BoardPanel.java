package it.unibo.client;

import it.unibo.shared.CellState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {
    private final List<List<JButton>> buttons = new ArrayList<>();

    public BoardPanel(CellClickListener cellClickListener) {
        this.setLayout(new GridLayout(3, 3, 5, 5));
        for (int i = 0; i < 3; i++) {
            List<JButton> buttonRow = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                JButton button = new JButton(" ");
                button.setFocusPainted(false);
                final int row = i, column = j;
                button.addActionListener(e -> cellClickListener.onClick(row, column));
                this.add(button);
                buttonRow.add(button);
            }
            buttons.add(buttonRow);
        }
    }

    public void refresh(List<List<CellState>> cells) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons.get(i).get(j).setText(cells.get(i).get(j).toString());
            }
        }
    }

    public void reset() {
        buttons.forEach(row -> row.forEach(button -> button.setText(" ")));
    }
}