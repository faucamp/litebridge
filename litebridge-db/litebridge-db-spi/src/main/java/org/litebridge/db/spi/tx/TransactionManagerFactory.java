package org.litebridge.db.spi.tx;

import javax.sql.DataSource;

public interface TransactionManagerFactory {

    /**
     * Creates a new transaction manager instance.
     *
     * @param dataSource the data source to use for transactions
     * @return a new transaction manager instance
     */
    TransactionManager create(DataSource dataSource);
}
