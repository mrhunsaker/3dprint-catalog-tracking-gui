import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the FileUtils class.
 */
public class FileUtilsTest {

    private static final Path TEST_SOURCE_DIR = Paths.get("test_source");
    private static final Path TEST_DEST_DIR = Paths.get("test_dest");

    @BeforeEach
    public void setUp() throws IOException {
        // Create test source directory with files
        Files.createDirectories(TEST_SOURCE_DIR);
        Files.writeString(TEST_SOURCE_DIR.resolve("test_file.txt"), "Test content");

        // Ensure destination directory exists
        Files.createDirectories(TEST_DEST_DIR);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Delete test directories
        if (Files.exists(TEST_SOURCE_DIR)) {
            FileUtils.deleteDirectory(TEST_SOURCE_DIR);
        }
        if (Files.exists(TEST_DEST_DIR)) {
            FileUtils.deleteDirectory(TEST_DEST_DIR);
        }
    }

    @Test
    public void testCopyDirectoryWithConflictHandling() throws IOException {
        // Create a conflicting file in the destination directory
        Files.createDirectories(TEST_DEST_DIR);
        Files.writeString(TEST_DEST_DIR.resolve("test_file.txt"), "Old content");

        // Copy directory with conflict handling
        FileUtils.copyDirectoryWithConflictHandling(TEST_SOURCE_DIR, TEST_DEST_DIR);

        // Verify the file was overwritten
        assertTrue(Files.exists(TEST_DEST_DIR.resolve("test_file.txt")));
        assertEquals("Test content", Files.readString(TEST_DEST_DIR.resolve("test_file.txt")));
    }

    @Test
    public void testHasEnoughDiskSpace() throws IOException {
        // Check disk space for the test source directory
        assertTrue(FileUtils.hasEnoughDiskSpace(TEST_SOURCE_DIR, TEST_DEST_DIR));
    }

    @Test
    public void testVerifyIntegrity() throws IOException {
        // Copy directory
        FileUtils.copyDirectory(TEST_SOURCE_DIR, TEST_DEST_DIR);

        // Verify integrity
        assertTrue(FileUtils.verifyIntegrity(TEST_SOURCE_DIR, TEST_DEST_DIR));
    }

    @Test
    public void testDeleteDirectory() throws IOException {
        // Delete the source directory
        FileUtils.deleteDirectory(TEST_SOURCE_DIR);

        // Verify the directory was deleted
        assertFalse(Files.exists(TEST_SOURCE_DIR));
    }
}
