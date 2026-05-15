package org.litebridgedb.orm.tx;

import org.litebridgedb.db.spi.tx.ManagedConnection;
import org.litebridgedb.db.spi.tx.TransactionControl;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Represents a transactional context that allows controlled execution of
 * operations within a transaction. This class is immutable and thread-safe.
 * <p>
 * A transaction is used to perform operations such as commit and rollback
 * on a transactional resource. It also provides methods for obtaining
 * connections and checking the state of the transaction.
 * <p>
 * Instances of this class are created with a {@link TransactionManager},
 * which provides the underlying transaction management capabilities.
 * The lifecycle of a transaction includes starting, committing, rolling
 * back, or closing. A transaction can be completed either by calling
 * {@link #commit()}, {@link #rollback()}, or automatically when the
 * {@link #close()} method is invoked.
 * <p>
 * This class also implements {@link AutoCloseable}, ensuring that resources are
 * properly released if the transaction block terminates without explicit commit.
 * If a transaction is not committed when closed, it will be rolled back.
 */
public final class Transaction implements TransactionControl, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transaction.class);
    private final TransactionManager transactionManager;
    private boolean completed = false;

    public Transaction(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void commit() {
        if (completed) {
            LOGGER.trace("Transaction already completed, aborting commit");
            throw new IllegalStateException("Transaction already completed");
        }

        transactionManager.commit();
        this.completed = true;
    }

    @Override
    public void rollback() {
        if (completed) {
            LOGGER.trace("Transaction already completed, ignore rollback");
            return;
        }

        transactionManager.rollback();
        this.completed = true;
    }

    @Override
    public boolean isTransactionActive() {
        return transactionManager.isTransactionActive();
    }

    @Override
    public boolean isRollbackOnly() {
        return transactionManager.isRollbackOnly();
    }

    @Override
    public ManagedConnection connection() throws SQLException {
        return transactionManager.connection();
    }

    @Override
    public void close() {
        final boolean rollbackOnly = transactionManager.isRollbackOnly();

        if (!completed || rollbackOnly) {
            try {
                if (!rollbackOnly) {
                    LOGGER.warn("Auto-closeable transaction not committed; rolling back");
                }

                rollback();
            } finally {
                completed = true;
            }
        }
    }
}
