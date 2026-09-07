package com.financetracker;

import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.service.BudgetService;
import com.financetracker.service.ReportService;
import com.financetracker.service.TransactionService;
import com.financetracker.util.AppLogger;
import com.financetracker.util.InputValidator;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main command-line interface entry point for Personal Finance Tracker.
 * Provides an interactive numbered menu loop with full validation and top-level error safety.
 */
public class Main {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final ReportService reportService;
    private final Scanner scanner;

    public Main() {
        this.transactionService = new TransactionService();
        this.budgetService = new BudgetService();
        this.reportService = new ReportService(transactionService);
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        AppLogger.info("Starting Personal Finance Tracker CLI...");
        Main app = new Main();
        app.run();
    }

    /**
     * Executes the main CLI loop wrapped in an outer safety try-catch block.
     */
    public void run() {
        printWelcomeBanner();
        boolean running = true;

        while (running) {
            try {
                printMenu();
                int choice = InputValidator.readInt(scanner, "Enter your choice [0-9]: ", 0, 9);
                System.out.println();

                switch (choice) {
                    case 1 -> handleAddTransaction();
                    case 2 -> handleViewAllTransactions();
                    case 3 -> handleEditTransaction();
                    case 4 -> handleDeleteTransaction();
                    case 5 -> handleSetCategoryBudget();
                    case 6 -> handleViewBudgetStatus();
                    case 7 -> handleMonthlySummary();
                    case 8 -> handleCategoryBreakdown();
                    case 9 -> handleMonthOverMonthTrend();
                    case 0 -> {
                        running = false;
                        printExitMessage();
                    }
                    default -> System.out.println("  [!] Invalid choice. Please select from the menu.");
                }

                if (running) {
                    System.out.println();
                }
            } catch (Exception e) {
                // Top-level safety net: never let the application crash
                AppLogger.logError("Unhandled error in main menu loop", e);
                System.out.println("  [!] An unexpected error occurred: " + e.getMessage());
                System.out.println("      The incident has been recorded to app.log. Returning to main menu.");
                System.out.println();
            }
        }
    }

    private void printWelcomeBanner() {
        System.out.println("===============================================================");
        System.out.println("               PERSONAL FINANCE TRACKER (CLI)                 ");
        System.out.println("       Core Java SE 17+ | Zero Dependencies | Local CSV       ");
        System.out.println("===============================================================");
    }

    private void printMenu() {
        System.out.println("------------------------- MAIN MENU ---------------------------");
        System.out.println("  1. Add Transaction");
        System.out.println("  2. View All Transactions");
        System.out.println("  3. Edit a Transaction");
        System.out.println("  4. Delete a Transaction");
        System.out.println("  5. Set a Category Budget");
        System.out.println("  6. View Budget Status for a Month");
        System.out.println("  7. Monthly Income / Expense Summary");
        System.out.println("  8. Category-wise Spend Breakdown");
        System.out.println("  9. Month-over-Month Spending Trend");
        System.out.println("  0. Exit");
        System.out.println("---------------------------------------------------------------");
    }

    // 1. Add Transaction
    private void handleAddTransaction() {
        System.out.println("--- Add New Transaction ---");
        LocalDate defaultDate = LocalDate.now();
        LocalDate date = InputValidator.readOptionalDate(scanner, "Date (yyyy-MM-dd)", defaultDate);
        TransactionType type = InputValidator.readTransactionType(scanner, "Type (INCOME or EXPENSE): ");
        String category = InputValidator.readNonEmptyString(scanner, "Category (e.g. Salary, Food, Utilities): ");
        double amount = InputValidator.readPositiveAmount(scanner, "Amount ($): ");
        System.out.print("Description (optional): ");
        String description = scanner.nextLine().trim();

        Transaction transaction = transactionService.addTransaction(date, type, category, amount, description);
        System.out.printf("  [OK] Success: Added %s transaction #%d for $%.2f under '%s'.%n",
                transaction.getType(), transaction.getId(), transaction.getAmount(), transaction.getCategory());
    }

