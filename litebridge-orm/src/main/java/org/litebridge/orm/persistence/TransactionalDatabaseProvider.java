package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;

import java.sql.SQLException;
import java.util.List;

/**
 * A decorator class that wraps a {@link DatabaseProvider} to provide transactional support using
 * a {@link TransactionManager}.
 * <p>
 * This class ensures that any database operation is executed
 * with proper transaction management and cleanup logic when necessary.
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Delegates all database operations to the underlying {@link DatabaseProvider}.</li>
 *   <li>Ensures that transactional contexts are handled properly using the provided {@link TransactionManager}.</li>
 *   <li>Cleans up the transaction context if it's no longer active and cleanup is required.</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * This class is thread-safe if the provided {@link TransactionManager} and {@link DatabaseProvider}
 * are thread-safe.
 *
 * <h2>Implementation Details:</h2>
 * <ul>
 *   <li>The class uses a {@code SqlOperationSupplier} functional interface to wrap database operations
 * and ensure proper invocation of cleanup logic via the {@link #executeAndCleanupIfNeeded(SqlOperationSupplier)} method.</li>
 *   <li>Transaction cleanup logic is triggered if no active transaction exists in the {@link TransactionManager},
 * and cleanup is deemed necessary.</li>
 * </ul>
 *
 * @see DatabaseProvider
 * @see TransactionManager
 */
public final class TransactionalDatabaseProvider implements DatabaseProvider {

    private final TransactionManager transactionManager;
    private final DatabaseProvider databaseProvider;

    /**
     * Constructs a new TransactionalDatabaseProvider with the provided transaction manager
     * and database provider.
     * <p>
     * This class is responsible for managing transactional interactions
     * with the underlying database provider.
     *
     * @param transactionManager The transaction manager responsible for managing transaction lifecycles.
     * @param databaseProvider   The database provider that executes database operations within transactions.
     */
    public TransactionalDatabaseProvider(final TransactionManager transactionManager, final DatabaseProvider databaseProvider) {
        this.transactionManager = transactionManager;
        this.databaseProvider = databaseProvider;
    }

    /**
     * Returns the transaction manager responsible for managing transaction lifecycles.
     *
     * @return The transaction manager.
     */
    public TransactionManager transactionManager() {
        return transactionManager;
    }

    @Override
    public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.tableMetaData(table, transactionManager));
    }

    @Override
    public InsertResult insert(final PreparedSql insert, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.insert(insert, transactionManager));
    }

    @Override
    public UpdateResult update(final PreparedSql update, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.update(update, transactionManager));
    }

    @Override
    public UpdateResult delete(final PreparedSql delete, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.delete(delete, transactionManager));
    }

    @Override
    public UpdateResult merge(final PreparedSql merge, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.delete(merge, transactionManager));
    }

    @Override
    public List<Row> select(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.select(preparedSql, transactionManager));
    }

    @Override
    public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
        return databaseProvider.toSql(operation, transactionManager);
    }

    @Override
    public List<Row> nativeSqlQuery(final String sql, final List<@Nullable Object> bindParameters, final ConnectionProvider connectionProvider) throws SQLException {
        return databaseProvider.nativeSqlQuery(sql, bindParameters, transactionManager);
    }

    @Override
    public UpdateResult nativeSqlUpdate(final String sql, final List<@Nullable Object> bindParameters, final ConnectionProvider connectionProvider) throws SQLException {
        return databaseProvider.nativeSqlUpdate(sql, bindParameters, transactionManager);
    }

    @Override
    public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequenceName) {
        return databaseProvider.getSequenceColumnValueGenerator(sequenceName);
    }

    @Override
    public SqlFunctionRegistry getSqlFunctionRegistry() {
        return databaseProvider.getSqlFunctionRegistry();
    }

    @Override
    public TypeConverter getTypeConverter() {
        return databaseProvider.getTypeConverter();
    }

    @Override
    public AliasTransformer getAliasTransformer() {
        return databaseProvider.getAliasTransformer();
    }

    private <T> T executeAndCleanupIfNeeded(final SqlOperationSupplier<T> supplier) throws SQLException {
        try {
            return supplier.get();
        } finally {
            cleanupIfNeeded();
        }
    }

    private void cleanupIfNeeded() {
        if (!transactionManager.isTransactionActive() && transactionManager.requiresCleanup()) {
            transactionManager.cleanup();
        }
    }

    @FunctionalInterface
    private interface SqlOperationSupplier<T> {
        T get() throws SQLException;
    }
}
