package it.unibo;

import io.vertx.core.Vertx;
import it.unibo.vertx.VertxFSStatService;

import java.nio.file.Path;

public class Main {
    static void main() {
        Vertx vertx = Vertx.vertx();
        VertxFSStatService service = new VertxFSStatService(vertx);
        service.getFSReport(Path.of("test-data"), 1000, 1)
            .onSuccess(report -> {
                System.out.println("Total files: " + report.getTotalFiles());
                report.getBuckets().forEach(bucket -> {
                    System.out.println(bucket.getLabel() + ": " + bucket.getCount());
                });
                vertx.close();
            })
            .onFailure(error -> {
                error.printStackTrace();
                vertx.close();
            });
    }
}
