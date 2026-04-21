package it.unibo.virtualthreads;

import it.unibo.api.FSReport;
import it.unibo.api.ScanParameters;

import java.nio.file.Path;

public class TestVirtualThreadsFSStatService {

    public static final int MAX_FILE_SIZE_BYTES = 1000;
    public static final int BAND_COUNT = 5;

    static void main() {
        try (var service = new VirtualThreadsFSStatService()) {
            System.out.println("Starting scan...");
            Path path = Path.of("."); // Set desired path here
            FSReport report = service.getFSReport(new ScanParameters(path, MAX_FILE_SIZE_BYTES, BAND_COUNT));
            System.out.println("Scan of [" + path.toAbsolutePath() + "] complete!");
            System.out.println(report);
        }
    }
}
