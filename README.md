# Personal Finance Tracker

A lightweight, zero-dependency personal finance management application with both a **Core Java SE 17+ CLI** and a **Browser Web Application (GitHub Pages)**.

The application tracks income and expenditures, monitors category budgets with early warning alerts, and delivers actionable financial analytics (monthly net balances, category spend proportions, and month-over-month trend changes). All financial records can be exported and imported as transparent, human-readable CSV files compatible across the Java CLI and the Web Application.

---

## Features

- **Income & Expense Tracking**: Full CRUD (Create, Read, Edit, Delete) operations with automatic unique transaction ID assignment.
- **Monthly Category Budgets**: Configure expense caps per category and receive automated status indicators:
  - `[ OK ]`: Spending is below 80% of limit.
  - `[ WARNING ]`: Spending is at or above 80% and within 100% of limit.
  - `[ OVER BUDGET ]`: Spending exceeds the configured limit.
- **Analytics & Reporting**:
  - Monthly financial summary (Total Income, Total Expenses, Net Surplus/Deficit).
  - Category-wise spending breakdown with percentages and ASCII visual proportion bars.
  - Month-over-month spending trend analysis with percentage variance comparison against the prior month.
- **Resilient CLI**: Centralized validation with infinite re-prompt loops (the application never crashes on invalid dates, numbers, or empty inputs) and top-level exception handling.
- **Audit Logging**: Timestamped operational history recorded to `app.log` via standard `java.util.logging`.
- **Zero External Dependencies**: Uses only standard Java SE library modules (no Maven, Gradle, or third-party JARs required).

---

## Project Structure

```
finance-tracker/
├── .gitignore                      # Excludes bin/, *.class, app.log, and data/*.csv
├── README.md                       # Project documentation & walkthrough
├── statement.md                    # Problem statement, scope & target users
├── data/
│   └── .gitkeep                    # Retains data directory for runtime CSV files
├── docs/
│   └── diagrams.md                 # Mermaid diagrams (Architecture, Use Case, Class, Sequence, Workflow)
└── src/
    └── com/
        └── financetracker/
            ├── Main.java           # CLI numbered menu loop entry point
            ├── AppTest.java        # Comprehensive dependency-free manual test runner
            ├── model/
            │   ├── TransactionType.java # INCOME / EXPENSE enum
            │   ├── Transaction.java     # Transaction record with CSV serialization
            │   └── Budget.java          # Budget limit model with CSV serialization
            ├── service/
            │   ├── TransactionService.java # Transaction CRUD, auto-increment IDs & persistence
            │   ├── BudgetService.java      # Budget limits & threshold evaluations
            │   └── ReportService.java      # Income/expense analytics & trend calculations
            ├── storage/
            │   └── CsvStorage.java         # Thread-safe generic CSV reader/writer/appender
            └── util/
                ├── AppLogger.java          # FileHandler logging to app.log
                └── InputValidator.java     # Strict validation and resilient CLI scanners
```

---

## Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher.
  Check your installed version with:
  ```bash
  java -version
  javac -version
  ```

---

## Compilation

From the root directory of the project, compile all Java source files into the `bin` directory:

### Windows (PowerShell / Command Prompt)
```powershell
javac -d bin src/com/financetracker/*.java src/com/financetracker/model/*.java src/com/financetracker/service/*.java src/com/financetracker/storage/*.java src/com/financetracker/util/*.java
```

### Linux / macOS (Bash / Zsh)
```bash
javac -d bin $(find src -name "*.java")
```

---

## Running the Application

After compiling, launch the interactive command-line interface:

```bash
java -cp bin com.financetracker.Main
```

---

## Running the Automated Test Suite

The project includes an automated test runner (`AppTest`) that runs 50+ manual assertions with zero dependencies (no JUnit needed) and prints clear `[PASS]` / `[FAIL]` lines:

```bash
java -cp bin com.financetracker.AppTest
```

Sample output:
```
===============================================================
               FINANCE TRACKER AUTOMATED TESTS                 
===============================================================

-- Testing Transaction Model & CSV Roundtrip --
  [PASS] Transaction ID preserves in CSV
  [PASS] Transaction Date preserves in CSV
  ...
===============================================================
Test Execution Summary: Total: 51 | Passed: 51 | Failed: 0
===============================================================
ALL TESTS PASSED SUCCESSFULLY [OK]
```

---

## Sample Usage Walkthrough

Below is an end-to-end walkthrough demonstrating typical CLI operations:

