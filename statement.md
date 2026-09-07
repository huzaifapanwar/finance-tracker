# Personal Finance Tracker — Statement of Purpose & Scope

## 1. Problem Statement
Managing individual income, expenditures, and budgetary adherence often proves difficult without dedicated tools. Commercial personal finance software is frequently bloated, requires remote cloud accounts, tracks personal data, or relies on complex database engines that require extensive runtime configuration.

Users require a lightweight, transparent, secure, and zero-dependency solution that runs completely offline on any standard Java environment, persists data locally in human-readable and portable CSV files, and provides clear insights into monthly spending trends and budget compliance without friction.

---

## 2. Scope
The **Personal Finance Tracker** is an interactive, menu-driven command-line interface (CLI) application built using standard core Java SE 17+. It provides comprehensive transaction management, category budget enforcement, and analytical reporting while storing all persistent state in local CSV files.

### In Scope
- Full CRUD operations for income and expense transactions.
- Automatic transaction ID assignment and strict format validation.
- Per-category monthly budget setting and threshold alerts (`OK`, `WARNING` at $\ge 80\%$, and `OVER BUDGET` at $> 100\%$).
- Monthly financial summaries (income, expense, and surplus/deficit balance).
- Category-level expense distribution analysis with percentage shares and ASCII bar charts.
- Month-over-month expenditure trend analysis with percentage variance calculation.
- Resilient input handling with automated re-prompting (zero unexpected crashes).
- Top-level exception safety net and file-based audit logging (`app.log`).
- Automated, dependency-free test suite (`AppTest`) with pass/fail reporting.

### Out of Scope
- Multi-currency conversions or live banking API syncing.
- Multi-tenant cloud databases or remote web server hosting.
- Graphical user interfaces (GUI) or browser frontends.

---

## 3. Target Users
- **Developers & Students**: Individuals looking for a clean, modular, zero-dependency Java application showcasing clean architecture and standard library capabilities.
- **Privacy-Conscious Individuals**: Users seeking 100% offline personal finance management where financial records remain solely on their local storage in transparent CSV files.
- **Budget Trackers**: Individuals wanting clear, immediate feedback on whether their category spending is approaching or exceeding target limits.

---

## 4. High-Level Features

| Feature ID | Name | Description |
| :--- | :--- | :--- |
| **F-01** | **Add Transaction** | Records a date, type (`INCOME` / `EXPENSE`), category, positive amount, and optional description. Auto-assigns sequential ID and immediately updates CSV storage. |
| **F-02** | **View Transactions** | Formats all recorded transactions in a sorted tabular view displaying ID, date, type, category, formatted amount, and description. |
| **F-03** | **Edit Transaction** | Selects an existing transaction by ID and allows updating individual fields while preserving unedited values. |
| **F-04** | **Delete Transaction** | Locates transaction by ID, requests user confirmation, removes it from memory and disk, and updates CSV persistence. |
| **F-05** | **Set Category Budget** | Assigns or updates a monthly expense cap for any category. |
| **F-06** | **Budget Status Monitor** | Compares actual monthly expenses against configured category limits, computing percentage utilized and assigning `OK`, `WARNING` ($\ge 80\%$), or `OVER BUDGET` ($> 100\%$) status badges. |
| **F-07** | **Monthly Summary** | Aggregates income and expenses for any calendar month (`yyyy-MM`), presenting net savings/deficit and transaction counts. |
| **F-08** | **Category Spend Breakdown** | Calculates proportional expense breakdown by category with calculated percentages and terminal-friendly visual proportion bars. |
| **F-09** | **Spending Trend Analysis** | Calculates month-over-month expenditure changes, computing absolute and percentage variance against the preceding calendar month. |
| **F-10** | **Audit Logging & Safety** | Records all mutations and unexpected runtime errors to `app.log` with timestamped records, protecting user sessions with top-level error recovery. |
