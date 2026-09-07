package com.financetracker.model;

/**
 * Enumeration representing the type of a financial transaction.
 */
public enum TransactionType {
    INCOME("Income"),
    EXPENSE("Expense");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a string representation into a TransactionType.
     * Case-insensitive match on name or display name.
     *
     * @param value String input (e.g. "INCOME", "EXPENSE", "Income", "Expense")
     * @return Corresponding TransactionType
     * @throws IllegalArgumentException if no match is found
     */
    public static TransactionType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        String trimmed = value.trim();
        for (TransactionType type : values()) {
            if (type.name().equalsIgnoreCase(trimmed) || type.displayName.equalsIgnoreCase(trimmed)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type: '" + value + "'. Expected INCOME or EXPENSE.");
    }
}
