package it.unibo.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Board implements Serializable {
    private final List<List<CellState>> cells;

    public Board() {
        cells = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            List<CellState> row = new ArrayList<>(3);
            for (int j = 0; j < 3; j++) {
                row.add(CellState.EMPTY);
            }
            cells.add(row);
        }
    }

    public List<List<CellState>> getCells() {
        return cells;
    }

    public boolean makeMove(int row,int col, CellState symbol){
        if (cells.get(row).get(col) != CellState.EMPTY){
            return false;
        } else {
            cells.get(row).set(col, symbol);
        }
        return true;
    }

    public boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells.get(i).get(j) == CellState.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkWin(CellState symbol) {
        // Rows
        for (int i = 0; i < 3; i++) {
            if (cells.get(i).get(0) == symbol && cells.get(i).get(1) == symbol && cells.get(i).get(2) == symbol) {
                return true;
            }
        }
        // Columns
        for (int j = 0; j < 3; j++) {
            if (cells.get(0).get(j) == symbol && cells.get(1).get(j) == symbol && cells.get(2).get(j) == symbol) {
                return true;
            }
        }
        // Diagonals
        if (cells.get(0).get(0) == symbol && cells.get(1).get(1) == symbol && cells.get(2).get(2) == symbol) {
            return true;
        }
        if (cells.get(0).get(2) == symbol && cells.get(1).get(1) == symbol && cells.get(2).get(0) == symbol) {
            return true;
        }
        return false;
    }
}
