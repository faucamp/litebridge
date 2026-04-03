package org.litebridge.orm.tx;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.tx.TransactionException;
import org.litebridge.db.spi.tx.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default Litebridge Transaction Manager
 * <p>
 * The {@code DefaultTransactionManager} provides transaction management using a {@link ThreadLocal}
 * to maintain a transaction-bound {@link Connection} for each thread.
 * It ensures that all database operations within a transaction are performed using the same
 * connection and that cleanup is handled properly.
 * <p>
 * Key features:
 * - Manages transactions per thread using a {@link ThreadLocal}.
 * - Initiates, commits, or rolls back transactions.
 * - Provides a transaction-bound connection for database interaction.
 * - Performs automatic cleanup of resources when a transaction ends.
 * <p>
 * This class is thread-safe in the sense that each thread is provided with its own transaction state,
 * but it is not suitable for use in scenarios where multiple threads share the same transaction.
 * <p>
 * Common Exceptions:
 * - {@link IllegalStateException} is thrown when a method requiring a transaction is invoked without an active transaction
 * - {@link TransactionException} is thrown for failures during transaction begin, commit, or rollback operations.
 * - {@link SQLException} is propagated when querying for a new connection or performing cleanup operations.
 **/
public final class DefaultTransactionManager implements TransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTransactionManager.class);
    private final DataSource dataSource;
    private final ThreadLocal<@Nullable TransactionState> holder = new ThreadLocal<>();

    public DefaultTransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void begin() {
        begin(false, Isolation.DEFAULT);
    }

    @Override
    @SuppressWarnings("MagicConstant")
    public void begin(final boolean readOnly, final Isolation isolation) {
        final TransactionState state = holder.get();

        if (state != null) {
            if (state.autoCommit) {
                LOGGER.error("Managed connection already acquired in auto-commit mode; nested transaction not supported");
                throw new TransactionException("Managed connection already acquired in auto-commit mode");
            } else {
                state.depth++;
                return;
            }
        }

        LOGGER.trace("Begin transaction");
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            if (readOnly) {
                connection.setReadOnly(readOnly);
            }

            if (isolation != Isolation.DEFAULT) {
                connection.setTransactionIsolation(isolation.level());
            }

            holder.set(new TransactionState(connection, false));
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    LOGGER.warn("Failed to close connection after begin failure", closeEx);
                }
            }

            throw new TransactionException("Failed to begin transaction", ex);
        }
    }

    @Override
    public ManagedConnection connection() throws SQLException {
        final TransactionState state = holder.get();

        if (state == null) {
            // No transaction active - create and return a new managed connection without a transaction
            final TransactionState newState = new TransactionState(dataSource.getConnection(), true);
            holder.set(newState);
            return newState.managedConnection;
        } else {
            return state.managedConnection;
        }
    }

    @Override
    public void commit() {
        LOGGER.trace("Commit transaction");
        final TransactionState state = transactionStateOrThrow();

        if (state.depth > 0) {
            state.depth--;
            return;
        }

        if (state.rollbackOnly) {
            rollback(state);
            throw new TransactionException("Transaction marked rollback-only");
        }

        try {
            state.connection.commit();
            cleanup(false);
        } catch (SQLException ex) {
            cleanup(true);
            throw new TransactionException("Commit failed", ex);
        }

        executeCompletionCallbacks(state.getCommitCallbacks(), "Rollback callback failed");
    }

    @Override
    public void rollback() {
        final TransactionState state = transactionStateOrThrow();

        if (state.depth > 0) {
            state.rollbackOnly = true;
            state.depth--;
            return;
        }

        LOGGER.trace("Rollback transaction");
        rollback(state);
    }

    @Override
    public boolean isTransactionActive() {
        return isTransactionActive(holder.get());
    }

    @Override
    public boolean isRollbackOnly() {
        final TransactionState state = holder.get();
        return state != null && state.rollbackOnly;
    }

    @Override
    public void cleanup() throws TransactionException {
        cleanup(false);
    }

    @Override
    public boolean requiresCleanup() {
        return holder.get() != null;
    }

    @Override
    public void addCommitCallback(Runnable callback) {
        final TransactionState state = holder.get();

        if (isTransactionActive(state)) {
            state.addCommitCallback(callback);
        } else {
            // Auto-commit, no transaction
            callback.run();
        }
    }

    @Override
    public void addRollbackCallback(final Runnable callback) {
        final TransactionState state = holder.get();

        if (isTransactionActive(state)) {
            state.addRollbackCallback(callback);
        }
    }

    private TransactionState transactionStateOrThrow() {
        return ObjectUtils.requireNonNull(holder.get(), () -> new IllegalStateException("No transaction active"));
    }

    private void rollback(final TransactionState state) {
        try {
            state.connection.rollback();
            cleanup(false);
        } catch (final SQLException ex) {
            cleanup(true);

            throw new TransactionException("Rollback failed", ex);
        }

        executeCompletionCallbacks(state.getRollbackCallbacks(), "Rollback callback failed");
    }

    private static boolean isTransactionActive(final @Nullable TransactionState state) {
        return state != null && !state.autoCommit;
    }

    private static void executeCompletionCallbacks(final List<Runnable> callbacks, final String errorStr) {
        if (!callbacks.isEmpty()) {
            LOGGER.trace("Executing transaction completion callbacks");

            try {
                callbacks.forEach(Runnable::run);
            } catch (Exception ex) {
                throw new TransactionException(errorStr, ex);
            }

            LOGGER.trace("Transaction completion callbacks done");
        }
    }

    private void cleanup(final boolean quiet) {
        final TransactionState state = holder.get();

        if (state == null) {
            return;
        }

        try {
            try {
                if (!state.autoCommit) {
                    state.connection.setAutoCommit(true);
                }
            } finally {
                state.connection.close();
            }
        } catch (SQLException ex) {
            if (quiet) {
                LOGGER.error("Quiet cleanup failed", ex);
            } else {
                throw new TransactionException("Cleanup failed", ex);
            }
        } finally {
            holder.remove();
        }
    }

    /**
     * State of a transaction within the thread-local context.
     */
    private static final class TransactionState {
        /**
         * Transaction-bound connection.
         */
        final Connection connection;
        /**
         * Managed connection wrapper.
         */
        final ManagedConnection managedConnection;
        /**
         * Whether the connection is in auto-commit mode.
         */
        final boolean autoCommit;
        /**
         * Current nesting depth of a transaction within the thread-local context.
         */
        int depth = 0;
        /**
         * Indicates whether the transaction is marked for rollback only.
         */
        boolean rollbackOnly = false;
        /**
         * List of callbacks to execute when a transaction is committed.
         * <p>
         * This is used by the ORM to synchronise the state of DTOs change tracking after updates
         */
        private @Nullable List<Runnable> commitCallbacks;
        /**
         * List of callbacks to execute when a transaction is rolled back.
         * <p>
         * This is used by the ORM to undo in-flight changes to DTOs while the transaction is running (e.g. generated PK setting)
         */
        private @Nullable List<Runnable> rollbackCallbacks;

        TransactionState(final Connection connection, final boolean autoCommit) throws SQLException {
            this.connection = connection;
            this.managedConnection = new ManagedConnection(connection);
            this.autoCommit = autoCommit;
        }

        void addCommitCallback(final Runnable callback) {
            if (commitCallbacks == null) {
                commitCallbacks = new ArrayList<>();
            }

            commitCallbacks.add(callback);
        }

        List<Runnable> getCommitCallbacks() {
            return commitCallbacks != null ? commitCallbacks : Collections.emptyList();
        }

        void addRollbackCallback(final Runnable callback) {
            if (rollbackCallbacks == null) {
                rollbackCallbacks = new ArrayList<>();
            }

            rollbackCallbacks.add(callback);
        }

        List<Runnable> getRollbackCallbacks() {
            return rollbackCallbacks != null ? rollbackCallbacks : Collections.emptyList();
        }
    }
}
