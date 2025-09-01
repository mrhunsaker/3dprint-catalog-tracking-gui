import java.awt.Desktop;
import java.io.File;
import java.io.File;
import java.io.IOException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.*;

public class FileUtils {

    /**
     * Recursively copies a directory and its contents.
     *
     * @param sourceDir The source directory to copy.
     * @param destDir   The destination directory.
     * @throws IOException if an I/O error occurs.
     */
    public static void copyDirectory(Path sourceDir, Path destDir)
        throws IOException {
        Files.walk(sourceDir).forEach(sourcePath -> {
                try {
                    Path destPath = destDir.resolve(
                        sourceDir.relativize(sourcePath)
                    );
                    Files.copy(
                        sourcePath,
                        destPath,
                        StandardCopyOption.REPLACE_EXISTING
                    );
                } catch (IOException e) {
                    // Handle or rethrow
                    System.err.println(
                        "Failed to copy file: " + e.getMessage()
                    );
                }
            });
    }

    /**
     * Opens a folder in the native file explorer.
     *
     * @param path The path of the folder to open.
     * @throws IOException if an I/O error occurs.
     */
    public static void openFolder(String path) throws IOException {
        Desktop.getDesktop().open(new File(path));
    }
}
