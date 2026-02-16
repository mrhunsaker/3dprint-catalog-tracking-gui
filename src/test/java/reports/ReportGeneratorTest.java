package reports;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the {@link ReportGenerator} utility. */
public class ReportGeneratorTest {

    /** Public no-arg constructor required by the test framework. */
    public ReportGeneratorTest() {}

    /**
     * Verify the generated project frequency report contains expected keys.
     */
    @Test
    public void testGenerateProjectFrequencyReport() {
        List<Map<String, Object>> projects = List.of(
            Map.of("name", "Project A"),
            Map.of("name", "Project B"),
            Map.of("name", "Project A")
        );

        JSONObject report = ReportGenerator.generateProjectFrequencyReport(projects);

        assertNotNull(report);
        assertEquals("Project A", report.getJSONObject("most_printed").get("key"));
        assertEquals(2, report.getJSONObject("most_printed").get("value"));
        assertEquals("Project B", report.getJSONObject("least_printed").get("key"));
        assertEquals(1, report.getJSONObject("least_printed").get("value"));
    }

    /**
     * Verify exporting a report to JSON writes a readable file.
     *
     * @throws IOException when file IO fails
     */
    @Test
    public void testExportReportToJson() throws IOException {
        JSONObject report = new JSONObject();
        report.put("key", "value");

        String filePath = "test_report.json";
        ReportGenerator.exportReportToJson(report, filePath);

        File file = new File(filePath);
        assertTrue(file.exists());

        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[(int) file.length()];
            reader.read(buffer);
            String content = new String(buffer);
            JSONObject readReport = new JSONObject(content);
            assertEquals("value", readReport.get("key"));
        }

        file.delete();
    }

    /**
     * Verify exporting a report to CSV writes a readable file.
     *
     * @throws IOException when file IO fails
     */
    @Test
    public void testExportReportToCsv() throws IOException {
        JSONObject report = new JSONObject();
        report.put("key1", "value1");
        report.put("key2", "value2");

        String filePath = "test_report.csv";
        ReportGenerator.exportReportToCsv(report, filePath);

        File file = new File(filePath);
        assertTrue(file.exists());

        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[(int) file.length()];
            reader.read(buffer);
            String content = new String(buffer);
            assertTrue(content.contains("key1,value1"));
            assertTrue(content.contains("key2,value2"));
        }

        file.delete();
    }
}
