package test;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for simple validation logic used by the project form.
 */
public class ProjectFormPanelTest {

    public ProjectFormPanelTest() {
    }

    @Test
    void testInvalidDateFormat() {
        String invalidDate = "2025-13-40";
        assertFalse(validateDate(invalidDate));
    }

    @Test
    void testSpecialCharactersInProjectName() {
        String invalidName = "Project<>:\\\"/\\|?*";
        assertFalse(validateProjectName(invalidName));
    }

    @Test
    void testVeryLongProjectName() {
        String longName = "a".repeat(256);
        assertFalse(validateProjectName(longName));
    }

    @Test
    void testVeryLongDescription() {
        String longDescription = "a".repeat(1001);
        assertFalse(validateDescription(longDescription));
    }

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
