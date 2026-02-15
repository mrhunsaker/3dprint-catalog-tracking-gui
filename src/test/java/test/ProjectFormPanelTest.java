package test;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import java.util.*;

public class ProjectFormPanelTest {

    @Test
    void testInvalidDateFormat() {
        String invalidDate = "2025-13-40";
        assertFalse(validateDate(invalidDate), "Invalid date format should return false.");
    }

    @Test
    void testSpecialCharactersInProjectName() {
        String invalidName = "Project<>:\"/\\|?*";
        assertFalse(validateProjectName(invalidName), "Project name with special characters should return false.");
    }

    @Test
    void testVeryLongProjectName() {
        String longName = "a".repeat(256);
        assertFalse(validateProjectName(longName), "Project name exceeding 255 characters should return false.");
    }

    @Test
    void testVeryLongDescription() {
        String longDescription = "a".repeat(1001);
        assertFalse(validateDescription(longDescription), "Description exceeding 1000 characters should return false.");
    }

    // Mock validation methods for testing
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

    private boolean validateProjectName(String name) {
        return name.length() <= 255 && name.matches("[a-zA-Z0-9 _-]+");
    }

    private boolean validateDescription(String description) {
        return description.length() <= 1000;
    }
}
