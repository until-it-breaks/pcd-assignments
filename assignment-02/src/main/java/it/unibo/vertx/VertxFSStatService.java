package it.unibo.vertx;

import io.vertx.core.*;
import it.unibo.api.Bucket;
import it.unibo.api.Buckets;
import it.unibo.api.FSReport;
import it.unibo.api.ScanParameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VertxFSStatService {
    private final Vertx vertx;

    public VertxFSStatService(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Entry point: Initiates the recursive asynchronous scan
     */
    public Future<FSReport> getFSReport(ScanParameters parameters) {
        return scanDirectory(parameters);
    }

    /**
     * Reads a directory, triggers sub-scans for each item,
     * and merges their reports once they all finish.
     */
    private Future<FSReport> scanDirectory(ScanParameters parameters) {
        // readDir is performed on a vertx worker thread
        return vertx.fileSystem().readDir(parameters.directory().toString())
            .compose(paths -> {
                log("Creating tasks for each path");
                List<Future<FSReport>> futures = new ArrayList<>();
                for (String path : paths) {
                    futures.add(processPath(parameters.withDirectory(Path.of(path))));
                }
                return Future.all(futures).map(_ -> {
                    log("Merging reports");
                    FSReport totalReport = new FSReport(Buckets.createBuckets(parameters.maxFileSize(), parameters.bandCount()));
                    for (Future<FSReport> future : futures) {
                        totalReport.merge(future.result());
                    }
                    return totalReport;
                });
            });
    }

    /**
     * Checks whether a path is a file or directory.
     * Recursively calls scanDirectory if it's a folder,
     * otherwise creates a mini-report for a single file.
     */
    private Future<FSReport> processPath(ScanParameters parameters) {
        // props is performed on a vertx worker thread
        return vertx.fileSystem().props(parameters.directory().toString())
            .compose(properties -> {
                log("Evaluating path");
                if (properties.isDirectory()) {
                    return scanDirectory(parameters);
                } else {
                    FSReport leafReport = new FSReport(Buckets.createBuckets(parameters.maxFileSize(), parameters.bandCount()));
                    assignToBucket(leafReport, properties.size());
                    return Future.succeededFuture(leafReport);
                }
            });
    }

    /**
     * Executed entirely on the Event Loop thread.
     */
    private void assignToBucket(FSReport report, long size) {
        log("Assigning to bucket");
        for (Bucket bucket : report.buckets()) {
            if (bucket.matches(size)) {
                bucket.increment(1);
                return;
            }
        }
    }

    private static void log(String msg) {
        System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
    }
}
