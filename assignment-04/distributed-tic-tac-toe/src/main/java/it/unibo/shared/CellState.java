package it.unibo.shared;

public enum CellState {
    EMPTY(" "),
    X("X"),
    O("O");

    private final String symbol;

    CellState(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return this.symbol;
    }
}
