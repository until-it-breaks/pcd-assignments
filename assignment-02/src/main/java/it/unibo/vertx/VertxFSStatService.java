package it.unibo.vertx;

import io.vertx.core.*;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import it.unibo.api.Bucket;
import it.unibo.api.FSReport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VertxFSStatService {
    private final Vertx vertx;

    public VertxFSStatService(Vertx vertx) {
        this.vertx = vertx;
    }

    public Future<FSReport> getFSReport(Path directory, long maxFileSize, int bandCount) {
        return scanDir(directory.toString(), maxFileSize, bandCount);
    }

    private Future<FSReport> scanDir(String directory, long maxFileSize, int bandCount) {
        Promise<FSReport> promise = Promise.promise();
        FileSystem fs = this.vertx.fileSystem();
        fs.readDir(directory).onComplete((AsyncResult<List<String>> readDirResult) -> {
            if (readDirResult.failed()) {
                promise.fail(readDirResult.cause());
                return;
            }
            List<String> entries = readDirResult.result();
            List<Future<Void>> futures = new ArrayList<>();
            FSReport partial = new FSReport(createBuckets(maxFileSize, bandCount));
            for (String entry : entries) {
                Future<Void> entryProcessFuture = processEntry(entry, maxFileSize, bandCount, partial);
                futures.add(entryProcessFuture);
            }
            Future.all(futures).onComplete(res -> {
                if (res.failed()) {
                    promise.fail(res.cause());
                } else {
                    promise.complete(partial);
                }
            });
        });
        return promise.future();
    }

    private Future<Void> processEntry(String path, long maxFileSize, int bandCount, FSReport report) {
        Promise<Void> promise = Promise.promise();
        vertx.fileSystem().props(path).onComplete(propsRes -> {
            FileProps props = propsRes.result();
            if (props.isDirectory()) {
                scanDir(path, maxFileSize, bandCount).onComplete(scanDirResult -> {
                    merge(report, scanDirResult.result());
                    promise.complete();
                });
            } else {
                long size = props.size();
                report.incrementTotalFiles(1);
                updateBands(report, size, maxFileSize, bandCount);
                promise.complete();
            }
        });
        return promise.future();
    }

    private void updateBands(FSReport report, long size, long maxFileSize, int bandCount) {
        long bandSize = maxFileSize / bandCount;
        List<Bucket> buckets = report.getBuckets();
        if (size > maxFileSize) {
            buckets.get(bandCount).increment(1);
        } else {
            int index = (int) (size / bandSize);
            if (index >= bandCount) {
                index = bandCount - 1;
            }
            buckets.get(index).increment(1);
        }
    }

    private void merge(FSReport report, FSReport other) {
        report.incrementTotalFiles(other.getTotalFiles());
        for (int i = 0; i < report.getBuckets().size(); i++) {
            report.getBuckets().get(i).increment(other.getBuckets().get(i).getCount());
        }
    }

    private List<Bucket> createBuckets(long maxFileSize, int bandCount) {
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
}
