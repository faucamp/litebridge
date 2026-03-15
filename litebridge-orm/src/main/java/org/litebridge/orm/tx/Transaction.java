package org.litebridge.orm.tx;

import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.tx.TransactionControl;
import org.litebridge.db.spi.tx.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

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
