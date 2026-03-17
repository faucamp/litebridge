package org.litebridge.orm.api.tx;

import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.TransactionException;
import org.litebridge.orm.tx.Transaction;

public final class ReadOnlyClause {

    private final TransactionContextTerminal transactionContextTerminal;

    public ReadOnlyClause(final TransactionContextTerminal transactionContextTerminal) {
        this.transactionContextTerminal = transactionContextTerminal;
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
}