### Step 1: Add Transactions (Menu Option 1)
```
Enter your choice [0-9]: 1
--- Add New Transaction ---
Date (yyyy-MM-dd) [2026-09-07]: 2026-09-01
Type (INCOME or EXPENSE): INCOME
Category (e.g. Salary, Food, Utilities): Salary
Amount ($): 5000.00
Description (optional): Monthly Salary
  [OK] Success: Added INCOME transaction #1 for $5000.00 under 'Salary'.
```
Add an expense:
```
Enter your choice [0-9]: 1
--- Add New Transaction ---
Date (yyyy-MM-dd) [2026-09-07]: 2026-09-05
Type (INCOME or EXPENSE): EXPENSE
Category (e.g. Salary, Food, Utilities): Groceries
Amount ($): 420.00
Description (optional): Supermarket groceries
  [OK] Success: Added EXPENSE transaction #2 for $420.00 under 'Groceries'.
```

### Step 2: Set a Category Budget (Menu Option 5)
```
Enter your choice [0-9]: 5
--- Set Category Monthly Budget ---
Category name (e.g. Groceries, Rent, Dining): Groceries
Monthly budget limit ($): 500.00
  [OK] Success: Monthly budget for 'Groceries' set to $500.00.
```

### Step 3: View Budget Status (Menu Option 6)
```
Enter your choice [0-9]: 6
--- View Monthly Budget Status ---
Enter month (yyyy-MM) [2026-09]: 2026-09

Budget Performance Report for 2026-09:
+-----------------+--------------+--------------+--------------+---------+---------------+
| Category        | Monthly Limit| Actual Spend | Remaining    | % Used  | Status        |
+-----------------+--------------+--------------+--------------+---------+---------------+
| Groceries       | $     500.00 | $     420.00 | $      80.00 |   84.0% | [ WARNING ]   |
+-----------------+--------------+--------------+--------------+---------+---------------+
```
*(Because $420.00 is 84% of the $500.00 limit, the status dynamically displays `[ WARNING ]`)*

### Step 4: Monthly Financial Summary (Menu Option 7)
```
Enter your choice [0-9]: 7
--- Monthly Income / Expense Summary ---
Enter month (yyyy-MM) [2026-09]: 2026-09

Financial Summary for 2026-09 (2 transactions):
--------------------------------------------------
  Total Income:   $     5000.00
  Total Expenses: $      420.00
--------------------------------------------------
  Net Balance:    $     4580.00 (Surplus)
--------------------------------------------------
```

### Step 5: Category-wise Spend Breakdown (Menu Option 8)
```
Enter your choice [0-9]: 8
--- Category-wise Spend Breakdown ---
Enter month (yyyy-MM) [2026-09]: 2026-09

Expense Breakdown for 2026-09 (Total Expenses: $420.00):
+----------------------+---------------+------------+------------------------------+
| Category             | Amount ($)    | Share (%)  | Visual Proportion            |
+----------------------+---------------+------------+------------------------------+
| Groceries            | $      420.00 |     100.0% | #########################    |
+----------------------+---------------+------------+------------------------------+
```

### Step 6: Month-over-Month Spending Trend (Menu Option 9)
```
Enter your choice [0-9]: 9
--- Month-over-Month Spending Trend ---
Enter target month (yyyy-MM) [2026-09]: 2026-09

Spending Trend Comparison: 2026-09 vs. 2026-08
-----------------------------------------------------------------
  Previous Month (2026-08) Spending: $      0.00
  Current Month  (2026-09) Spending: $    420.00
-----------------------------------------------------------------
  Absolute Difference:               $    420.00 (Increase)

  Insight: Spending in 2026-09 is $420.00 (no expenses recorded in 2026-08 for baseline comparison).
-----------------------------------------------------------------
```

### Step 7: View All Transactions (Menu Option 2)
```
Enter your choice [0-9]: 2
--- All Recorded Transactions ---
+-----+------------+---------+-----------------+--------------+--------------------------------+
| ID  | Date       | Type    | Category        | Amount ($)   | Description                    |
+-----+------------+---------+-----------------+--------------+--------------------------------+
| 2   | 2026-09-05 | EXPENSE | Groceries       | $     420.00 | Supermarket groceries          |
| 1   | 2026-09-01 | INCOME  | Salary          | $    5000.00 | Monthly Salary                 |
+-----+------------+---------+-----------------+--------------+--------------------------------+
  Total records: 2
```

---

## Data Persistence & Logging

- **`data/transactions.csv`**: Contains persistent records in CSV format (`id,date,type,category,amount,description`).
- **`data/budgets.csv`**: Contains category monthly limits (`category,monthlyLimit`).
- **`app.log`**: Records timestamped audit logs for every transaction added, edited, deleted, budget changed, and any runtime exceptions.

---

## Architecture Diagrams

Detailed architectural diagrams (System Architecture, Use Case, Class, Sequence, and Workflow) are available in [docs/diagrams.md](docs/diagrams.md).

---

## License

This project is open-source software licensed under the MIT License.
