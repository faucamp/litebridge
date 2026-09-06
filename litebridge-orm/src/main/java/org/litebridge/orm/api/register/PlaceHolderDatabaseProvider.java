package org.litebridge.orm.api.register;

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
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.UpdateResult;

import java.sql.SQLException;
import java.util.List;

/**
 * A placeholder implementation of the {@link DatabaseProvider} interface.
 * <p>
 * This class is designed to act as a stub for the {@code DatabaseProvider}
 * interface, with all methods throwing {@link UnsupportedOperationException}.
 * <p>
 * It is not intended to provide actual database functionality and serves
 * purely as a placeholder which is overridden by the actual database provider during registration.
 */
final class PlaceHolderDatabaseProvider implements DatabaseProvider {

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @return this implementation always throws {@link UnsupportedOperationException}
     */
    @Override
    public DatabaseProviderMetaData metaData() {
        throw new UnsupportedOperationException("N/A");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @param connectionProvider Not used
     * @return this implementation always throws {@link UnsupportedOperationException}
     */
    @Override
    public DatabaseMetaData databaseMetaData(final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @param table              Not used
     * @param connectionProvider Not used
     * @return this implementation always throws {@link UnsupportedOperationException}
     */
    @Override
    public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) {
        throw new UnsupportedOperationException("N/A");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @param preparedSql        Not used
     * @param resultType         Not used
     * @param connectionProvider Not used
     * @return this implementation always throws {@link UnsupportedOperationException}
     */
    @Override
    public <T extends UpdateResult> T executeUpdate(final PreparedSql preparedSql, final Class<T> resultType, final ConnectionProvider connectionProvider) {
        throw new UnsupportedOperationException("N/A");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @param preparedSql        Not used
     * @param connectionProvider Not used
     * @return this implementation always throws {@link UnsupportedOperationException}
     */
    @Override
    public List<Row> executeQuery(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) {
        throw new UnsupportedOperationException("N/A");
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * @param operation          Not used
     * @param connectionProvider Not used
     * @return this implementation always throws {@link UnsupportedOperationException}
     */
    @Override
    public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public TypeConverter typeConverter() {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public SequenceColumnValueGenerator sequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return new PlaceholderSequenceColumnValueGenerator(sequence);
    }

    @Override
    public SqlFunctionRegistry sqlFunctionRegistry() {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public AliasTransformer aliasTransformer() {
        throw new UnsupportedOperationException("N/A");
    }
}
