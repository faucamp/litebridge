package org.litebridgedb.orm.api.register;

import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.alias.AliasTransformer;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.update.Delete;
import org.litebridgedb.db.spi.update.Insert;
import org.litebridgedb.db.spi.update.InsertResult;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.db.spi.update.UpdateResult;

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
    public InsertResult insert(final Insert insert, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public UpdateResult update(final Update update, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public List<Row> select(final Select select, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public UpdateResult delete(final Delete delete, final ConnectionProvider connectionProvider) throws SQLException {
        throw new UnsupportedOperationException("N/A");
    }

    @Override
    public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
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
