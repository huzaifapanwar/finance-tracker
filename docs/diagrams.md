# System Architecture & Design Diagrams

This document contains architectural and behavioral design diagrams for the **Personal Finance Tracker** application, rendered using GitHub-compatible Mermaid syntax.

---

## 1. System Architecture Diagram
The application follows a clean, decoupled 4-tier layered architecture with cross-cutting utility concerns.

```mermaid
graph TD
    subgraph Presentation_Layer ["Presentation Layer"]
        CLI["Main CLI (Menu Loop)"]
    end

    subgraph Service_Layer ["Service Layer (Business Logic)"]
        TS["TransactionService"]
        BS["BudgetService"]
        RS["ReportService"]
    end

    subgraph Storage_Layer ["Storage Layer"]
        CSV["CsvStorage (Generic I/O)"]
    end

    subgraph Persistence ["Persistence (Local Filesystem)"]
        TX_CSV[("transactions.csv")]
        BG_CSV[("budgets.csv")]
    end

    subgraph Cross_Cutting ["Cross-Cutting Concerns"]
        VAL["InputValidator"]
        LOG["AppLogger -> app.log"]
    end

    CLI --> TS
    CLI --> BS
    CLI --> RS
    CLI --> VAL
    CLI --> LOG

    RS --> TS
    BS --> TS

    TS --> CSV
    BS --> CSV

    TS --> LOG
    BS --> LOG

    CSV --> TX_CSV
    CSV --> BG_CSV
```

---

## 2. Use Case Diagram
Demonstrates all primary functional capabilities accessible to the end user through the CLI.

```mermaid
flowchart LR
    User((User))

    subgraph Finance_Tracker ["Personal Finance Tracker CLI"]
        UC1["1. Add Transaction"]
        UC2["2. View All Transactions"]
        UC3["3. Edit Transaction"]
        UC4["4. Delete Transaction"]
        UC5["5. Set Category Budget"]
        UC6["6. View Budget Status"]
        UC7["7. View Monthly Summary"]
        UC8["8. View Category Breakdown"]
        UC9["9. View Spending Trend"]
        UC10["0. Exit Application"]
    end

    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC5
    User --> UC6
    User --> UC7
    User --> UC8
    User --> UC9
    User --> UC10
```

---

## 3. Class Diagram
Illustrates the models, services, storage layer, and utility relationships along with their core fields and operations.

```mermaid
classDiagram
    direction TB

    class Main {
        -TransactionService transactionService
        -BudgetService budgetService
        -ReportService reportService
        -Scanner scanner
        +main(args: String[]) void
        +run() void
        -handleAddTransaction() void
        -handleViewAllTransactions() void
        -handleEditTransaction() void
        -handleDeleteTransaction() void
        -handleSetCategoryBudget() void
        -handleViewBudgetStatus() void
        -handleMonthlySummary() void
        -handleCategoryBreakdown() void
        -handleMonthOverMonthTrend() void
    }

    class TransactionType {
        <<enumeration>>
        INCOME
        EXPENSE
        +getDisplayName() String
        +fromString(value: String)$ TransactionType
    }

    class Transaction {
        -int id
        -LocalDate date
        -TransactionType type
        -String category
        -double amount
        -String description
        +getId() int
        +getDate() LocalDate
        +getType() TransactionType
        +getCategory() String
        +getAmount() double
        +getDescription() String
        +toCsvRow() String
        +fromCsvRow(csvLine: String)$ Transaction
    }

    class Budget {
        -String category
        -double monthlyLimit
        +getCategory() String
        +getMonthlyLimit() double
        +setMonthlyLimit(limit: double) void
        +toCsvRow() String
        +fromCsvRow(csvLine: String)$ Budget
    }

    class CsvStorage {
        +readLines(filePath: String) List~String~
        +writeLines(filePath: String, lines: List~String~) void
        +appendLine(filePath: String, line: String) void
        +exists(filePath: String) boolean
        +deleteFile(filePath: String) boolean
    }

    class TransactionService {
        -String csvPath
        -CsvStorage storage
        -List~Transaction~ transactions
        -AtomicInteger nextId
        +addTransaction(date: LocalDate, type: TransactionType, cat: String, amt: double, desc: String) Transaction
        +editTransaction(id: int, date: LocalDate, type: TransactionType, cat: String, amt: double, desc: String) boolean
        +deleteTransaction(id: int) boolean
        +getAllTransactions() List~Transaction~
        +getTransactionById(id: int) Optional~Transaction~
        +getTransactionsByMonth(month: YearMonth) List~Transaction~
        +getTransactionsByCategory(category: String) List~Transaction~
        +getCount() int
    }

    class BudgetService {
        -String csvPath
        -CsvStorage storage
        -Map~String, Budget~ budgets
        +setBudget(category: String, limit: double) void
        +getAllBudgets() List~Budget~
        +getBudget(category: String) Optional~Budget~
        +getBudgetStatuses(month: YearMonth, txService: TransactionService) List~BudgetReportItem~
    }

    class ReportService {
        -TransactionService transactionService
        +getMonthlySummary(month: YearMonth) MonthlySummary
        +getCategoryWiseSpendBreakdown(month: YearMonth) List~CategorySpend~
        +getMonthOverMonthSpendingTrend(currentMonth: YearMonth) MonthOverMonthTrend
    }

    class InputValidator {
        +isValidDate(dateStr: String)$ boolean
        +isValidMonth(monthStr: String)$ boolean
        +isValidPositiveAmount(amt: double)$ boolean
        +isNonEmpty(input: String)$ boolean
        +readDate(scanner: Scanner, prompt: String)$ LocalDate
        +readMonth(scanner: Scanner, prompt: String)$ YearMonth
        +readPositiveAmount(scanner: Scanner, prompt: String)$ double
        +readTransactionType(scanner: Scanner, prompt: String)$ TransactionType
        +readInt(scanner: Scanner, prompt: String, min: int, max: int)$ int
    }

    class AppLogger {
        -Logger LOGGER$
        -FileHandler fileHandler$
        +info(msg: String)$ void
        +warning(msg: String)$ void
        +severe(msg: String, t: Throwable)$ void
        +logAdd(entity: String, details: Object)$ void
        +logEdit(entity: String, details: Object)$ void
        +logDelete(entity: String, details: Object)$ void
        +logError(ctx: String, t: Throwable)$ void
    }

    Main --> TransactionService
    Main --> BudgetService
    Main --> ReportService
    Main ..> InputValidator
    Main ..> AppLogger

    TransactionService --> CsvStorage
    TransactionService ..> Transaction
    Transaction ..> TransactionType

    BudgetService --> CsvStorage
    BudgetService ..> Budget
    BudgetService ..> TransactionService

    ReportService --> TransactionService
```

