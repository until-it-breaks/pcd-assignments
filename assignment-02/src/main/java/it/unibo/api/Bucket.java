package it.unibo.api;

public class Bucket {
    private int count;
    private final String label;

    public Bucket(String label) {
        this.label = label;
    }

    public void increment(int amount) {
        count += amount;
    }

    public int getCount() {
        return count;
    }

    public String getLabel() {
        return label;
    }
}
