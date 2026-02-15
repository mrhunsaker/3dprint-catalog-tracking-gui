package utils;

import java.io.IOException;

/**
 * Functional interface for file operations.
 */
@FunctionalInterface
public interface FileOperation {
    /**
     * Execute the file operation. Implementations should throw {@link IOException}
     * for any I/O related failure so callers can retry or report errors.
     *
     * @throws IOException when an I/O error occurs
     */
    void execute() throws IOException;
}