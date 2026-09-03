package org.litebridge.orm.api.register;

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
import org.litebridge.db.spi.update.InsertResult;
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

    @Override
    public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public InsertResult insert(final PreparedSql insert, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public UpdateResult update(final PreparedSql update, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public List<Row> select(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public UpdateResult delete(final PreparedSql delete, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public UpdateResult merge(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public List<Row> nativeSqlQuery(final String sql, final List<@Nullable Object> bindParameters, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public UpdateResult nativeSqlUpdate(final String sql, final List<@Nullable Object> bindParameters, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public TypeConverter getTypeConverter() {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return new PlaceholderSequenceColumnValueGenerator(sequence);
    }

    @Override
    public SqlFunctionRegistry getSqlFunctionRegistry() {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public AliasTransformer getAliasTransformer() {
        throw new UnsupportedOperationException("N/A");
    }
}
