package com.financetracker.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic reusable CSV read/write/append helper used across services.
 * Ensures zero-dependency, safe file I/O operations with UTF-8 encoding.
 */
public class CsvStorage {

    /**
     * Reads all non-empty lines from a CSV file.
     * If the file does not exist, returns an empty list without error.
     *
     * @param filePath Path to the CSV file
     * @return List of lines in the CSV file
     * @throws IOException on I/O failure
     */
    public synchronized List<String> readLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    /**
     * Overwrites the CSV file with the provided lines.
     * Ensures parent directories are created if necessary.
     *
     * @param filePath Path to the CSV file
     * @param lines    List of string rows to write
     * @throws IOException on I/O failure
     */
    public synchronized void writeLines(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);
        ensureParentDirectory(path);

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Appends a single line to the CSV file.
     * Ensures parent directories are created if necessary.
     *
     * @param filePath Path to the CSV file
     * @param line     Single row to append
     * @throws IOException on I/O failure
     */
    public synchronized void appendLine(String filePath, String line) throws IOException {
        Path path = Paths.get(filePath);
        ensureParentDirectory(path);

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE)) {
            writer.write(line);
            writer.newLine();
        }
    }

    /**
     * Checks if the specified file exists.
     *
     * @param filePath Path to the file
     * @return true if exists, false otherwise
     */
    public boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Safely deletes a file (useful for test setup/teardown).
     *
     * @param filePath Path to the file
     * @return true if deleted, false if file did not exist
     * @throws IOException on I/O failure
     */
    public synchronized boolean deleteFile(String filePath) throws IOException {
        return Files.deleteIfExists(Paths.get(filePath));
    }

    private void ensureParentDirectory(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
