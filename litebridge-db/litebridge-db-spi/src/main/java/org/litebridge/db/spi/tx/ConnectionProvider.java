package org.litebridge.db.spi.tx;

import java.sql.SQLException;

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
