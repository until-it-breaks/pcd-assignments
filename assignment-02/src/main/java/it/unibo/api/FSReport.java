package it.unibo.api;

import java.util.List;

public class FSReport {
    private int totalFiles;
    private final List<Bucket> buckets;

    public FSReport(List<Bucket> buckets) {
        this.totalFiles = 0;
        this.buckets = buckets;
    }

    public void incrementTotalFiles(int amount) {
        totalFiles += amount;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }
}
