package it.unibo.virtualthreads;

import it.unibo.api.Bucket;
import it.unibo.api.Buckets;
import it.unibo.api.FSReport;
import it.unibo.api.ScanParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class VirtualThreadsFSStatService implements AutoCloseable {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public FSReport getFSReport(ScanParameters parameters) {
        return scanDirectory(parameters);
    }

    /**
     * Uses a Fork-Join strategy. Each directory scan runs in its own Virtual Thread,
     * while files within that directory are processed sequentially by that same thread.
     */
    private FSReport scanDirectory(ScanParameters parameters) {
        FSReport report = new FSReport(Buckets.createBuckets(parameters.maxFileSize(), parameters.bandCount()));
        List<Future<FSReport>> futures = new ArrayList<>();
        try (Stream<Path> stream = Files.list(parameters.directory())) {
            stream.forEach(path -> {
                if (Files.isDirectory(path)) {
                    // Fork: Sub-directories get their own Virtual Thread
                    futures.add(executor.submit(() -> scanDirectory(parameters.withDirectory(path))));
                } else if (Files.isRegularFile(path)) {
                    long fileSize = getFileSize(path);
                    assignToBucket(report, fileSize);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Join: Block and wait for all subdirectory results
        for (Future<FSReport> future : futures) {
            try {
                report.merge(future.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return report;
    }

    private long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assignToBucket(FSReport report, long size) {
        for (Bucket bucket : report.buckets()) {
            if (bucket.matches(size)) {
                bucket.increment(1);
                return;
            }
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
