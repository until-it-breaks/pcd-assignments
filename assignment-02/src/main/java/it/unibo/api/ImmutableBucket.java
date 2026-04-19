package it.unibo.api;

public record ImmutableBucket(String label, int count) {

    public ImmutableBucket add(int amount) {
        return new ImmutableBucket(this.label, this.count + amount);
    }

    public ImmutableBucket combine(ImmutableBucket other) {
        return new ImmutableBucket(this.label, this.count + other.count);
    }
}
