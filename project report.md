## P ERSONAL FINANCE TRACKER

## P roject Report

T echnology: Java 17+

A pplication Type: Command-Line Application

D ata Storage: Local CSV Files

## 1 . Introduction

P ersonal financial management is an important part of everyday life. Users need to keep track o f their income, expenses, budgets, and spending patterns to understand their financial position a nd make better decisions.

T he Personal Finance Tracker is a Java-based command-line application developed to p rovide a simple and lightweight solution for managing personal finances. The application a llows users to record income and expenses, edit or delete transactions, set category-wise m onthly budgets, and generate financial reports.

T he application uses Java 17+ and stores data in local CSV files instead of a database. This m akes the system portable and easy to run because it does not require a database server, e xternal libraries, Maven, or Gradle.

T he project follows a layered architecture consisting of model, service, storage, and utility c omponents. This improves maintainability and keeps different responsibilities separated.

## 2 . Problem Statement

M anaging personal finances manually can become difficult as the number of transactions i ncreases. Users may find it difficult to remember their expenses, calculate monthly spending, m onitor category budgets, and compare spending between different months.

T raditional finance applications may also require databases, accounts, internet connectivity, or c omplicated setup.


T he objective of this project is to develop a simple command-line Personal Finance Tracker t hat allows users to:

- Record income and expenses.

- View all financial transactions.

- Edit and delete existing transactions.

- Set monthly budgets for different categories.

- Check spending against a budget.

- Generate monthly financial summaries.

- Analyze category-wise spending.

- Compare spending between months.

- Store data locally using CSV files.

## 3 . Functional Requirements

T he project guidelines require at least three major functional modules, clear input/output, and a l ogical workflow.

T he Personal Finance Tracker provides the following functional requirements.

## 3 .1 Transaction Management

T he application allows users to:

- Add a new transaction.

- View all transactions.

- Edit an existing transaction.

- Delete a transaction.

- Retrieve transactions by month.

- Retrieve transactions by category.

E ach transaction contains:

- Transaction ID

- Date

- Transaction type

- Category

- Amount

- Description

T ransaction types are:


- INCOME

- EXPENSE

## 3 .2 Budget Management

U sers can set a monthly spending limit for a category.

T he system compares actual spending with the defined budget and returns one of three s tatuses:

- OK – spending is below 80% of the budget.

- WARNING – spending is at least 80% of the budget.

- OVER BUDGET – spending exceeds the budget.

## 3 .3 Financial Reporting

T he application provides:

- Total income.

- Total expenses.

- Net balance.

- Category-wise spending percentages.

- Month-over-month spending trend.

## 3 .4 Input Validation

T he application validates:

- Dates in yyyy-MM-dd format.

- Months in yyyy-MM format.

- Positive transaction amounts.

- Non-empty strings.

I nvalid input does not terminate the application. Instead, the user is prompted to enter the value a gain.

## 3 .5 Data Persistence

T ransaction and budget information is stored in CSV files.

T he application immediately persists changes after write operations.

## 3 .6 Logging


T he application uses Java's built-in java.util.logging package to record important events s uch as:

- Adding transactions.

- Editing transactions.

- Deleting transactions.

- Errors.

T he log is stored in app.log .

## 4 . Non-functional Requirements

T he VITyarthi guidelines require at least four non-functional requirements.

## 4 .1 Usability

T he application provides a simple numbered command-line menu. Users can select operations u sing numbers and receive clear prompts.

## 4 .2 Reliability

T he application includes exception handling and a top-level safety net in the Main class to p revent unexpected termination.

## 4 .3 Maintainability

T he project uses separate packages for models, services, storage, and utilities. This makes the c ode easier to understand, modify, and extend.

## 4 .4 Portability

T he project uses only standard Java 17+ features and has no external dependencies.

## 4 .5 Error Handling

I nvalid user input is detected through the centralized InputValidator class. The application r e-prompts the user instead of crashing.

## 4 .6 Resource Efficiency


T he application uses local CSV files instead of requiring a database server or other i nfrastructure.

## 4 .7 Logging

I mportant application events and errors are recorded in app.log using the Java standard l ogging framework.

## 5 . System Architecture

## A rchitecture Components

R esponsible for the command-line interface, menu loop, user input, and interaction with s ervices.

C ontains classes representing application data.

C ontains the application's business logic.

P rovides reusable CSV reading, writing, and appending functionality.

P rovides centralized validation and logging functionality.

T he layered approach supports modular and maintainable implementation, which is one of the t echnical expectations in the supplied guidelines.

## M ain:

## M odel Layer:

## S ervice Layer:

## S torage Layer:

## U tility Layer:

## M ain Use Cases

T he user can:

- 1 . Add a transaction.

- 2 . View transactions.

