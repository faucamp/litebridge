package org.litebridge.orm.api.tx;

import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.TransactionControl;
import org.litebridge.db.spi.tx.TransactionException;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.tx.Transaction;

public sealed class TransactionContextTerminal permits TransactionContext {

    protected final TransactionManager transactionManager;
    protected boolean readOnly = false;
    protected Isolation isolation = Isolation.DEFAULT;

    TransactionContextTerminal(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    void setReadOnly() {
        this.readOnly = true;
    }

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
     * @throws TransactionException if an error occurs during starting the transaction
     */
    public Transaction begin() {
        transactionManager.begin(readOnly, isolation);
        return new Transaction(transactionManager);
    }
}
