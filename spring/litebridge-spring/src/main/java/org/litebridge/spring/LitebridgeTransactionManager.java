package org.litebridge.spring;

import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.tx.TransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Spring-compatible {@link org.springframework.transaction.PlatformTransactionManager} for Litebridge.
 * <p>
 * This class extends {@link DataSourceTransactionManager} to provide standard JDBC transaction management
 * while implementing Litebridge's {@link TransactionManager} interface to allow Litebridge to participate
 * in Spring-managed transactions.
 */
public class LitebridgeTransactionManager extends DataSourceTransactionManager implements TransactionManager {

    private final ThreadLocal<List<Connection>> nonTransactionalConnections = ThreadLocal.withInitial(ArrayList::new);

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
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }

        final DataSource dataSource = getDataSource();
        final List<Connection> currentConnections = nonTransactionalConnections.get();

        try {
            for (final Connection connection : currentConnections) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        } finally {
            currentConnections.clear();
            nonTransactionalConnections.remove();
        }
    }

    @Override
    public boolean requiresCleanup() {
        return !TransactionSynchronizationManager.isActualTransactionActive();
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
        final DataSource dataSource = getDataSource();
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        if (!DataSourceUtils.isConnectionTransactional(connection, dataSource)) {
            nonTransactionalConnections.get().add(connection);
        }

        return new ManagedConnection(connection);
    }

    @Override
    public DataSource getDataSource() {
        return Objects.requireNonNull(super.getDataSource(), "No datasource provided");
    }
}