- 3 . Edit a transaction.

- 4 . Delete a transaction.

- 5 . Set a budget.


- 6 . View budget status.

- 7 . View monthly summary.

- 8 . View category breakdown.

- 9 . View spending trend.

## 6 . Storage Design

T he application uses CSV files rather than a relational database.

T he supplied guidelines mention an ER diagram when database/storage design is applicable. S ince this project uses CSV storage, the file schema is used to represent the storage design.

## t ransactions.csv

i d,date,type,category,amount,description

E xample:

1 ,2026-09-01,INCOME,Salary,50000,Monthly salary 2 ,2026-09-02,EXPENSE,Food,500,Lunch

## b udgets.csv

c ategory,monthlyLimit

E xample:

F ood,5000 T ransport,3000 E ntertainment,2000

## 7 . Design Decisions and Rationale

## J ava 17+

J ava 17+ was selected because it provides a stable modern Java environment while keeping t he application portable.


## C SV Instead of Database

C SV was selected because the project requires zero setup. Users do not need to install or c onfigure a database.

C SV files are also:

- Easy to read.

- Easy to back up.

- Lightweight.

- Portable.

## L ayered Architecture

T he project separates the application into model, service, storage, and utility layers. This p revents all logic from being placed inside the main CLI class.

## M anual Testing

A ppTest uses manual assertions rather than JUnit. This satisfies the dependency-free r equirement while still providing a simple testing mechanism.

## S tandard Java Logging

j ava.util.logging was selected instead of an external logging library to maintain the z ero-dependency requirement.

## I mmediate Persistence

C hanges are written to CSV immediately after modification so that important data is not kept o nly in memory.

## 1 8. Implementation Details

## 8 .1 Transaction

T he Transaction class represents a financial transaction.

I t contains:


- id

- date

- type

- category

- amount

- description

T he class provides toCsvRow() and fromCsvRow() methods for CSV serialization.

## 8 .2 TransactionType

T ransactionType is an enum containing:

I NCOME

E XPENSE

T his prevents invalid transaction types from being introduced.

## 8 .3 Budget

T he Budget class contains:

- Category.

- Monthly spending limit.

I t also provides CSV conversion methods.

## 8 .4 TransactionService

T he TransactionService manages transaction data and provides:

- Add.

- Edit.

- Delete.

- Get all.

- Get by month.

- Get by category.

T ransaction IDs are automatically incremented.

## 8 .5 BudgetService


T he BudgetService manages category budgets and determines the budget status.

T he status rules are:

S pending < 80% → OK

S pending >= 80% → WARNING

S pending > 100% → OVER BUDGET

## 8 .6 ReportService

T he reporting service calculates:

- Total income.

- Total expenses.

- Net balance.

- Category-wise spending percentages.

- Month-over-month spending trend.

## 8 .7 CsvStorage

C svStorage is a reusable component responsible for reading, writing, and appending CSV d ata.

B oth transaction and budget services use this component.

## 8 .8 InputValidator

T he validator ensures that user input follows the required format.

E xamples include:

D ate: yyyy-MM-dd

M onth: yyyy-MM

A mount: Positive number

S tring: Non-empty

## 8 .9 AppLogger

A ppLogger configures Java's standard logging framework and writes application events to:

a pp.log


## 9 . Screenshots / Results


```
MAIN MENU
. Add Transaction
. View All Transactions
. Edit a Transaction
. Delete a Transaction
. Set a Category Budget
. View Budget Status for a Month
. Monthly Income / Expense Summary
. Category-wise Spend Breakdown
. Month-over-Month Spending Trend
. Exit
Enter your choice [0-9]: 4
--- Delete Transaction -—-—
Enter Transaction ID to delete: 1
Target transaction: Transaction #1 | 2026-04-05 | INCOME | 500
| $1000.00 |
Are you sure you want to delete this transaction? (y/N): y
[OK] Success: Transaction #1 deleted.
. Add Transaction
. View All Transactions
. Edit a Transaction
. Delete a Transaction
. Set a Category Budget
. View Budget Status for a Month
. Monthly Income / Expense Summary
. Category-wise Spend Breakdown
. Month-over-Month Spending Trend
. Exit
Enter your choice [0-9]: 5
——- Set Category Monthly Budget -—-
Category name (e.g. Groceries, Rent, Dining): household
Monthly budget limit ($): 300
[OK] Success: Monthly budget for 'household' set to $300.00.
```


