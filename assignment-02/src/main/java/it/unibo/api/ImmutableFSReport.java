package it.unibo.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ImmutableFSReport(List<ImmutableBucket> buckets) {
    public ImmutableFSReport(List<ImmutableBucket> buckets) {
        this.buckets = Collections.unmodifiableList(buckets);
    }

    public int getTotalFiles() {
        return buckets.stream()
                .mapToInt(ImmutableBucket::count)
                .sum();
    }

    public ImmutableFSReport combine(ImmutableFSReport other) {
        List<ImmutableBucket> newBuckets = new ArrayList<>();
        for (int i = 0; i < this.buckets.size(); i++) {
            newBuckets.add(this.buckets.get(i).combine(other.buckets().get(i)));
        }
        return new ImmutableFSReport(newBuckets);
    }
}