package utils;

import java.util.logging.*;

/**
 * Centralized error handling utility class.
 */
public class ErrorHandler {

    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("app_home/logs/app.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
        }
    }

    /**
     * Logs an error message and exception details.
     *
     * @param message The error message.
     * @param throwable The exception to log.
     */
    public static void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Logs a warning message.
     *
     * @param message The warning message.
     */
    public static void logWarning(String message) {
        logger.log(Level.WARNING, message);
    }

    /**
     * Logs an informational message.
     *
     * @param message The informational message.
     */
    public static void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

    /**
     * Displays a user-friendly error message.
     *
     * @param userMessage The message to display to the user.
     * @param technicalDetails Technical details for logging.
     */
    public static void showErrorToUser(String userMessage, String technicalDetails) {
        logError(userMessage, new Exception(technicalDetails));
        // Display user-friendly message (e.g., JOptionPane or console output)
        System.err.println(userMessage);
    }
}
