package org.litebridgedb.db.spi.tx;

import java.sql.SQLException;

/**
 * Provides a mechanism to obtain a managed database connection, typically bound to the scope of a transaction.
 * This interface abstracts the underlying connection management, ensuring proper lifecycle handling of the
 * connection resource.
 * <p>
 * Implementations of this interface are expected to manage the allocation, reuse, and cleanup of
 * database connections in a manner that aligns with the application's transaction and resource management
 * strategy. Connections obtained through this interface may either participate in transactions or operate
 * in auto-commit mode, depending on the context in which they are used.
 */
public interface ConnectionProvider {

    /**
     * Provides a transaction-bound managed connection to the underlying database.
     * <p>
     * This method is typically used to execute database operations within
     * the context of the current transaction (or not, if the connection is in auto-commit mode).
     * <p>
     * Transactions themselves (and corresponding {@link java.sql.Connection} lifecycle methods)
     * are managed by a {@link TransactionManager} and are not supported by the returned {@link ManagedConnection}.
     *
     * @return a {@link ManagedConnection} instance representing the database connection
     * @throws SQLException if an error occurs while obtaining the connection
     */
    ManagedConnection connection() throws SQLException;
}
