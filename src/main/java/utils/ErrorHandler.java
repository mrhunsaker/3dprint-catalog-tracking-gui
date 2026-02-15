package utils;

import java.util.logging.*;

/**
 * Centralized error and logging helper used across the UI and utilities.
 *
 * This class configures a file-based {@link java.util.logging.Logger} and
 * exposes convenience methods to log at common levels. It also provides a
 * helper to display user-facing error dialogs while ensuring the technical
 * details are written to the log file.
 */
public class ErrorHandler {

    /** Logger used for application diagnostics and error recording. */
    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());

    static {
        try {
            String logFilePath = System.getProperty("app.log.path", "app_home/logs/app.log");
            FileHandler fileHandler = new FileHandler(logFilePath, 1024 * 1024, 5, true); // 1MB per file, 5 files max
            fileHandler.setFormatter(new SimpleFormatter());
            synchronized (logger) {
                logger.addHandler(fileHandler);
                logger.setLevel(Level.ALL);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
        }
    }

    /**
     * Log a severe error with an exception.
     *
     * @param message short human-readable message
     * @param throwable throwable to include in the log
     */
    public static void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Log a warning message.
     *
     * @param message warning text
     */
    public static void logWarning(String message) {
        logger.log(Level.WARNING, message);
    }

    /**
     * Log an informational message.
     *
     * @param message info text
     */
    public static void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

    /**
     * Show a user-facing error dialog while logging technical details to the
     * configured log file. This should be used for errors that require user
     * attention.
     *
     * @param userMessage message shown to the user
     * @param technicalDetails technical details recorded in logs
     */
    public static void showErrorToUser(String userMessage, String technicalDetails) {
        logError(userMessage, new Exception(technicalDetails));
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null, userMessage, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ErrorHandler() {
        // utility class
    }
}