    // 2. View All Transactions
    private void handleViewAllTransactions() {
        System.out.println("--- All Recorded Transactions ---");
        List<Transaction> list = transactionService.getAllTransactions();
        if (list.isEmpty()) {
            System.out.println("  No transactions recorded yet.");
            return;
        }

        printTransactionTable(list);
    }

    // 3. Edit a Transaction
    private void handleEditTransaction() {
        System.out.println("--- Edit Transaction ---");
        if (transactionService.getCount() == 0) {
            System.out.println("  No transactions available to edit.");
            return;
        }

        int id = InputValidator.readInt(scanner, "Enter Transaction ID to edit: ", 1, Integer.MAX_VALUE);
        Optional<Transaction> opt = transactionService.getTransactionById(id);
        if (opt.isEmpty()) {
            System.out.printf("  [!] Transaction #%d not found.%n", id);
            return;
        }

        Transaction existing = opt.get();
        System.out.println("Editing current record: " + existing);
        System.out.println("(Press Enter to keep the current value in brackets)");

        LocalDate date = InputValidator.readOptionalDate(scanner, "Date", existing.getDate());
        TransactionType type = InputValidator.readOptionalTransactionType(scanner, "Type", existing.getType());
        String category = InputValidator.readOptionalString(scanner, "Category", existing.getCategory());
        double amount = InputValidator.readOptionalPositiveAmount(scanner, "Amount", existing.getAmount());
        String description = InputValidator.readOptionalString(scanner, "Description", existing.getDescription());

        boolean updated = transactionService.editTransaction(id, date, type, category, amount, description);
        if (updated) {
            System.out.printf("  [OK] Success: Transaction #%d has been updated.%n", id);
        } else {
            System.out.printf("  [!] Failed to update transaction #%d.%n", id);
        }
    }

    // 4. Delete a Transaction
    private void handleDeleteTransaction() {
        System.out.println("--- Delete Transaction ---");
        if (transactionService.getCount() == 0) {
            System.out.println("  No transactions available to delete.");
            return;
        }

        int id = InputValidator.readInt(scanner, "Enter Transaction ID to delete: ", 1, Integer.MAX_VALUE);
        Optional<Transaction> opt = transactionService.getTransactionById(id);
        if (opt.isEmpty()) {
            System.out.printf("  [!] Transaction #%d not found.%n", id);
            return;
        }

        Transaction existing = opt.get();
        System.out.println("Target transaction: " + existing);
        System.out.print("Are you sure you want to delete this transaction? (y/N): ");
        String confirmation = scanner.nextLine().trim();
        if (confirmation.equalsIgnoreCase("y") || confirmation.equalsIgnoreCase("yes")) {
            boolean deleted = transactionService.deleteTransaction(id);
            if (deleted) {
                System.out.printf("  [OK] Success: Transaction #%d deleted.%n", id);
            } else {
                System.out.printf("  [!] Failed to delete transaction #%d.%n", id);
            }
        } else {
            System.out.println("  Deletion canceled.");
        }
    }

    // 5. Set a Category Budget
    private void handleSetCategoryBudget() {
        System.out.println("--- Set Category Monthly Budget ---");
        String category = InputValidator.readNonEmptyString(scanner, "Category name (e.g. Groceries, Rent, Dining): ");
        double limit = InputValidator.readPositiveAmount(scanner, "Monthly budget limit ($): ");

        budgetService.setBudget(category, limit);
        System.out.printf("  [OK] Success: Monthly budget for '%s' set to $%.2f.%n", category, limit);
    }

