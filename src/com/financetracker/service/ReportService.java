package com.financetracker.service;

import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service generating financial analytics: monthly totals, net balance,
 * category percentage breakdowns, and month-over-month spending trends.
 */
public class ReportService {
    private final TransactionService transactionService;

    public ReportService(TransactionService transactionService) {
        this.transactionService = Objects.requireNonNull(transactionService, "TransactionService cannot be null");
    }

    /**
     * DTO containing monthly income, expense, and net balance summary.
     */
    public static class MonthlySummary {
        private final YearMonth month;
        private final double totalIncome;
        private final double totalExpense;
        private final double netBalance;
        private final int transactionCount;

        public MonthlySummary(YearMonth month, double totalIncome, double totalExpense, int transactionCount) {
            this.month = month;
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.netBalance = totalIncome - totalExpense;
            this.transactionCount = transactionCount;
        }

        public YearMonth getMonth() {
            return month;
        }

        public double getTotalIncome() {
            return totalIncome;
        }

        public double getTotalExpense() {
            return totalExpense;
        }

        public double getNetBalance() {
            return netBalance;
        }

        public int getTransactionCount() {
            return transactionCount;
        }
    }

    /**
     * DTO containing category spending amount and relative percentage of total expense.
     */
    public static class CategorySpend {
        private final String category;
        private final double amount;
        private final double percentage;

        public CategorySpend(String category, double amount, double percentage) {
            this.category = category;
            this.amount = amount;
            this.percentage = percentage;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        public double getPercentage() {
            return percentage;
        }
    }

    /**
     * DTO representing month-over-month spending trend analysis.
     */
    public static class MonthOverMonthTrend {
        private final YearMonth currentMonth;
        private final YearMonth previousMonth;
        private final double currentExpense;
        private final double previousExpense;
        private final double absoluteChange;
        private final Double percentageChange; // null if previous month had 0 expense and current is 0

        public MonthOverMonthTrend(YearMonth currentMonth, YearMonth previousMonth, double currentExpense, double previousExpense) {
            this.currentMonth = currentMonth;
            this.previousMonth = previousMonth;
            this.currentExpense = currentExpense;
            this.previousExpense = previousExpense;
            this.absoluteChange = currentExpense - previousExpense;

            if (previousExpense == 0.0) {
                if (currentExpense == 0.0) {
                    this.percentageChange = 0.0;
                } else {
                    this.percentageChange = null; // Infinite / new baseline
                }
            } else {
                this.percentageChange = ((currentExpense - previousExpense) / previousExpense) * 100.0;
            }
        }

        public YearMonth getCurrentMonth() {
            return currentMonth;
        }

        public YearMonth getPreviousMonth() {
            return previousMonth;
        }

        public double getCurrentExpense() {
            return currentExpense;
        }

        public double getPreviousExpense() {
            return previousExpense;
        }

        public double getAbsoluteChange() {
            return absoluteChange;
        }

        public Double getPercentageChange() {
            return percentageChange;
        }

        public String getSummaryDescription() {
            if (previousExpense == 0.0 && currentExpense == 0.0) {
                return "No spending recorded in either " + previousMonth + " or " + currentMonth + ".";
            }
            if (previousExpense == 0.0) {
                return String.format("Spending in %s is $%.2f (no expenses recorded in %s for baseline comparison).",
                        currentMonth, currentExpense, previousMonth);
            }
            if (percentageChange == null) {
                return String.format("Spending changed by $%.2f.", absoluteChange);
            }
            if (percentageChange > 0) {
                return String.format("Spending increased by %.2f%% (+$%.2f) compared to %s.", percentageChange, absoluteChange, previousMonth);
            } else if (percentageChange < 0) {
                return String.format("Spending decreased by %.2f%% (-$%.2f) compared to %s.", Math.abs(percentageChange), Math.abs(absoluteChange), previousMonth);
            } else {
                return "Spending remained unchanged compared to " + previousMonth + ".";
            }
        }
    }

    /**
     * Computes the financial summary (total income, total expense, net balance) for a specific month.
     */
    public MonthlySummary getMonthlySummary(YearMonth month) {
        Objects.requireNonNull(month, "month cannot be null");
        List<Transaction> transactions = transactionService.getTransactionsByMonth(month);

        double totalIncome = 0.0;
        double totalExpense = 0.0;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome += t.getAmount();
            } else if (t.getType() == TransactionType.EXPENSE) {
                totalExpense += t.getAmount();
            }
        }

        return new MonthlySummary(month, totalIncome, totalExpense, transactions.size());
    }

    /**
     * Computes category-wise spend and percentage of total expense for a given month, sorted descending by spend.
     */
    public List<CategorySpend> getCategoryWiseSpendBreakdown(YearMonth month) {
        Objects.requireNonNull(month, "month cannot be null");
        List<Transaction> transactions = transactionService.getTransactionsByMonth(month);

        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpense = 0.0;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                categoryTotals.put(t.getCategory(), categoryTotals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                totalExpense += t.getAmount();
            }
        }

        List<CategorySpend> list = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            double amount = entry.getValue();
            double pct = (totalExpense > 0) ? (amount / totalExpense) * 100.0 : 0.0;
            list.add(new CategorySpend(entry.getKey(), amount, pct));
        }

        list.sort(Comparator.comparing(CategorySpend::getAmount).reversed());
        return list;
    }

    /**
     * Computes month-over-month spending trend comparing the specified month against the previous calendar month.
     */
    public MonthOverMonthTrend getMonthOverMonthSpendingTrend(YearMonth currentMonth) {
        Objects.requireNonNull(currentMonth, "currentMonth cannot be null");
        YearMonth prevMonth = currentMonth.minusMonths(1);

        double currentExpense = calculateTotalExpense(currentMonth);
        double prevExpense = calculateTotalExpense(prevMonth);

        return new MonthOverMonthTrend(currentMonth, prevMonth, currentExpense, prevExpense);
    }

    private double calculateTotalExpense(YearMonth month) {
        return transactionService.getTransactionsByMonth(month).stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}
