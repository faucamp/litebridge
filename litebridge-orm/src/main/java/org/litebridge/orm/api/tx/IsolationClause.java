package org.litebridge.orm.api.tx;

import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.TransactionException;
import org.litebridge.orm.tx.Transaction;

public final class IsolationClause {

    private final TransactionContextTerminal transactionContextTerminal;

    public IsolationClause(final Isolation level, final TransactionContextTerminal transactionContextTerminal) {
        this.transactionContextTerminal = transactionContextTerminal;
        this.transactionContextTerminal.setIsolation(level);
    }

    /**
     * Indicates that the transaction should be read-only.
     * <p>
     * Read-only transactions are used when the transaction
     * is intended to only read data and not modify it.
     *
     * @return Available clauses for transaction configuration chaining
     */
    public TransactionContextTerminal readOnly() {
        transactionContextTerminal.setReadOnly();
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
}
