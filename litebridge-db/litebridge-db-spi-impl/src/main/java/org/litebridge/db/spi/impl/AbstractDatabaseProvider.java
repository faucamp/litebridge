package org.litebridge.db.spi.impl;

import org.litebridge.db.spi.DatabaseMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.engine.ExecutionEngine;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;

import java.sql.SQLException;
import java.util.List;

/**
 * An abstract implementation of the {@link DatabaseProvider} interface that provides a framework for interacting
 * with a database by managing SQL queries, metadata retrieval, and type conversions. This class serves as a base
 * for specific database implementations, handling common functionality while leaving database-specific details
 * to subclasses.
 * <p>
 * This class includes utility methods for preparing and executing SQL statements, fetching table metadata, and
 * performing insert, update, and select operations. It uses a caching mechanism for table metadata to improve
 * efficiency and ensures type conversion using a pluggable {@link TypeConverter}.
 */
public abstract class AbstractDatabaseProvider implements DatabaseProvider {

    protected final DatabaseProviderContext context;

    /**
     * Constructs a new {@code AbstractDatabaseProvider}.
     */
    protected AbstractDatabaseProvider(final DatabaseProviderContext context) {
        this.context = context;
    }

    @Override
    public DatabaseProviderMetaData metaData() {
        return context.metaDataEngine().metaData();
    }

    @Override
    public DatabaseMetaData databaseMetaData(final ConnectionProvider connectionProvider) throws SQLException {
        return context.metaDataEngine().databaseMetaData(connectionProvider);
    }

    @Override
    public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
        return context.metaDataEngine().ensureTableMetaData(table, connectionProvider);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends UpdateResult> T executeUpdate(final PreparedSql preparedSql, final Class<T> resultType, final ConnectionProvider connectionProvider) throws SQLException {
        final ExecutionEngine executionEngine = context.executionEngine();

        if (resultType == InsertResult.class) {
            return (T) executionEngine.executeInsert(preparedSql, connectionProvider);
        } else {
            return (T) executionEngine.executeUpdate(preparedSql, connectionProvider);
        }
    }

    @Override
    public List<Row> executeQuery(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        return context.executionEngine().executeQuery(preparedSql, connectionProvider);
    }

    @Override
    public TypeConverter typeConverter() {
        return context.executionEngine().typeConverter();
    }

    @Override
    public SequenceColumnValueGenerator sequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return context.sequenceColumnValueGeneratorCreator().apply(sequence);
    }

    @Override
    public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
        return context.sqlGenerator().generateSql(operation, connectionProvider);
    }

    @Override
    public SqlFunctionRegistry sqlFunctionRegistry() {
        return context.sqlFunctionRegistry();
    }

    @Override
    public AliasTransformer aliasTransformer() {
        return context.executionEngine().aliasTransformer();
    }
}
