package test;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import java.util.*;

/**
 * Unit tests for simple validation logic used by the project form.
 */
public class ProjectFormPanelTest {

    /** Public no-arg constructor required by the test framework. */
    public ProjectFormPanelTest() {
    }

    /** Ensure invalid date formats are rejected by the validator. */
    @Test
    void testInvalidDateFormat() {
        // Invalid date strings should be rejected by the validator
        String invalidDate = "2025-13-40";
        assertFalse(validateDate(invalidDate), "Invalid date format should return false.");
    }

    /** Ensure project names with special filesystem characters are rejected. */
    @Test
    void testSpecialCharactersInProjectName() {
        // Project names containing special filesystem characters should be invalid
        String invalidName = "Project<>:\"/\\|?*";
        assertFalse(validateProjectName(invalidName), "Project name with special characters should return false.");
    }

    /** Ensure extremely long project names are rejected by length limits. */
    @Test
    void testVeryLongProjectName() {
        // Project names longer than 255 characters should be invalid
        String longName = "a".repeat(256);
        assertFalse(validateProjectName(longName), "Project name exceeding 255 characters should return false.");
    }

    /** Ensure descriptions exceeding allowed length are rejected. */
    @Test
    void testVeryLongDescription() {
        // Descriptions longer than 1000 characters should be invalid
        String longDescription = "a".repeat(1001);
        assertFalse(validateDescription(longDescription), "Description exceeding 1000 characters should return false.");
    }

    /** Mock validation helper: validate date string format and range. */
    private boolean validateDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date parsedDate = sdf.parse(date);
            return !parsedDate.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /** Mock validation helper: validate allowed project name characters and length. */
    private boolean validateProjectName(String name) {
        return name.length() <= 255 && name.matches("[a-zA-Z0-9 _-]+");
    }

    /** Mock validation helper: validate description length. */
    private boolean validateDescription(String description) {
        return description.length() <= 1000;
    }
}
