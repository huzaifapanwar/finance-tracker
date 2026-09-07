package com.financetracker.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Centralized logging utility writing timestamped entries to app.log.
 * Captures all transaction additions, edits, deletions, and runtime errors.
 */
public class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("com.financetracker");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static FileHandler fileHandler;

    static {
        init();
    }

    private static synchronized void init() {
        try {
            // Remove existing default console handlers to keep CLI interface clean
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }

            LOGGER.setUseParentHandlers(false);

            if (fileHandler == null) {
                fileHandler = new FileHandler("app.log", true);
                fileHandler.setFormatter(new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
                        StringBuilder sb = new StringBuilder();
                        sb.append(timestamp)
                          .append(" [")
                          .append(record.getLevel().getName())
                          .append("] ")
                          .append(record.getMessage())
                          .append(System.lineSeparator());

                        if (record.getThrown() != null) {
                            StringWriter sw = new StringWriter();
                            try (PrintWriter pw = new PrintWriter(sw)) {
                                record.getThrown().printStackTrace(pw);
                            }
                            sb.append(sw);
                        }
                        return sb.toString();
                    }
                });
                LOGGER.addHandler(fileHandler);
                LOGGER.setLevel(Level.ALL);
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize AppLogger: " + e.getMessage());
        }
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message) {
        LOGGER.warning(message);
    }

    public static void severe(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void logAdd(String entity, Object details) {
        info("ADDED " + entity + ": " + details);
    }

    public static void logEdit(String entity, Object details) {
        info("EDITED " + entity + ": " + details);
    }

    public static void logDelete(String entity, Object details) {
        info("DELETED " + entity + ": " + details);
    }

    public static void logError(String context, Throwable throwable) {
        severe("ERROR in [" + context + "]: " + throwable.getMessage(), throwable);
    }
}
