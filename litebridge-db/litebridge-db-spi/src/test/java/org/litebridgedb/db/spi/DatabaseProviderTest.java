package org.litebridgedb.db.spi;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.convert.TypeConverter;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseProviderTest {

    @Test
    void transformAlias() {
        // Given
        final String alias = "TestAlias";
        final DatabaseProvider databaseProvider = new TestDatabaseProvider();

        // When
        final String result = databaseProvider.transformAlias(alias);

        // Then
        assertEquals(alias, result);
    }

    public class TestDatabaseProvider implements DatabaseProvider {
        @Override
        public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public InsertResult insert(final Insert insert, final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public UpdateResult update(final Update update, final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Row> select(final Select select, final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public UpdateResult delete(final Delete delete, final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toSql(final Select select) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TypeConverter getTypeConverter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }
}