```
Enter your choice [0-9]: 7
——— Monthly Income / Expense Summary ———
Enter month (yyyy-MM) [2026-09]: 2026-10
Financial Summary for 2026-10 (© transactions):
Total Income: $ 0.00
Total Expenses: $ 0.00
Net Balance: $ 0.00 (Surplus)
Enter your choice [0-9]: 8
Category-wise Spend Breakdown -—-
Enter month (yyyy-MM) [2026-09]: 2026-04
No expense transactions recorded for 2026-04.
Enter your choice [0-9]: 9
Month-over-Month Spending Trend -—-
Enter target month (yyyy-MM) [2026-09]: 2026-04
Spending Trend Comparison: 2026-04 vs. 2026-03
Previous Month (2026-03) Spending: $ 0.00
Current Month (2026-64) Spending: $ 0.00
Absolute Difference: $ 0.00
Percentage Change: 0.00%
Insight: No spending recorded in either 2026-03 or 2026-04.
```


## 1 0. Testing Approach

T he project uses AppTest , a dependency-free test class containing manual assertions. The V ITyarthi guidelines specifically require testing wherever applicable.

T he following areas should be tested:

| T est | E xpected Result |
| --- | --- |
|   | T ransaction CSV serialization Data can be converted to and from CSV |
| B udget CSV serialization | B udget information is preserved |
| D ate validation | I nvalid dates are rejected |
| M onth validation | I nvalid months are rejected |
| A mount validation | Z ero/negative amounts are rejected |
| S tring validation | E mpty strings are rejected |
| A dd transaction | T ransaction is stored successfully |
| E dit transaction | E xisting transaction is updated |
| D elete transaction | T ransaction is removed |
| B udget status | C orrect status is returned |
| M onthly summary | I ncome/expense totals are correct |
| C ategory breakdown | P ercentages are calculated correctly |
| S pending trend | M onth-over-month percentage is |
|   | c alculated |
| C LI flow | C omplete workflow runs without crashing |

T he application should also be tested using the required end-to-end sequence:

- 1 . Start the application.

- 2 . Add transaction 1.

- 3 . Add transaction 2.

- 4 . Set a category budget.

- 5 . View monthly summary.

- 6 . View category breakdown.

- 7 . View spending trend.


## 1 1. Challenges Faced

S everal challenges were encountered during the development of the project.

## C SV Data Management

M anaging structured data using CSV files required careful handling of reading, writing, a ppending, and converting Java objects into CSV rows.

## A utomatic ID Generation

T he application needs to maintain unique transaction IDs while supporting additions and d eletions.

## I nput Validation

C ommand-line applications receive input directly from users, so invalid values need to be h andled without terminating the program.

## B udget Calculations

T he application needs to correctly determine whether spending is within the budget, a pproaching the limit, or exceeding it.

## R eporting

C alculating category percentages and month-over-month spending requires processing t ransaction data accurately.

## S eparation of Responsibilities

A nother challenge was maintaining clean separation between CLI interaction, business logic, s torage, validation, and logging.

## 1 2. Learnings and Key Takeaways


M ajor learnings include:

T his project provided practical experience in Java application development and software a rchitecture.

- Applying object-oriented programming to a real-world problem.

- Designing a layered software architecture.

- Working with Java collections and file handling.

- Implementing CSV-based persistence.

- Creating reusable service and storage components.

- Implementing CRUD operations.

- Designing validation and error-handling mechanisms.

- Using Java's standard logging framework.

- Writing dependency-free manual tests.

- Understanding separation of concerns.

- Organizing a project using packages.

- Using Git and GitHub for version control and project sharing.

## 1 3. Future Enhancements

T he current application can be extended with several features.

## G raphical User Interface

A GUI could make the application more accessible to users who prefer visual interfaces.

## W eb Application

T he service layer could be reused to build a web-based finance management system.

## D atabase Support

A future version could use MySQL, PostgreSQL, or another database for handling larger d atasets.

## C harts and Visualization

C harts could be added to visualize:

- Monthly expenses.

- Category spending.


- Income versus expenses.

- Budget utilization.

## R ecurring Transactions

U sers could create recurring transactions such as monthly salary, rent, subscriptions, and bills.

## D ata Export

T he application could support exporting reports to PDF or Excel formats.

## A uthentication

A multi-user version could provide user accounts and authentication.

## A dvanced Budgeting

F uture versions could support:

- Yearly budgets.

- Multiple budgets per category.

- Budget history.

- Budget recommendations.

## 1 4. Conclusion

T he Personal Finance Tracker is a lightweight Java 17+ command-line application designed to s implify personal financial management.

T he application provides transaction management, category budgeting, financial reporting, CSV p ersistence, input validation, error handling, and logging. Its layered architecture separates the u ser interface from business logic and storage, making the system easier to maintain and e xtend.

T he project demonstrates practical application of Java programming, object-oriented design, file h andling, validation, testing, and software architecture concepts.

B y avoiding external dependencies and databases, the application remains simple to install and p ortable across systems with a compatible JDK.
