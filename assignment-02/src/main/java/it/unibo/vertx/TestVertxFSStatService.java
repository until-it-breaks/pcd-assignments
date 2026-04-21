package it.unibo.vertx;

import io.vertx.core.Vertx;
import it.unibo.api.ScanParameters;

import java.nio.file.Path;

public class TestVertxFSStatService {

    public static final int MAX_FILE_SIZE_BYTES = 1000;
    public static final int BAND_COUNT = 5;

    static void main() {
        Vertx vertx = Vertx.vertx();
        VertxFSStatService service = new VertxFSStatService(vertx);
        System.out.println("Starting scan...");
        Path path = Path.of("."); // Set desired path here
        service.getFSReport(new ScanParameters(path, MAX_FILE_SIZE_BYTES, BAND_COUNT))
            .onSuccess(report -> {
                System.out.println("Scan of [" + path.toAbsolutePath() + "] complete!");
                System.out.println(report);
            })
            .onFailure(error -> System.err.println("Scan failed: " + error.getMessage()))
            .onComplete(_ -> vertx.close());
    }
}
