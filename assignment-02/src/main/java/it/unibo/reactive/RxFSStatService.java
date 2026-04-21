package it.unibo.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.unibo.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.BaseStream;

/**
 * A service that scans the file system reactively, providing both
 * periodic updates and a final comprehensive report.
 */
public class RxFSStatService {

    public Observable<ImmutableFSReport> getFSReportUpdates(ScanParameters parameters) {
        // Initial empty state for the report (0 files in all buckets)
        var seed = new ImmutableFSReport(Buckets.createImmutableBuckets(parameters.maxFileSize(), parameters.bandCount()));
        Observable<ImmutableFSReport> base = scanDirectory(parameters)
                // Every time a new report arrives, combine it with the current accumulated one and return the result.
                .scan(seed, ImmutableFSReport::combine)
                // Ensures the initial directory listing starts on an IO thread rather than the caller's thread (e.g. EDT)
                .subscribeOn(Schedulers.io());
        Observable<ImmutableFSReport> shared = base
                // Caches the most recent emission so new subscribers don't miss latest data due to bad timings
                .replay(1)
                // Keeps the upstream connection alive as long as there is at least one observer
                .refCount();

        // Emits the latest state every 100ms. This prevents the UI from freezing by trying to render thousands of updates per second.
        Observable<ImmutableFSReport> ui = shared
                .throttleLast(100, TimeUnit.MILLISECONDS);

        // Captures the final report
        Single<ImmutableFSReport> finalReport = shared
                .last(seed);

        // Merge the periodic updates with the final complete report
        return Observable.merge(ui, finalReport.toObservable())
                .distinctUntilChanged();
    }

    private Observable<ImmutableFSReport> scanDirectory(ScanParameters parameters) {
        // Ensures the 'Files.list' stream is closed automatically when the Observable is finished or disposed.
        return Observable.using(
            () -> Files.list(parameters.directory()),
            stream -> Observable.fromStream(stream)
                .flatMap(path -> {
                    if (Files.isDirectory(path)) {
                        // Start a new scan for the subdirectory
                        return scanDirectory(parameters.withDirectory(path))
                                // Fork this subdirectory scan onto a new IO thread
                                .subscribeOn(Schedulers.io())
                                .onErrorResumeNext(_ -> Observable.empty());
                    } else if (Files.isRegularFile(path)) {
                        // Create a report for a single file
                        return Observable.fromCallable(() -> createFileReport(parameters.withDirectory(path)))
                                // Fork the file metadata read onto a new IO thread
                                .subscribeOn(Schedulers.io())
                                .doOnError(e -> System.err.println(e.toString()))
                                .onErrorResumeNext(_ -> Observable.empty());
                    } else {
                        return Observable.empty();
                    }
                }, 8), // Process up to 8 paths (files/folders) in parallel
            BaseStream::close
            )
            .doOnError(e -> System.err.println(e.toString()));
    }

    /**
     * Reads file size and maps it to the appropriate histogram bucket.
     */
    private ImmutableFSReport createFileReport(ScanParameters parameters) throws IOException {
        Path path = parameters.directory();
        long fileSize = Files.size(path);   // Blocking IO call
        List<ImmutableBucket> initialBuckets = Buckets.createImmutableBuckets(parameters.maxFileSize(), parameters.bandCount());
        List<ImmutableBucket> updatedBuckets = initialBuckets.stream()
                .map(bucket -> bucket.matches(fileSize) ? bucket.add(1) : bucket)
                .toList();
        return new ImmutableFSReport(updatedBuckets);
    }
}
