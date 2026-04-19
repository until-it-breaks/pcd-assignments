package it.unibo.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ImmutableFSReport(long totalFiles, List<ImmutableBucket> buckets) {
    public ImmutableFSReport(long totalFiles, List<ImmutableBucket> buckets) {
        this.totalFiles = totalFiles;
        this.buckets = Collections.unmodifiableList(buckets);
    }

    public ImmutableFSReport combineWith(ImmutableFSReport other) {
        List<ImmutableBucket> newBuckets = new ArrayList<>();
        for (int i = 0; i < this.buckets.size(); i++) {
            ImmutableBucket combinedBucket = this.buckets.get(i).combine(other.buckets.get(i));
            newBuckets.add(combinedBucket);
        }
        return new ImmutableFSReport(this.totalFiles + other.totalFiles, newBuckets);
    }
}