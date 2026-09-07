package com.financetracker;

import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.service.BudgetService;
import com.financetracker.service.ReportService;
import com.financetracker.service.TransactionService;
import com.financetracker.storage.CsvStorage;
import com.financetracker.util.InputValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Dependency-free test suite executing manual assertions across models, services, storage, and validation.
 * No JUnit or external testing frameworks required.
 */
public class AppTest {
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    private static final String TEST_TX_CSV = "data/test_transactions.csv";
    private static final String TEST_BUDGET_CSV = "data/test_budgets.csv";

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("               FINANCE TRACKER AUTOMATED TESTS                 ");
        System.out.println("===============================================================");

        cleanupTestFiles();

        try {
            testTransactionModelCsv();
            testBudgetModelCsv();
            testInputValidator();
            testTransactionServiceCrud();
            testBudgetServiceThresholds();
            testReportServiceAnalytics();
            testMonthOverMonthTrend();
        } finally {
            cleanupTestFiles();
        }

        System.out.println("===============================================================");
        System.out.printf("Test Execution Summary: Total: %d | Passed: %d | Failed: %d%n",
                totalTests, passedTests, failedTests);
        System.out.println("===============================================================");

        if (failedTests > 0) {
            System.err.println("TEST SUITE FAILED!");
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY [OK]");
            System.exit(0);
        }
    }

    private static void cleanupTestFiles() {
        try {
            Files.deleteIfExists(Paths.get(TEST_TX_CSV));
            Files.deleteIfExists(Paths.get(TEST_BUDGET_CSV));
        } catch (IOException ignored) {
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            failedTests++;
            System.err.println("  [FAIL] " + testName + " -> " + message);
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        totalTests++;
        if (expected == null && actual == null) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else if (expected != null && expected.equals(actual)) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            failedTests++;
            System.err.printf("  [FAIL] %s -> Expected [%s] but received [%s]%n", testName, expected, actual);
        }
    }

    private static void assertDoubleEquals(String testName, double expected, double actual, double delta) {
        totalTests++;
        if (Math.abs(expected - actual) <= delta) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            failedTests++;
            System.err.printf("  [FAIL] %s -> Expected [%.4f] but received [%.4f]%n", testName, expected, actual);
        }
    }

    private static void testTransactionModelCsv() {
        System.out.println("\n-- Testing Transaction Model & CSV Roundtrip --");
        LocalDate date = LocalDate.of(2026, 9, 15);
        Transaction tx = new Transaction(10, date, TransactionType.EXPENSE, "Groceries", 124.50, "Weekly groceries, milk & eggs");
        String csvRow = tx.toCsvRow();

        Transaction parsed = Transaction.fromCsvRow(csvRow);
        assertEquals("Transaction ID preserves in CSV", 10, parsed.getId());
        assertEquals("Transaction Date preserves in CSV", date, parsed.getDate());
        assertEquals("Transaction Type preserves in CSV", TransactionType.EXPENSE, parsed.getType());
        assertEquals("Transaction Category preserves in CSV", "Groceries", parsed.getCategory());
        assertDoubleEquals("Transaction Amount preserves in CSV", 124.50, parsed.getAmount(), 0.001);
        assertEquals("Transaction Description with comma preserves in CSV", "Weekly groceries, milk & eggs", parsed.getDescription());
    }

    private static void testBudgetModelCsv() {
        System.out.println("\n-- Testing Budget Model & CSV Roundtrip --");
        Budget b = new Budget("Dining Out", 350.00);
        String csvRow = b.toCsvRow();

        Budget parsed = Budget.fromCsvRow(csvRow);
        assertEquals("Budget Category preserves in CSV", "Dining Out", parsed.getCategory());
        assertDoubleEquals("Budget Limit preserves in CSV", 350.00, parsed.getMonthlyLimit(), 0.001);
    }

    private static void testInputValidator() {
        System.out.println("\n-- Testing InputValidator --");
        assertTrue("Valid date format yyyy-MM-dd", InputValidator.isValidDate("2026-09-07"), "Expected true for 2026-09-07");
        assertTrue("Invalid date format", !InputValidator.isValidDate("07-09-2026"), "Expected false for 07-09-2026");
        assertTrue("Invalid leap year date", !InputValidator.isValidDate("2025-02-29"), "Expected false for 2025-02-29");

        assertTrue("Valid month format yyyy-MM", InputValidator.isValidMonth("2026-09"), "Expected true for 2026-09");
        assertTrue("Invalid month format", !InputValidator.isValidMonth("2026-13"), "Expected false for 2026-13");

        assertTrue("Valid positive amount", InputValidator.isValidPositiveAmount(45.50), "Expected true for 45.50");
        assertTrue("Zero amount is rejected", !InputValidator.isValidPositiveAmount(0.0), "Expected false for 0.0");
        assertTrue("Negative amount is rejected", !InputValidator.isValidPositiveAmount(-10.0), "Expected false for -10.0");

        assertTrue("Non-empty string is valid", InputValidator.isNonEmpty("Salary"), "Expected true for Salary");
        assertTrue("Whitespace-only string is rejected", !InputValidator.isNonEmpty("   "), "Expected false for spaces");
    }

    private static void testTransactionServiceCrud() {
        System.out.println("\n-- Testing TransactionService CRUD & Auto-Increment --");
        CsvStorage storage = new CsvStorage();
        TransactionService service = new TransactionService(TEST_TX_CSV, storage);

        Transaction t1 = service.addTransaction(LocalDate.of(2026, 8, 1), TransactionType.INCOME, "Salary", 4000.0, "Monthly Salary");
        Transaction t2 = service.addTransaction(LocalDate.of(2026, 8, 5), TransactionType.EXPENSE, "Rent", 1200.0, "Apartment rent");
        Transaction t3 = service.addTransaction(LocalDate.of(2026, 9, 1), TransactionType.INCOME, "Salary", 4200.0, "Promotion salary");
        Transaction t4 = service.addTransaction(LocalDate.of(2026, 9, 3), TransactionType.EXPENSE, "Food", 300.0, "Supermarket");

        assertEquals("Auto-increment ID for first item", 1, t1.getId());
        assertEquals("Auto-increment ID for second item", 2, t2.getId());
        assertEquals("Auto-increment ID for third item", 3, t3.getId());
        assertEquals("Total transaction count is 4", 4, service.getCount());

        // Edit transaction 2
        boolean edited = service.editTransaction(2, LocalDate.of(2026, 8, 5), TransactionType.EXPENSE, "Rent", 1250.0, "Updated rent");
        assertTrue("Edit transaction returns true", edited, "Expected edit to succeed");
        Optional<Transaction> editedTx = service.getTransactionById(2);
        assertTrue("Edited transaction found", editedTx.isPresent(), "Expected tx to exist");
        assertDoubleEquals("Updated amount is reflected", 1250.0, editedTx.get().getAmount(), 0.001);

        // Filter by month
        List<Transaction> augustTx = service.getTransactionsByMonth(YearMonth.of(2026, 8));
        assertEquals("August transactions count", 2, augustTx.size());

        List<Transaction> septemberTx = service.getTransactionsByMonth(YearMonth.of(2026, 9));
        assertEquals("September transactions count", 2, septemberTx.size());

        // Filter by category
        List<Transaction> salaryTx = service.getTransactionsByCategory("salary");
        assertEquals("Salary transactions count across months", 2, salaryTx.size());

        // Delete transaction 1
        boolean deleted = service.deleteTransaction(1);
        assertTrue("Delete transaction 1 succeeded", deleted, "Expected delete to return true");
        assertEquals("Transaction count after delete", 3, service.getCount());

        // Re-load service from file to verify persistence
        TransactionService reloaded = new TransactionService(TEST_TX_CSV, storage);
        assertEquals("Reloaded service has matching transaction count", 3, reloaded.getCount());
        assertTrue("Deleted transaction is not in reloaded service", reloaded.getTransactionById(1).isEmpty(), "Tx 1 should be gone");
        assertTrue("Remaining transaction 2 exists in reloaded service", reloaded.getTransactionById(2).isPresent(), "Tx 2 should exist");

        // Next added transaction should have id 5 (max was 4)
        Transaction t5 = reloaded.addTransaction(LocalDate.of(2026, 9, 10), TransactionType.EXPENSE, "Utilities", 150.0, "Electricity");
        assertEquals("New transaction receives id 5 after re-open", 5, t5.getId());
    }

    private static void testBudgetServiceThresholds() {
        System.out.println("\n-- Testing BudgetService Thresholds (OK, WARNING, OVER BUDGET) --");
        CsvStorage storage = new CsvStorage();
        BudgetService budgetService = new BudgetService(TEST_BUDGET_CSV, storage);
        TransactionService txService = new TransactionService(TEST_TX_CSV, storage);

        // Configure budgets
        // Rent: limit 1000, spend 0 in Sept -> OK
        // Food: limit 350, spend 300 in Sept (85.7% >= 80%) -> WARNING
        // Utilities: limit 100, spend 150 in Sept (150% > 100%) -> OVER BUDGET
        budgetService.setBudget("Rent", 1000.0);
        budgetService.setBudget("Food", 350.0);
        budgetService.setBudget("Utilities", 100.0);

        List<BudgetService.BudgetReportItem> report = budgetService.getBudgetStatuses(YearMonth.of(2026, 9), txService);
        assertEquals("Report contains 3 budget items", 3, report.size());

        BudgetService.BudgetReportItem rentReport = report.stream().filter(r -> r.getCategory().equalsIgnoreCase("Rent")).findFirst().orElseThrow();
        assertEquals("Rent status is OK", BudgetService.Status.OK, rentReport.getStatus());

        BudgetService.BudgetReportItem foodReport = report.stream().filter(r -> r.getCategory().equalsIgnoreCase("Food")).findFirst().orElseThrow();
        assertEquals("Food status (85.7%) is WARNING", BudgetService.Status.WARNING, foodReport.getStatus());

        BudgetService.BudgetReportItem utilReport = report.stream().filter(r -> r.getCategory().equalsIgnoreCase("Utilities")).findFirst().orElseThrow();
        assertEquals("Utilities status (150%) is OVER_BUDGET", BudgetService.Status.OVER_BUDGET, utilReport.getStatus());
    }

    private static void testReportServiceAnalytics() {
        System.out.println("\n-- Testing ReportService Summaries & Breakdown --");
        CsvStorage storage = new CsvStorage();
        TransactionService txService = new TransactionService(TEST_TX_CSV, storage);
        ReportService reportService = new ReportService(txService);

        // In Sept 2026:
        // Income: 4200.00 (Salary)
        // Expenses: 300.00 (Food) + 150.00 (Utilities) = 450.00
        // Net balance: 4200 - 450 = 3750.00
        ReportService.MonthlySummary summary = reportService.getMonthlySummary(YearMonth.of(2026, 9));
        assertDoubleEquals("Sept Total Income", 4200.0, summary.getTotalIncome(), 0.001);
        assertDoubleEquals("Sept Total Expense", 450.0, summary.getTotalExpense(), 0.001);
        assertDoubleEquals("Sept Net Balance", 3750.0, summary.getNetBalance(), 0.001);

        // Breakdown: Food 300/450 = 66.67%, Utilities 150/450 = 33.33%
        List<ReportService.CategorySpend> breakdown = reportService.getCategoryWiseSpendBreakdown(YearMonth.of(2026, 9));
        assertEquals("Breakdown contains 2 expense categories", 2, breakdown.size());
        assertEquals("Top expense category is Food", "Food", breakdown.get(0).getCategory());
        assertDoubleEquals("Food percentage is 66.6667%", 66.6667, breakdown.get(0).getPercentage(), 0.1);
        assertEquals("Second expense category is Utilities", "Utilities", breakdown.get(1).getCategory());
        assertDoubleEquals("Utilities percentage is 33.3333%", 33.3333, breakdown.get(1).getPercentage(), 0.1);
    }

    private static void testMonthOverMonthTrend() {
        System.out.println("\n-- Testing Month-over-Month Trend Calculation --");
        CsvStorage storage = new CsvStorage();
        TransactionService txService = new TransactionService(TEST_TX_CSV, storage);
        ReportService reportService = new ReportService(txService);

        // Aug 2026: Rent was updated to 1250.00
        // Sept 2026: Food 300 + Utilities 150 = 450.00
        // Change: 450 - 1250 = -800.00
        // Percent change: (-800 / 1250) * 100 = -64.0%
        ReportService.MonthOverMonthTrend trend = reportService.getMonthOverMonthSpendingTrend(YearMonth.of(2026, 9));
        assertDoubleEquals("Aug (previous) expense", 1250.0, trend.getPreviousExpense(), 0.001);
        assertDoubleEquals("Sept (current) expense", 450.0, trend.getCurrentExpense(), 0.001);
        assertDoubleEquals("Absolute change", -800.0, trend.getAbsoluteChange(), 0.001);
        assertTrue("Percentage change is non-null", trend.getPercentageChange() != null, "Expected non-null percentage");
        assertDoubleEquals("Percentage change is -64%", -64.0, trend.getPercentageChange(), 0.001);
    }
}
