package com.financetracker.service;

import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.storage.CsvStorage;
import com.financetracker.util.AppLogger;

import java.io.IOException;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing category budget limits and monitoring monthly spending thresholds.
 */
public class BudgetService {
    public static final String DEFAULT_CSV_PATH = "data/budgets.csv";
    private static final String CSV_HEADER = "category,monthlyLimit";

    public enum Status {
        OK("OK"),
        WARNING("WARNING"),
        OVER_BUDGET("OVER BUDGET");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * DTO containing budget evaluation results for a category in a given month.
     */
    public static class BudgetReportItem {
        private final String category;
        private final double limit;
        private final double actualSpend;
        private final double remaining;
        private final double percentUsed;
        private final Status status;

        public BudgetReportItem(String category, double limit, double actualSpend, double remaining, double percentUsed, Status status) {
            this.category = category;
            this.limit = limit;
            this.actualSpend = actualSpend;
            this.remaining = remaining;
            this.percentUsed = percentUsed;
            this.status = status;
        }

        public String getCategory() {
            return category;
        }

        public double getLimit() {
            return limit;
        }

        public double getActualSpend() {
            return actualSpend;
        }

        public double getRemaining() {
            return remaining;
        }

        public double getPercentUsed() {
            return percentUsed;
        }

        public Status getStatus() {
            return status;
        }
    }

    private final String csvPath;
    private final CsvStorage storage;
    private final Map<String, Budget> budgets = new LinkedHashMap<>();

    public BudgetService() {
        this(DEFAULT_CSV_PATH, new CsvStorage());
    }

    public BudgetService(String csvPath, CsvStorage storage) {
        this.csvPath = Objects.requireNonNull(csvPath, "csvPath cannot be null");
        this.storage = Objects.requireNonNull(storage, "storage cannot be null");
        loadFromFile();
    }

    private synchronized void loadFromFile() {
        budgets.clear();
        try {
            List<String> lines = storage.readLines(csvPath);
            boolean firstLine = true;
            for (String line : lines) {
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("category,")) {
                        continue;
                    }
                }
                try {
                    Budget b = Budget.fromCsvRow(line);
                    budgets.put(b.getCategory().toLowerCase(), b);
                } catch (Exception e) {
                    AppLogger.logError("Parsing line in " + csvPath + ": " + line, e);
                }
            }
            AppLogger.info("Loaded " + budgets.size() + " budgets from " + csvPath);
        } catch (IOException e) {
            AppLogger.logError("Loading budgets from " + csvPath, e);
        }
    }

    private synchronized void saveToFile() {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        for (Budget b : budgets.values()) {
            lines.add(b.toCsvRow());
        }
        try {
            storage.writeLines(csvPath, lines);
        } catch (IOException e) {
            AppLogger.logError("Saving budgets to " + csvPath, e);
            throw new RuntimeException("Failed to persist budgets: " + e.getMessage(), e);
        }
    }

    /**
     * Sets or updates a monthly budget limit for a category and immediately persists to CSV.
     */
    public synchronized void setBudget(String category, double monthlyLimit) {
        Budget budget = new Budget(category, monthlyLimit);
        budgets.put(category.trim().toLowerCase(), budget);
        saveToFile();
        AppLogger.logAdd("Budget", budget);
    }

    /**
     * Retrieves all configured budgets.
     */
    public synchronized List<Budget> getAllBudgets() {
        return new ArrayList<>(budgets.values());
    }

    /**
     * Retrieves the budget for a specific category, if present.
     */
    public synchronized Optional<Budget> getBudget(String category) {
        if (category == null) return Optional.empty();
        return Optional.ofNullable(budgets.get(category.trim().toLowerCase()));
    }

    /**
     * Evaluates actual spend against budget limits for all configured budgets for the given month.
     * Status thresholds:
     * - OVER BUDGET: actual spend > limit
     * - WARNING: actual spend >= 80% of limit (and <= 100%)
     * - OK: actual spend < 80% of limit
     */
    public synchronized List<BudgetReportItem> getBudgetStatuses(YearMonth month, TransactionService transactionService) {
        Objects.requireNonNull(month, "Month cannot be null");
        Objects.requireNonNull(transactionService, "TransactionService cannot be null");

        List<Transaction> monthlyTransactions = transactionService.getTransactionsByMonth(month);

        // Sum expenses by category (case-insensitive)
        Map<String, Double> spendByCategory = new HashMap<>();
        for (Transaction t : monthlyTransactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                String catKey = t.getCategory().trim().toLowerCase();
                spendByCategory.put(catKey, spendByCategory.getOrDefault(catKey, 0.0) + t.getAmount());
            }
        }

        List<BudgetReportItem> report = new ArrayList<>();
        for (Budget budget : budgets.values()) {
            String key = budget.getCategory().toLowerCase();
            double actualSpend = spendByCategory.getOrDefault(key, 0.0);
            double limit = budget.getMonthlyLimit();
            double remaining = limit - actualSpend;
            double percentUsed = (limit > 0) ? (actualSpend / limit) * 100.0 : (actualSpend > 0 ? 100.0 : 0.0);

            Status status;
            if (actualSpend > limit) {
                status = Status.OVER_BUDGET;
            } else if (percentUsed >= 80.0) {
                status = Status.WARNING;
            } else {
                status = Status.OK;
            }

            report.add(new BudgetReportItem(budget.getCategory(), limit, actualSpend, remaining, percentUsed, status));
        }

        return report;
    }
}
