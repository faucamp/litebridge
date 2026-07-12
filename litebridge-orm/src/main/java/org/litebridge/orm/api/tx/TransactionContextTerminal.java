package org.litebridge.orm.api.tx;

import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.TransactionException;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.tx.Transaction;

/**
 * Represents the terminal stage of a transactional context, providing core methods for configuring
 * and managing transactions. This class defines basic transactional operations such as setting the
 * transaction as read-only, specifying isolation levels, and handling the lifecycle of transactions.
 * <p>
 * This is a sealed class, permitting only specific subclasses to extend it.
 */
public sealed class TransactionContextTerminal permits TransactionContext {

    protected final TransactionManager transactionManager;
    protected boolean readOnly = false;
    protected Isolation isolation = Isolation.DEFAULT;

    TransactionContextTerminal(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Indicates that the transaction should be read-only.
     * <p>
     * Read-only transactions are used when the transaction
     * is intended to only read data and not modify it.
     */
    void setReadOnly() {
        this.readOnly = true;
    }

    /**
     * Sets the transaction isolation level for the current thread.
     * <p>
     * The isolation level determines the degree of isolation provided
     * for transactions, affecting how changes made by one transaction
     * are visible to other transactions.
     *
     * @param isolation the desired isolation level for the transaction
     */
    void setIsolation(final Isolation isolation) {
        this.isolation = isolation;
    }

    /**
     * Begins a new transaction for the current thread.
     * <p>
     * This method should be called before performing
     * operations that need to be executed within a transactional context.
     * It ensures that subsequent database interactions are part of the same transaction
     * until it is either committed or rolled back.
     * <p>
     * This method may be called multiple times (nested).
     *
     * @return a {@link Transaction} auto-closeable, suitable for use in a try-with-resources statement.
     * @throws TransactionException if an error occurs during starting the transaction
     */
    public Transaction begin() {
        transactionManager.begin(readOnly, isolation);
        return new Transaction(transactionManager);
    }

    /**
     * Executes a given {@link Runnable} within the context of a transaction.
     * <p>
     * This method begins a new transaction, executes the provided {@link Runnable},
     * and commits the transaction if no exception occurs. In case of an exception,
     * the transaction is rolled back, and a {@link TransactionException} is thrown.
     *
     * @param runnable the {@link Runnable} containing the operations to be executed
     *                 within the transactional context
     * @throws TransactionException if an error occurs during execution or if the
     *                              transaction is rolled back due to an exception
     */
    public void execute(final Runnable runnable) {
        try (Transaction tx = begin()) {
            runnable.run();
            tx.commit();
        } catch (Exception ex) {
            throw new TransactionException("Transaction rolled back", ex);
        }
    }
}