---

## 4. Sequence Diagram: Add Transaction & View Budget Check
Traces the execution flow when adding an expense and checking category budget thresholds.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Main as Main CLI
    participant Val as InputValidator
    participant TS as TransactionService
    participant BS as BudgetService
    participant CSV as CsvStorage
    participant Disk as Local Disk (CSV & Log)
    participant Log as AppLogger

    User->>Main: Select option 1 (Add Transaction)
    Main->>Val: Prompt & validate date, type, category, amount
    Val-->>Main: Validated input attributes
    Main->>TS: addTransaction(date, EXPENSE, "Groceries", 420.00, "Supermarket")
    TS->>TS: Auto-increment ID (e.g. #2)
    TS->>CSV: writeLines("data/transactions.csv", serializedRows)
    CSV->>Disk: Persist rows to data/transactions.csv
    TS->>Log: logAdd("Transaction", transaction)
    Log->>Disk: Write timestamped log entry to app.log
    TS-->>Main: Created Transaction (#2)
    Main-->>User: [OK] Success: Added EXPENSE transaction #2

    User->>Main: Select option 6 (View Budget Status)
    Main->>BS: getBudgetStatuses(2026-09, transactionService)
    BS->>TS: getTransactionsByMonth(2026-09)
    TS-->>BS: List of transactions in 2026-09
    BS->>BS: Sum expenses for "Groceries" ($420.00) vs. Limit ($500.00)
    BS->>BS: Ratio = 84.0% (>= 80% threshold -> WARNING)
    BS-->>Main: List containing BudgetReportItem(WARNING)
    Main-->>User: Render formatted table with status badge [ WARNING ]
```

---

## 5. Activity / Workflow Diagram: CLI Menu Loop
Visualizes user interaction flow, input validation re-prompt loops, execution branches, and exception protection.

```mermaid
flowchart TD
    Start([Launch CLI]) --> Init[Initialize Services & Setup Logger]
    Init --> DisplayMenu[Display Main Menu Options 0 - 9]
    DisplayMenu --> ReadChoice[/Read Option Selection/]

    ReadChoice --> ValidateChoice{Is Valid 0-9?}
    ValidateChoice -- No --> RepromptChoice[Display Error & Re-prompt] --> ReadChoice
    ValidateChoice -- Yes --> Dispatch{Selected Option}

    Dispatch -- 1 --> OptAdd[1. Add Transaction]
    Dispatch -- 2 --> OptView[2. View All Transactions]
    Dispatch -- 3 --> OptEdit[3. Edit Transaction]
    Dispatch -- 4 --> OptDel[4. Delete Transaction]
    Dispatch -- 5 --> OptBudget[5. Set Category Budget]
    Dispatch -- 6 --> OptBStatus[6. View Budget Status]
    Dispatch -- 7 --> OptSum[7. Monthly Summary]
    Dispatch -- 8 --> OptBreak[8. Category Spend Breakdown]
    Dispatch -- 9 --> OptTrend[9. Month-over-Month Trend]
    Dispatch -- 0 --> OptExit[0. Exit Application]

    subgraph OperationExecution ["Resilient Operation Execution"]
        OptAdd --> InputLoop[/Read Field with InputValidator/]
        InputLoop --> ValidField{Valid Input?}
        ValidField -- No --> ShowFieldError[Show specific field error] --> InputLoop
        ValidField -- Yes --> PersistWrite[Persist immediately to CSV & Log to app.log]
        PersistWrite --> ShowSuccess[Display formatted feedback]
    end

    ShowSuccess --> SafetyCheck{Exception Raised?}
    OptView --> SafetyCheck
    OptEdit --> SafetyCheck
    OptDel --> SafetyCheck
    OptBudget --> SafetyCheck
    OptBStatus --> SafetyCheck
    OptSum --> SafetyCheck
    OptBreak --> SafetyCheck
    OptTrend --> SafetyCheck

    SafetyCheck -- Yes --> CatchBlock[Top-Level Catch: Log error to app.log and inform user] --> DisplayMenu
    SafetyCheck -- No --> DisplayMenu

    OptExit --> Terminate([Log graceful shutdown & Exit])
```