    // 6. View Budget Status for a Month
    private void handleViewBudgetStatus() {
        System.out.println("--- View Monthly Budget Status ---");
        YearMonth defaultMonth = YearMonth.now();
        System.out.print("Enter month (yyyy-MM) [" + defaultMonth + "]: ");
        String input = scanner.nextLine().trim();
        YearMonth month = input.isEmpty() ? defaultMonth : (InputValidator.isValidMonth(input) ? InputValidator.parseMonth(input) : null);
        if (month == null) {
            month = InputValidator.readMonth(scanner, "Invalid format. Re-enter month (yyyy-MM): ");
        }

        List<BudgetService.BudgetReportItem> items = budgetService.getBudgetStatuses(month, transactionService);
        if (items.isEmpty()) {
            System.out.println("  No budgets configured. Use menu option 5 to set category budgets.");
            return;
        }

        System.out.println();
        System.out.printf("Budget Performance Report for %s:%n", month);
        System.out.println("+-----------------+--------------+--------------+--------------+---------+---------------+");
        System.out.println("| Category        | Monthly Limit| Actual Spend | Remaining    | % Used  | Status        |");
        System.out.println("+-----------------+--------------+--------------+--------------+---------+---------------+");
        for (BudgetService.BudgetReportItem item : items) {
            String statusBadge = switch (item.getStatus()) {
                case OK -> "[ OK ]";
                case WARNING -> "[ WARNING ]";
                case OVER_BUDGET -> "[ OVER BUDGET ]";
            };
            System.out.printf("| %-15s | $%11.2f | $%11.2f | $%11.2f | %6.1f%% | %-13s |%n",
                    item.getCategory(),
                    item.getLimit(),
                    item.getActualSpend(),
                    item.getRemaining(),
                    item.getPercentUsed(),
                    statusBadge);
        }
        System.out.println("+-----------------+--------------+--------------+--------------+---------+---------------+");
    }

    // 7. Monthly Summary
    private void handleMonthlySummary() {
        System.out.println("--- Monthly Income / Expense Summary ---");
        YearMonth defaultMonth = YearMonth.now();
        System.out.print("Enter month (yyyy-MM) [" + defaultMonth + "]: ");
        String input = scanner.nextLine().trim();
        YearMonth month = input.isEmpty() ? defaultMonth : (InputValidator.isValidMonth(input) ? InputValidator.parseMonth(input) : null);
        if (month == null) {
            month = InputValidator.readMonth(scanner, "Invalid format. Re-enter month (yyyy-MM): ");
        }

        ReportService.MonthlySummary summary = reportService.getMonthlySummary(month);
        System.out.println();
        System.out.printf("Financial Summary for %s (%d transactions):%n", summary.getMonth(), summary.getTransactionCount());
        System.out.println("--------------------------------------------------");
        System.out.printf("  Total Income:   $%12.2f%n", summary.getTotalIncome());
        System.out.printf("  Total Expenses: $%12.2f%n", summary.getTotalExpense());
        System.out.println("--------------------------------------------------");
        System.out.printf("  Net Balance:    $%12.2f %s%n",
                summary.getNetBalance(),
                summary.getNetBalance() >= 0 ? "(Surplus)" : "(Deficit)");
        System.out.println("--------------------------------------------------");
    }

    // 8. Category-wise Spend Breakdown
    private void handleCategoryBreakdown() {
        System.out.println("--- Category-wise Spend Breakdown ---");
        YearMonth defaultMonth = YearMonth.now();
        System.out.print("Enter month (yyyy-MM) [" + defaultMonth + "]: ");
        String input = scanner.nextLine().trim();
        YearMonth month = input.isEmpty() ? defaultMonth : (InputValidator.isValidMonth(input) ? InputValidator.parseMonth(input) : null);
        if (month == null) {
            month = InputValidator.readMonth(scanner, "Invalid format. Re-enter month (yyyy-MM): ");
        }

        List<ReportService.CategorySpend> breakdown = reportService.getCategoryWiseSpendBreakdown(month);
        if (breakdown.isEmpty()) {
            System.out.printf("  No expense transactions recorded for %s.%n", month);
            return;
        }

        double total = breakdown.stream().mapToDouble(ReportService.CategorySpend::getAmount).sum();
        System.out.println();
        System.out.printf("Expense Breakdown for %s (Total Expenses: $%.2f):%n", month, total);
        System.out.println("+----------------------+---------------+------------+------------------------------+");
        System.out.println("| Category             | Amount ($)    | Share (%)  | Visual Proportion            |");
        System.out.println("+----------------------+---------------+------------+------------------------------+");
        for (ReportService.CategorySpend cs : breakdown) {
            int barLength = (int) Math.round((cs.getPercentage() / 100.0) * 25);
            String bar = "#".repeat(Math.max(0, barLength)) + "-".repeat(Math.max(0, 25 - barLength));
            System.out.printf("| %-20s | $%12.2f | %9.1f%% | %s |%n",
                    cs.getCategory(), cs.getAmount(), cs.getPercentage(), bar);
        }
        System.out.println("+----------------------+---------------+------------+------------------------------+");
    }

