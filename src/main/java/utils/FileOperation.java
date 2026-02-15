package utils;

import java.io.IOException;

/**
 * Functional interface for file operations.
 */
@FunctionalInterface
public interface FileOperation {
    void execute() throws IOException;
}