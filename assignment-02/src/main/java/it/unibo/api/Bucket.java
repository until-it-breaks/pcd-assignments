package it.unibo.api;

public class Bucket {
    private final long start;
    private final long end;
    private final String label;
    private int count;

    public Bucket(long start, long end) {
        this.start = start;
        this.end = end;
        this.label = (end == Long.MAX_VALUE) ? ">" + (start - 1) : start + "-" + end;
    }

    public boolean matches(long size) {
        return size >= start && size <= end;
    }

    public void increment(int amount) {
        count += amount;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "label='" + label + '\'' +
                ", count=" + count +
                '}';
    }
}