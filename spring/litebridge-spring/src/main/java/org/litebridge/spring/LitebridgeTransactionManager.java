package org.litebridge.spring;

import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.tx.TransactionManager;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Spring-compatible {@link org.springframework.transaction.PlatformTransactionManager} for Litebridge.
 * <p>
 * This class extends {@link DataSourceTransactionManager} to provide standard JDBC transaction management
 * while implementing Litebridge's {@link TransactionManager} interface to allow Litebridge to participate
 * in Spring-managed transactions.
 */
public class LitebridgeTransactionManager extends DataSourceTransactionManager implements TransactionManager {

    /**
     * Creates a new {@code LitebridgeTransactionManager}.
     *
     * @param dataSource the DataSource to use
     */
    public LitebridgeTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void begin() throws org.litebridge.db.spi.tx.TransactionException {
        throw new UnsupportedOperationException("LitebridgeTransactionManager does not support direct begin(). Use Spring's transaction management (e.g., @Transactional).");
    }

    @Override
    public void begin(final boolean readOnly, final Isolation isolation) throws org.litebridge.db.spi.tx.TransactionException {
        throw new UnsupportedOperationException("LitebridgeTransactionManager does not support direct begin(). Use Spring's transaction management (e.g., @Transactional).");
    }

    @Override
    public void cleanup() throws org.litebridge.db.spi.tx.TransactionException {
        // Handled by DataSourceTransactionManager
    }

    @Override
    public boolean requiresCleanup() {
        return false;
    }

    @Override
    public void addCommitCallback(final Runnable callback) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    callback.run();
                }
            });
        } else {
            // Auto-commit mode or no active synchronization
            callback.run();
        }
    }

    @Override
    public void addRollbackCallback(final Runnable callback) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        callback.run();
                    }
                }
            });
        }
    }

    @Override
    public void commit() throws org.litebridge.db.spi.tx.TransactionException, IllegalStateException {
        throw new UnsupportedOperationException("LitebridgeTransactionManager does not support direct commit(). Use Spring's transaction management (e.g., @Transactional).");
    }

    @Override
    public void rollback() throws org.litebridge.db.spi.tx.TransactionException, IllegalStateException {
        throw new UnsupportedOperationException("LitebridgeTransactionManager does not support direct rollback(). Use Spring's transaction management (e.g., @Transactional).");
    }

    @Override
    public boolean isTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    public boolean isRollbackOnly() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    @Override
    public ManagedConnection connection() throws SQLException {
        return new ManagedConnection(DataSourceUtils.getConnection(Objects.requireNonNull(getDataSource(), "No datasource provided")));
    }
}
