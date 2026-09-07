package com.financetracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model representing a financial transaction.
 */
public class Transaction {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private int id;
    private LocalDate date;
    private TransactionType type;
    private String category;
    private double amount;
    private String description;

    public Transaction(int id, LocalDate date, TransactionType type, String category, double amount, String description) {
        this.id = id;
        this.date = Objects.requireNonNull(date, "Date cannot be null");
        this.type = Objects.requireNonNull(type, "TransactionType cannot be null");
        this.category = Objects.requireNonNull(category, "Category cannot be null").trim();
        this.amount = amount;
        this.description = description == null ? "" : description.trim();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = Objects.requireNonNull(date, "Date cannot be null");
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = Objects.requireNonNull(type, "TransactionType cannot be null");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = Objects.requireNonNull(category, "Category cannot be null").trim();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    /**
     * Serializes this transaction into a CSV line format:
     * id,date,type,category,amount,description
     */
    public String toCsvRow() {
        return id + ","
                + date.format(DATE_FORMATTER) + ","
                + type.name() + ","
                + escapeCsv(category) + ","
                + String.format(java.util.Locale.US, "%.2f", amount) + ","
                + escapeCsv(description);
    }

    /**
     * Parses a CSV row into a Transaction object.
     *
     * @param csvLine Single CSV line
     * @return Transaction object
     * @throws IllegalArgumentException if format is invalid
     */
    public static Transaction fromCsvRow(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV line cannot be null or empty");
        }

        List<String> tokens = parseCsvLine(csvLine);
        if (tokens.size() < 6) {
            throw new IllegalArgumentException("Invalid transaction CSV line (expected 6 columns, got " + tokens.size() + "): " + csvLine);
        }

        try {
            int id = Integer.parseInt(tokens.get(0).trim());
            LocalDate date = LocalDate.parse(tokens.get(1).trim(), DATE_FORMATTER);
            TransactionType type = TransactionType.fromString(tokens.get(2).trim());
            String category = tokens.get(3).trim();
            double amount = Double.parseDouble(tokens.get(4).trim());
            String description = tokens.get(5).trim();

            return new Transaction(id, date, type, category, amount, description);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse transaction from CSV line [" + csvLine + "]: " + e.getMessage(), e);
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
                    i++; // skip escaped quote
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
        if (!(o instanceof Transaction that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Transaction #%d | %s | %-7s | %-15s | $%-9.2f | %s",
                id, date.format(DATE_FORMATTER), type.name(), category, amount, description);
    }
}