    // 9. Month-over-Month Spending Trend
    private void handleMonthOverMonthTrend() {
        System.out.println("--- Month-over-Month Spending Trend ---");
        YearMonth defaultMonth = YearMonth.now();
        System.out.print("Enter target month (yyyy-MM) [" + defaultMonth + "]: ");
        String input = scanner.nextLine().trim();
        YearMonth currentMonth = input.isEmpty() ? defaultMonth : (InputValidator.isValidMonth(input) ? InputValidator.parseMonth(input) : null);
        if (currentMonth == null) {
            currentMonth = InputValidator.readMonth(scanner, "Invalid format. Re-enter month (yyyy-MM): ");
        }

        ReportService.MonthOverMonthTrend trend = reportService.getMonthOverMonthSpendingTrend(currentMonth);
        System.out.println();
        System.out.printf("Spending Trend Comparison: %s vs. %s%n", trend.getCurrentMonth(), trend.getPreviousMonth());
        System.out.println("-----------------------------------------------------------------");
        System.out.printf("  Previous Month (%s) Spending: $%10.2f%n", trend.getPreviousMonth(), trend.getPreviousExpense());
        System.out.printf("  Current Month  (%s) Spending: $%10.2f%n", trend.getCurrentMonth(), trend.getCurrentExpense());
        System.out.println("-----------------------------------------------------------------");
        System.out.printf("  Absolute Difference:               $%10.2f %s%n",
                trend.getAbsoluteChange(),
                trend.getAbsoluteChange() > 0 ? "(Increase)" : (trend.getAbsoluteChange() < 0 ? "(Decrease)" : ""));
        if (trend.getPercentageChange() != null) {
            System.out.printf("  Percentage Change:                 %10.2f%%%n", trend.getPercentageChange());
        }
        System.out.println();
        System.out.println("  Insight: " + trend.getSummaryDescription());
        System.out.println("-----------------------------------------------------------------");
    }

    private void printExitMessage() {
        System.out.println("Thank you for using Personal Finance Tracker. Goodbye!");
        AppLogger.info("Personal Finance Tracker CLI session terminated gracefully.");
    }

    private void printTransactionTable(List<Transaction> list) {
        System.out.println("+-----+------------+---------+-----------------+--------------+--------------------------------+");
        System.out.println("| ID  | Date       | Type    | Category        | Amount ($)   | Description                    |");
        System.out.println("+-----+------------+---------+-----------------+--------------+--------------------------------+");
        for (Transaction t : list) {
            String desc = t.getDescription();
            if (desc.length() > 30) {
                desc = desc.substring(0, 27) + "...";
            }
            System.out.printf("| %-3d | %s | %-7s | %-15s | $%11.2f | %-30s |%n",
                    t.getId(),
                    t.getDate().format(DATE_FMT),
                    t.getType().name(),
                    t.getCategory(),
                    t.getAmount(),
                    desc);
        }
        System.out.println("+-----+------------+---------+-----------------+--------------+--------------------------------+");
        System.out.printf("  Total records: %d%n", list.size());
    }
}
