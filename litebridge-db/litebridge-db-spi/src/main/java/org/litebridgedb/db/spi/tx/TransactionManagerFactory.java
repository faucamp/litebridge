package org.litebridgedb.db.spi.tx;

import javax.sql.DataSource;

/**
 * A factory interface for creating instances of {@link TransactionManager}.
 * <p>
 * Implementations of this interface are responsible for providing
 * customized {@link TransactionManager} instances configured with a specific {@link DataSource}.
 */
public interface TransactionManagerFactory {

    /**
     * Creates a new transaction manager instance.
     *
     * @param dataSource the data source to use for transactions
     * @return a new transaction manager instance
     */
    TransactionManager create(DataSource dataSource);
}
