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
 * Utility class for importing data from JSON files.
 */
public class JsonImporter {

    /**
     * Imports projects from a JSON file.
     *
     * @param filePath The path to the JSON file.
     * @return A list of maps, each representing a project.
     * @throws IOException If an error occurs during file reading.
     * @throws JSONException If the JSON structure is invalid.
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

                // Validate and extract project fields
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
     * Validates the structure of a JSON file before importing.
     *
     * @param filePath The path to the JSON file.
     * @return true if the JSON structure is valid, false otherwise.
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
}
