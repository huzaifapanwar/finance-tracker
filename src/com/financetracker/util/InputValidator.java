package com.financetracker.util;

import com.financetracker.model.TransactionType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

/**
 * Centralized validation and resilient CLI input reading utility.
 * Guarantees that the application never crashes on invalid inputs and re-prompts continuously.
 */
public class InputValidator {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Checks if a string represents a valid date in yyyy-MM-dd format.
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Parses a date string in yyyy-MM-dd format.
     */
    public static LocalDate parseDate(String dateStr) {
        if (!isValidDate(dateStr)) {
            throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd, received: " + dateStr);
        }
        return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
    }

    /**
     * Checks if a string represents a valid month in yyyy-MM format.
     */
    public static boolean isValidMonth(String monthStr) {
        if (monthStr == null || monthStr.trim().isEmpty()) {
            return false;
        }
        try {
            YearMonth.parse(monthStr.trim(), MONTH_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Parses a month string in yyyy-MM format.
     */
    public static YearMonth parseMonth(String monthStr) {
        if (!isValidMonth(monthStr)) {
            throw new IllegalArgumentException("Invalid month format. Expected yyyy-MM, received: " + monthStr);
        }
        return YearMonth.parse(monthStr.trim(), MONTH_FORMATTER);
    }

    /**
     * Checks if an amount is strictly positive (> 0) and finite.
     */
    public static boolean isValidPositiveAmount(double amount) {
        return !Double.isNaN(amount) && !Double.isInfinite(amount) && amount > 0.0;
    }

    /**
     * Checks if an amount string represents a strictly positive number.
     */
    public static boolean isValidPositiveAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return false;
        }
        try {
            double value = Double.parseDouble(amountStr.trim());
            return isValidPositiveAmount(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parses a positive amount string.
     */
    public static double parsePositiveAmount(String amountStr) {
        if (!isValidPositiveAmount(amountStr)) {
            throw new IllegalArgumentException("Invalid amount. Must be a positive number: " + amountStr);
        }
        return Double.parseDouble(amountStr.trim());
    }

    /**
     * Checks if a string is non-null and not empty after trimming.
     */
    public static boolean isNonEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    // --- Resilient Interactive CLI Prompt Helpers ---

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (isNonEmpty(line)) {
                return line.trim();
            }
            System.out.println("  [!] Error: Input cannot be blank. Please try again.");
        }
    }

    public static String readOptionalString(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt + (defaultValue != null && !defaultValue.isEmpty() ? " [" + defaultValue + "]: " : ": "));
        String line = scanner.nextLine().trim();
        if (line.isEmpty() && defaultValue != null) {
            return defaultValue;
        }
        return line;
    }

    public static LocalDate readDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (isValidDate(line)) {
                return parseDate(line);
            }
            System.out.println("  [!] Error: Please enter a valid date in yyyy-MM-dd format (e.g. 2026-09-07).");
        }
    }

    public static LocalDate readOptionalDate(Scanner scanner, String prompt, LocalDate defaultDate) {
        while (true) {
            System.out.print(prompt + " [" + defaultDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "]: ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return defaultDate;
            }
            if (isValidDate(line)) {
                return parseDate(line);
            }
            System.out.println("  [!] Error: Please enter a valid date in yyyy-MM-dd format.");
        }
    }

    public static YearMonth readMonth(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (isValidMonth(line)) {
                return parseMonth(line);
            }
            System.out.println("  [!] Error: Please enter a valid month in yyyy-MM format (e.g. 2026-09).");
        }
    }

    public static double readPositiveAmount(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (isValidPositiveAmount(line)) {
                return parsePositiveAmount(line);
            }
            System.out.println("  [!] Error: Please enter a positive number greater than 0 (e.g. 49.99).");
        }
    }

    public static double readOptionalPositiveAmount(Scanner scanner, String prompt, double defaultAmount) {
        while (true) {
            System.out.print(prompt + String.format(java.util.Locale.US, " [%.2f]: ", defaultAmount));
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return defaultAmount;
            }
            if (isValidPositiveAmount(line)) {
                return parsePositiveAmount(line);
            }
            System.out.println("  [!] Error: Please enter a positive number greater than 0.");
        }
    }

    public static TransactionType readTransactionType(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return TransactionType.fromString(line);
            } catch (IllegalArgumentException e) {
                System.out.println("  [!] Error: Type must be either 'INCOME' or 'EXPENSE'.");
            }
        }
    }

    public static TransactionType readOptionalTransactionType(Scanner scanner, String prompt, TransactionType defaultType) {
        while (true) {
            System.out.print(prompt + " [" + defaultType.name() + "]: ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return defaultType;
            }
            try {
                return TransactionType.fromString(line);
            } catch (IllegalArgumentException e) {
                System.out.println("  [!] Error: Type must be either 'INCOME' or 'EXPENSE'.");
            }
        }
    }

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("  [!] Error: Value must be between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Error: Please enter a valid integer.");
            }
        }
    }
}
