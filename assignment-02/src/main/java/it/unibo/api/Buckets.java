package it.unibo.api;

import java.util.ArrayList;
import java.util.List;

public class Buckets {
    public static List<Bucket> createBuckets(long maxFileSize, int bandCount) {
        long bandSize = Math.max(1, maxFileSize / bandCount);
        List<Bucket> buckets = new ArrayList<>();
        for (int i = 0; i < bandCount; i++) {
            long start = bandSize * i;
            long end = (i == bandCount - 1) ? maxFileSize : bandSize * (i + 1) - 1;
            buckets.add(new Bucket(start, end));
        }
        buckets.add(new Bucket(maxFileSize + 1, Long.MAX_VALUE));
        return buckets;
    }

    public static List<ImmutableBucket> createImmutableBuckets(long maxFileSize, int bandCount) {
        long bandSize = Math.max(1, maxFileSize / bandCount);
        List<ImmutableBucket> buckets = new ArrayList<>();
        for (int i = 0; i < bandCount; i++) {
            long start = bandSize * i;
            long end = (i == bandCount - 1) ? maxFileSize : bandSize * (i + 1) - 1;
            buckets.add(new ImmutableBucket(start, end));
        }
        buckets.add(new ImmutableBucket(maxFileSize + 1, Long.MAX_VALUE));
        return buckets;
    }
}
