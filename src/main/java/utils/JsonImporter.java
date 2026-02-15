package utils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Utilities to import project metadata from JSON files.
 * <p>
 * The expected JSON format is an array of objects where each object contains
 * fields such as `id`, `name`, `project_type`, `file_path`, `description` and
 * `created_date`.
 * </p>
 *
 * Example file content:
 * <pre>
 * [ { "id": 1, "name": "Box", "file_path": "/tmp/box", "description": "..." } ]
 * </pre>
 */
public class JsonImporter {

    /**
     * Read a JSON array of projects and return a list of maps with the extracted fields.
     *
     * @param filePath path to JSON file
     * @return list of project maps
     * @throws IOException when file cannot be read
     * @throws JSONException when parsing fails or expected keys are missing
     */
    public static List<Map<String, Object>> importProjects(String filePath) throws IOException, JSONException {
        List<Map<String, Object>> projects = new ArrayList<>();

        try (FileReader reader = new FileReader(filePath)) {
            StringBuilder jsonContent = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                jsonContent.append((char) i);
            }

            JSONArray projectsJson = new JSONArray(jsonContent.toString());
            for (int j = 0; j < projectsJson.length(); j++) {
                JSONObject projectJson = projectsJson.getJSONObject(j);
                Map<String, Object> project = new HashMap<>();

                project.put("id", projectJson.optInt("id", -1));
                project.put("name", projectJson.getString("name"));
                project.put("project_type", projectJson.optString("project_type", ""));
                project.put("file_path", projectJson.getString("file_path"));
                project.put("description", projectJson.optString("description", ""));
                project.put("created_date", projectJson.optString("created_date", ""));

                projects.add(project);
            }
        }

        return projects;
    }

    /**
     * Quick validation that the file contains a JSON array. Returns false on any parse or IO error.
     *
     * @param filePath path to validate
     * @return true when the file contains a parsable JSON array
     */
    public static boolean validateJsonStructure(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            StringBuilder jsonContent = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                jsonContent.append((char) i);
            }

            new JSONArray(jsonContent.toString()); // Attempt to parse as JSON array
            return true;
        } catch (IOException | JSONException e) {
            return false;
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private JsonImporter() {
        // utility class
    }
}
