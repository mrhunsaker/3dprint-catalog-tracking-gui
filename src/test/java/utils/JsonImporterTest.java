package utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JsonImporter utility class.
 */
public class JsonImporterTest {

    /** Public no-arg constructor required by the test framework. */
    public JsonImporterTest() {
    }

    /** Test JSON filename used by the fixture. */
    private static final String TEST_JSON_FILE = "test_projects.json";

    /** Prepare a small sample JSON file used by tests. */
    @BeforeEach
    public void setUp() throws IOException {
        // Create a small sample JSON file used by multiple tests
        // Create a sample JSON file for testing
        String sampleJson = """
        [
            {
                "id": 1,
                "name": "Project A",
                "project_type": "Model",
                "file_path": "path/to/projectA",
                "description": "Description for Project A",
                "created_date": "2025-09-01"
            },
            {
                "id": 2,
                "name": "Project B",
                "project_type": "Prototype",
                "file_path": "path/to/projectB",
                "description": "Description for Project B",
                "created_date": "2025-09-02"
            }
        ]
        """;

        try (FileWriter writer = new FileWriter(TEST_JSON_FILE)) {
            writer.write(sampleJson);
        }
    }

    /** Verify importing projects from a well-formed sample JSON file. */
    @Test
    public void testImportProjects() throws IOException {
        // Ensure importing returns the expected project structures
        List<Map<String, Object>> projects = JsonImporter.importProjects(TEST_JSON_FILE);
package utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JsonImporter utility class.
 */
public class JsonImporterTest {

    /** Public no-arg constructor required by the test framework. */
    public JsonImporterTest() {
    }

    /** Test JSON filename used by the fixture. */
    private static final String TEST_JSON_FILE = "test_projects.json";

    /** Prepare a small sample JSON file used by tests. */
    @BeforeEach
    public void setUp() throws IOException {
        // Create a small sample JSON file used by multiple tests
        // Create a sample JSON file for testing
        String sampleJson = """
        [
            {
                "id": 1,
                "name": "Project A",
                "project_type": "Model",
                "file_path": "path/to/projectA",
                "description": "Description for Project A",
                "created_date": "2025-09-01"
            },
            {
                "id": 2,
                "name": "Project B",
                "project_type": "Prototype",
                "file_path": "path/to/projectB",
                "description": "Description for Project B",
                "created_date": "2025-09-02"
            }
        ]
        """;

        try (FileWriter writer = new FileWriter(TEST_JSON_FILE)) {
            writer.write(sampleJson);
        }
    }

    /** Verify importing projects from a well-formed sample JSON file. */
    @Test
    public void testImportProjects() throws IOException {
        // Ensure importing returns the expected project structures
        List<Map<String, Object>> projects = JsonImporter.importProjects(TEST_JSON_FILE);
package utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link JsonImporter} utility.
 */
public class JsonImporterTest {

    /** Public no-arg constructor required by the test framework. */
    public JsonImporterTest() {
    }

    private static final String TEST_JSON_FILE = "test_projects.json";

    /** Create a small sample JSON file used by tests. */
    @BeforeEach
    public void setUp() throws IOException {
        String sampleJson = """
        [
          { "id": 1, "name": "Project A", "project_type": "Model", "file_path": "path/to/projectA", "description": "Description for Project A", "created_date": "2025-09-01" },
          { "id": 2, "name": "Project B", "project_type": "Prototype", "file_path": "path/to/projectB", "description": "Description for Project B", "created_date": "2025-09-02" }
        ]
        """;

        try (FileWriter writer = new FileWriter(TEST_JSON_FILE)) {
            writer.write(sampleJson);
        }
    }

    /** Verify importing returns expected project maps. */
    @Test
    public void testImportProjects() throws IOException {
        List<Map<String, Object>> projects = JsonImporter.importProjects(TEST_JSON_FILE);
        assertEquals(2, projects.size());
        assertEquals("Project A", projects.get(0).get("name"));
        assertEquals("Project B", projects.get(1).get("name"));
    }

    /** Validate structure acceptance for a well-formed file. */
    @Test
    public void testValidateJsonStructure_ValidFile() {
        assertTrue(JsonImporter.validateJsonStructure(TEST_JSON_FILE));
    }

    /** Validate structure rejection for malformed JSON. */
    @Test
    public void testValidateJsonStructure_InvalidFile() throws IOException {
        String invalidFile = "invalid_projects.json";
        try (FileWriter writer = new FileWriter(invalidFile)) {
            writer.write("{ invalid json }");
        }
        assertFalse(JsonImporter.validateJsonStructure(invalidFile));
    }
}
