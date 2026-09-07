package com.financetracker.service;

import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.storage.CsvStorage;
import com.financetracker.util.AppLogger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service managing Transaction CRUD operations with immediate CSV persistence and auto-increment IDs.
 */
public class TransactionService {
    public static final String DEFAULT_CSV_PATH = "data/transactions.csv";
    private static final String CSV_HEADER = "id,date,type,category,amount,description";

    private final String csvPath;
    private final CsvStorage storage;
    private final List<Transaction> transactions = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public TransactionService() {
        this(DEFAULT_CSV_PATH, new CsvStorage());
    }

    public TransactionService(String csvPath, CsvStorage storage) {
        this.csvPath = Objects.requireNonNull(csvPath, "csvPath cannot be null");
        this.storage = Objects.requireNonNull(storage, "storage cannot be null");
        loadFromFile();
    }

    private synchronized void loadFromFile() {
        transactions.clear();
        int maxId = 0;
        try {
            List<String> lines = storage.readLines(csvPath);
            boolean firstLine = true;
            for (String line : lines) {
                if (firstLine) {
                    firstLine = false;
                    // Skip header line if present
                    if (line.toLowerCase().startsWith("id,")) {
                        continue;
                    }
                }
                try {
                    Transaction t = Transaction.fromCsvRow(line);
                    transactions.add(t);
                    if (t.getId() > maxId) {
                        maxId = t.getId();
                    }
                } catch (Exception e) {
                    AppLogger.logError("Parsing line in " + csvPath + ": " + line, e);
                }
            }
            nextId.set(maxId + 1);
            AppLogger.info("Loaded " + transactions.size() + " transactions from " + csvPath);
        } catch (IOException e) {
            AppLogger.logError("Loading transactions from " + csvPath, e);
        }
    }

    private synchronized void saveToFile() {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        for (Transaction t : transactions) {
            lines.add(t.toCsvRow());
        }
        try {
            storage.writeLines(csvPath, lines);
        } catch (IOException e) {
            AppLogger.logError("Saving transactions to " + csvPath, e);
            throw new RuntimeException("Failed to persist transactions: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a new transaction, auto-increments the ID, immediately persists to CSV, and logs.
     */
    public synchronized Transaction addTransaction(LocalDate date, TransactionType type, String category, double amount, String description) {
        int id = nextId.getAndIncrement();
        Transaction transaction = new Transaction(id, date, type, category, amount, description);
        transactions.add(transaction);
        saveToFile();
        AppLogger.logAdd("Transaction", transaction);
        return transaction;
    }

    /**
     * Edits an existing transaction by ID. Returns true if updated, false if not found.
     */
    public synchronized boolean editTransaction(int id, LocalDate date, TransactionType type, String category, double amount, String description) {
        for (int i = 0; i < transactions.size(); i++) {
            Transaction existing = transactions.get(i);
            if (existing.getId() == id) {
                Transaction updated = new Transaction(id, date, type, category, amount, description);
                transactions.set(i, updated);
                saveToFile();
                AppLogger.logEdit("Transaction #" + id, updated);
                return true;
            }
        }
        AppLogger.warning("Attempted to edit non-existent transaction #" + id);
        return false;
    }

    /**
     * Deletes a transaction by ID. Returns true if deleted, false if not found.
     */
    public synchronized boolean deleteTransaction(int id) {
        Iterator<Transaction> it = transactions.iterator();
        while (it.hasNext()) {
            Transaction t = it.next();
            if (t.getId() == id) {
                it.remove();
                saveToFile();
                AppLogger.logDelete("Transaction", t);
                return true;
            }
        }
        AppLogger.warning("Attempted to delete non-existent transaction #" + id);
        return false;
    }

    /**
     * Retrieves an unmodifiable list of all transactions sorted by date descending.
     */
    public synchronized List<Transaction> getAllTransactions() {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate).reversed().thenComparing(Transaction::getId).reversed())
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Finds a transaction by its ID.
     */
    public synchronized Optional<Transaction> getTransactionById(int id) {
        return transactions.stream().filter(t -> t.getId() == id).findFirst();
    }

    /**
     * Filters transactions matching a specific YearMonth.
     */
    public synchronized List<Transaction> getTransactionsByMonth(YearMonth month) {
        Objects.requireNonNull(month, "month cannot be null");
        return transactions.stream()
                .filter(t -> YearMonth.from(t.getDate()).equals(month))
                .sorted(Comparator.comparing(Transaction::getDate))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Filters transactions matching a category (case-insensitive).
     */
    public synchronized List<Transaction> getTransactionsByCategory(String category) {
        Objects.requireNonNull(category, "category cannot be null");
        return transactions.stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(category.trim()))
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .collect(Collectors.toUnmodifiableList());
    }

    public synchronized int getCount() {
        return transactions.size();
    }
}
