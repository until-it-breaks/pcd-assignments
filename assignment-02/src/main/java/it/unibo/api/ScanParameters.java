package it.unibo.api;

import java.nio.file.Path;

public record ScanParameters(Path directory, long maxFileSize, int bandCount) {
    public ScanParameters withDirectory(Path directory) {
        return new ScanParameters(directory, maxFileSize, bandCount);
    }
}