package org.litebridgedb.orm.api.tx;

import org.litebridgedb.db.spi.tx.Isolation;
import org.litebridgedb.db.spi.tx.TransactionException;
import org.litebridgedb.orm.tx.Transaction;

/**
 * A class representing a read-only clause in a transaction context.
 * <p>
 * Instances of this class are used to enforce read-only transactional behavior
 * and configure transactional settings like isolation levels. The read-only
 * nature ensures that no changes can be made to the database within the
 * transactional scope.
 * <p>
 * This class delegates transactional operations to an underlying
 * {@link TransactionContextTerminal} instance.
 */
public final class ReadOnlyClause {

    private final TransactionContextTerminal transactionContextTerminal;

    public ReadOnlyClause(final TransactionContextTerminal transactionContextTerminal) {
        this.transactionContextTerminal = transactionContextTerminal;
        this.transactionContextTerminal.setReadOnly();
    }

    /**
     * Sets the transaction isolation level for the current thread.
     * <p>
     * The isolation level determines the degree of isolation provided
     * for transactions, affecting how changes made by one transaction
     * are visible to other transactions.
     *
     * @return Available clauses for transaction configuration chaining
     */
    public TransactionContextTerminal isolation(Isolation level) {
        transactionContextTerminal.setIsolation(level);
        return transactionContextTerminal;
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
        return transactionContextTerminal.begin();
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
        transactionContextTerminal.execute(runnable);
    }
}
