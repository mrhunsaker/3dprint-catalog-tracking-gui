package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for file operations.
 */
public class FileUtils {

    /**
     * Retries a file operation multiple times in case of transient failures.
     * @param operation The file operation to execute.
     * @param maxRetries The maximum number of retries.
     * @throws IOException If the operation fails after all retries.
     */
    public static void retryFileOperation(FileOperation operation, int maxRetries) throws IOException {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                operation.execute();
                return; // Exit if successful
            } catch (IOException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw e; // Rethrow after max retries
                }
                try {
                    Thread.sleep(1000); // Wait before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Operation interrupted during retry.", ie);
                }
            }
        }
    }

    /**
     * Verifies the integrity of a file by comparing its size before and after copying.
     * @param source The source file path.
     * @param target The target file path.
     * @return True if the file sizes match, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public static boolean verifyFileIntegrity(Path source, Path target) throws IOException {
        if (!Files.exists(source) || !Files.exists(target)) {
            return false;
        }
        return Files.size(source) == Files.size(target);
    }
}