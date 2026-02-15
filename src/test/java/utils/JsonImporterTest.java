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

    private static final String TEST_JSON_FILE = "test_projects.json";

    @BeforeEach
    public void setUp() throws IOException {
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

    @Test
    public void testImportProjects() throws IOException {
        List<Map<String, Object>> projects = JsonImporter.importProjects(TEST_JSON_FILE);

        assertEquals(2, projects.size());

        Map<String, Object> projectA = projects.get(0);
        assertEquals(1, projectA.get("id"));
        assertEquals("Project A", projectA.get("name"));
        assertEquals("Model", projectA.get("project_type"));
        assertEquals("path/to/projectA", projectA.get("file_path"));
        assertEquals("Description for Project A", projectA.get("description"));
        assertEquals("2025-09-01", projectA.get("created_date"));

        Map<String, Object> projectB = projects.get(1);
        assertEquals(2, projectB.get("id"));
        assertEquals("Project B", projectB.get("name"));
        assertEquals("Prototype", projectB.get("project_type"));
        assertEquals("path/to/projectB", projectB.get("file_path"));
        assertEquals("Description for Project B", projectB.get("description"));
        assertEquals("2025-09-02", projectB.get("created_date"));
    }

    @Test
    public void testValidateJsonStructure_ValidFile() {
        assertTrue(JsonImporter.validateJsonStructure(TEST_JSON_FILE));
    }

    @Test
    public void testValidateJsonStructure_InvalidFile() throws IOException {
        String invalidJson = "{ invalid json }";
        String invalidFile = "invalid_projects.json";

        try (FileWriter writer = new FileWriter(invalidFile)) {
            writer.write(invalidJson);
        }

        assertFalse(JsonImporter.validateJsonStructure(invalidFile));
    }
}
