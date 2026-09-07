package com.financetracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model representing a monthly category budget limit.
 */
public class Budget {
    private String category;
    private double monthlyLimit;

    public Budget(String category, double monthlyLimit) {
        this.category = Objects.requireNonNull(category, "Category cannot be null").trim();
        if (monthlyLimit < 0) {
            throw new IllegalArgumentException("Monthly limit cannot be negative");
        }
        this.monthlyLimit = monthlyLimit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = Objects.requireNonNull(category, "Category cannot be null").trim();
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        if (monthlyLimit < 0) {
            throw new IllegalArgumentException("Monthly limit cannot be negative");
        }
        this.monthlyLimit = monthlyLimit;
    }

    /**
     * Serializes this budget into a CSV row:
     * category,monthlyLimit
     */
    public String toCsvRow() {
        return escapeCsv(category) + "," + String.format(java.util.Locale.US, "%.2f", monthlyLimit);
    }

    /**
     * Parses a CSV row into a Budget object.
     *
     * @param csvLine Single CSV line
     * @return Budget object
     * @throws IllegalArgumentException if format is invalid
     */
    public static Budget fromCsvRow(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV line cannot be null or empty");
        }

        List<String> tokens = parseCsvLine(csvLine);
        if (tokens.size() < 2) {
            throw new IllegalArgumentException("Invalid budget CSV line (expected 2 columns, got " + tokens.size() + "): " + csvLine);
        }

        try {
            String category = tokens.get(0).trim();
            double limit = Double.parseDouble(tokens.get(1).trim());
            return new Budget(category, limit);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse budget from CSV line [" + csvLine + "]: " + e.getMessage(), e);
        }
    }

    private static String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        if (input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    current.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Budget budget)) return false;
        return category.equalsIgnoreCase(budget.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("Budget | %-15s | Limit: $%.2f", category, monthlyLimit);
    }
}
