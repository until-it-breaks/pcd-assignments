package it.unibo.api;

import java.util.List;

public record FSReport(List<Bucket> buckets) {

    public int getTotalFiles() {
        return buckets.stream()
                .mapToInt(Bucket::getCount)
                .sum();
    }

    public void merge(FSReport other) {
        for (int i = 0; i < this.buckets.size(); i++) {
            this.buckets.get(i).increment(other.buckets().get(i).getCount());
        }
    }

    @Override
    public String toString() {
        return "FSReport{" +
                "totalFiles=" + getTotalFiles() +
                ", buckets=" + buckets +
                '}';
    }
}
