import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link FileUtils} class.
 *
 * These tests exercise directory copy/delete and integrity helpers.
 */
public class FileUtilsTest {
    /** Public no-arg constructor required by the test framework. */
    public FileUtilsTest() {
    }

    /** Path used as the source directory for test copies. */
    private static final Path TEST_SOURCE_DIR = Paths.get("test_source");

    /** Path used as the destination directory for test copies. */
    private static final Path TEST_DEST_DIR = Paths.get("test_dest");

    @BeforeEach
    /** Prepare test fixtures before each test. */
    @BeforeEach
    public void setUp() throws IOException {
        // Prepare test directories and files
        // Create test source directory with files
        Files.createDirectories(TEST_SOURCE_DIR);
        Files.writeString(TEST_SOURCE_DIR.resolve("test_file.txt"), "Test content");

        // Ensure destination directory exists
        Files.createDirectories(TEST_DEST_DIR);
    }

    @AfterEach
    /** Clean up test fixtures after each test. */
    @AfterEach
    public void tearDown() throws IOException {
        // Cleanup test directories after each test
        // Delete test directories
        if (Files.exists(TEST_SOURCE_DIR)) {
            FileUtils.deleteDirectory(TEST_SOURCE_DIR);
        }
        if (Files.exists(TEST_DEST_DIR)) {
            FileUtils.deleteDirectory(TEST_DEST_DIR);
        }
    }

    @Test
    /** Test copying a directory where destination contains conflicting files. */
    @Test
    public void testCopyDirectoryWithConflictHandling() throws IOException {
        // Verify copying overwrites existing files when appropriate
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
    /** Test disk space estimation for copying small test data. */
    @Test
    public void testHasEnoughDiskSpace() throws IOException {
        // Ensure disk space check returns true for small test data
        // Check disk space for the test source directory
        assertTrue(FileUtils.hasEnoughDiskSpace(TEST_SOURCE_DIR, TEST_DEST_DIR));
    }

    @Test
    /** Test verification of file integrity after copying. */
    @Test
    public void testVerifyIntegrity() throws IOException {
        // Verify that copied files report as identical
        // Copy directory
        FileUtils.copyDirectory(TEST_SOURCE_DIR, TEST_DEST_DIR);

        // Verify integrity
        assertTrue(FileUtils.verifyIntegrity(TEST_SOURCE_DIR, TEST_DEST_DIR));
    }

    @Test
    /** Test recursive deletion of directories. */
    @Test
    public void testDeleteDirectory() throws IOException {
        // Verify deletion of created test directories
        // Delete the source directory
        FileUtils.deleteDirectory(TEST_SOURCE_DIR);

        // Verify the directory was deleted
        assertFalse(Files.exists(TEST_SOURCE_DIR));
    }
}
