package it.unibo.api;

public record ImmutableBucket(long start, long end, String label, int count) {

    public ImmutableBucket(long start, long end) {
        this(start, end, (end == Long.MAX_VALUE) ? ">" + (start - 1) : start + "-" + end, 0);
    }

    public boolean matches(long size) {
        return size >= start && size <= end;
    }

    public ImmutableBucket add(int amount) {
        return new ImmutableBucket(this.start, this.end, this.label, this.count + amount);
    }

    public ImmutableBucket combine(ImmutableBucket other) {
        return new ImmutableBucket(this.start, this.end, this.label, this.count + other.count);
    }
}