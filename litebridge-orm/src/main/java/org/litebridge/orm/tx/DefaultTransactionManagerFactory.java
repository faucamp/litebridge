package org.litebridge.orm.tx;

import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.tx.TransactionManagerFactory;

import javax.sql.DataSource;

/**
 * A factory for creating instances of {@link DefaultTransactionManager}.
 * <p>
 * This class provides an implementation of the {@link TransactionManagerFactory} interface.
 * It creates transaction manager instances bound to a specific {@link DataSource}.
 * <p>
 * The produced {@link DefaultTransactionManager} instances are used to manage
 * database transactions within a defined transactional context.
 * This includes managing the lifecycle of transactions (begin, commit, rollback)
 * and coordinating these with the underlying database system.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class DefaultTransactionManagerFactory implements TransactionManagerFactory {

    @Override
    public TransactionManager create(final DataSource dataSource) {
        return new DefaultTransactionManager(dataSource);
    }
}
