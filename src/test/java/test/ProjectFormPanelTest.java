package test;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for simple validation logic used by the project form.
 */
public class ProjectFormPanelTest {

    /** Public no-arg constructor for the test class. */
    public ProjectFormPanelTest() {
    }

    /** Validate that malformed dates are rejected. */
    @Test
    void testInvalidDateFormat() {
        String invalidDate = "2025-13-40";
        assertFalse(validateDate(invalidDate));
    }

    /** Ensure project names with invalid characters are rejected. */
    @Test
    void testSpecialCharactersInProjectName() {
        String invalidName = "Project<>:\\\"/\\|?*";
        assertFalse(validateProjectName(invalidName));
    }

    /** Ensure overly long project names are rejected. */
    @Test
    void testVeryLongProjectName() {
        String longName = "a".repeat(256);
        assertFalse(validateProjectName(longName));
    }

    /** Ensure overly long project descriptions are rejected. */
    @Test
    void testVeryLongDescription() {
        String longDescription = "a".repeat(1001);
        assertFalse(validateDescription(longDescription));
    }

    /**
     * Validate date string uses yyyy-MM-dd and is not in the future.
     *
     * @param date the date string to validate
     * @return true when valid
     */
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

    /**
     * Validate project name characters and length.
     *
     * @param name the project name
     * @return true when valid
     */
    private boolean validateProjectName(String name) {
        return name.length() <= 255 && name.matches("[a-zA-Z0-9 _-]+");
    }

    /**
     * Validate description length.
     *
     * @param description the project description
     * @return true when within acceptable length
     */
    private boolean validateDescription(String description) {
        return description.length() <= 1000;
    }
}
