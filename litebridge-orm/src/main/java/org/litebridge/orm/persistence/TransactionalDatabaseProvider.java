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
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;

import java.sql.SQLException;
import java.util.List;

public final class TransactionalDatabaseProvider implements DatabaseProvider {

    private final TransactionManager transactionManager;
    private final DatabaseProvider databaseProvider;

    public TransactionalDatabaseProvider(final TransactionManager transactionManager, final DatabaseProvider databaseProvider) {
        this.transactionManager = transactionManager;
        this.databaseProvider = databaseProvider;
    }

    public TransactionManager transactionManager() {
        return transactionManager;
    }

    @Override
    public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.tableMetaData(table, transactionManager));
    }

    @Override
    public InsertResult insert(final Insert insert, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.insert(insert, transactionManager));
    }

    @Override
    public UpdateResult update(final Update update, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.update(update, transactionManager));
    }

    @Override
    public UpdateResult delete(final Delete delete, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.delete(delete, transactionManager));
    }

    @Override
    public List<Row> select(final Select select, final ConnectionProvider connectionProvider) throws SQLException {
        return executeAndCleanupIfNeeded(() -> databaseProvider.select(select, transactionManager));
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
