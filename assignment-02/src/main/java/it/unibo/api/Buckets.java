package it.unibo.api;

import java.util.ArrayList;
import java.util.List;

public class Buckets {
    public static List<Bucket> createBuckets(long maxFileSize, int bandCount) {
        long bandSize = maxFileSize / bandCount;
        List<Bucket> buckets = new ArrayList<>();
        for (int i = 0; i < bandCount; i++) {
            long start = bandSize * i;
            long end = bandSize * (i + 1);
            buckets.add(new Bucket(start + "-" + end));
        }
        buckets.add(new Bucket(">" + maxFileSize));
        return buckets;
    }

    public static List<ImmutableBucket> createImmutableBuckets(long maxFileSize, int bandCount) {
        long bandSize = maxFileSize / bandCount;
        List<ImmutableBucket> buckets = new ArrayList<>();
        for (int i = 0; i < bandCount; i++) {
            long start = bandSize * i;
            long end = bandSize * (i + 1);
            buckets.add(new ImmutableBucket(start + "-" + end, 0));
        }
        buckets.add(new ImmutableBucket(">" + maxFileSize, 0));
        return buckets;
    }
}
