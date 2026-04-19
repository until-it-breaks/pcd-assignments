package it.unibo.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.unibo.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.BaseStream;

public class RxFSStatService {

    public Observable<ImmutableFSReport> getFSReportUpdates(Path directory, long maxFileSize, int bandCount) {
        // Create the high-speed scanning stream
        Observable<ImmutableFSReport> scanStream = scanDir(directory, maxFileSize, bandCount)
                // Accumulate file data as fast as the disk can read
                .scan(new ImmutableFSReport(0, Buckets.createImmutableBuckets(maxFileSize, bandCount)), ImmutableFSReport::combineWith)
                // Multicast the results to avoid rerunning the scan
                .share();

        return scanStream
                // Only let the latest total through every 200ms to keep the receiver (GUI) responsive
                .throttleLast(200, TimeUnit.MILLISECONDS)
                // When the scan finishes, append the absolute final accurate report
                .concatWith(scanStream.takeLast(1))
                // If the final report was already caught by the last 200ms tick, don't send it twice
                .distinctUntilChanged()
                .observeOn(Schedulers.single());
    }

    private Observable<ImmutableFSReport> scanDir(Path directory, long maxFileSize, int bandCount) {
        return Observable.using(
            () -> Files.list(directory),
            pathStream -> Observable.fromStream(pathStream)
                .flatMap(path -> Observable.fromCallable(() -> {
                    if (Files.isDirectory(path)) return "DIR";
                    if (Files.isRegularFile(path)) return "FILE";
                    return "SKIP";
                })
                .subscribeOn(Schedulers.io()) // Perform the "is it a file/dir?" check on IO threads
                .flatMap(type -> {
                    if (type.equals("DIR")) {
                        return scanDir(path, maxFileSize, bandCount);
                    } else if (type.equals("FILE")) {
                        return Observable.fromCallable(() -> createFileReport(path, maxFileSize, bandCount))
                                .subscribeOn(Schedulers.io());
                    }
                    return Observable.empty();
                })),
            BaseStream::close
        );
    }

    private ImmutableFSReport createFileReport(Path path, long maxFileSize, int bandCount) throws IOException {
        long fileSize = Files.size(path);
        List<ImmutableBucket> buckets = Buckets.createImmutableBuckets(maxFileSize, bandCount);
        long bandSize = maxFileSize / bandCount;
        int index = (fileSize > maxFileSize) ? bandCount : (int) (fileSize / bandSize);
        if (index >= buckets.size()) index = buckets.size() - 1;
        buckets.set(index, buckets.get(index).add(1));
        return new ImmutableFSReport(1, buckets);
    }
}
