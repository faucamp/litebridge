package org.litebridge.orm.tx;

import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.tx.TransactionManagerFactory;

import javax.sql.DataSource;

public final class DefaultTransactionManagerFactory implements TransactionManagerFactory {

    @Override
    public TransactionManager create(final DataSource dataSource) {
        return new DefaultTransactionManager(dataSource);
    }
}
