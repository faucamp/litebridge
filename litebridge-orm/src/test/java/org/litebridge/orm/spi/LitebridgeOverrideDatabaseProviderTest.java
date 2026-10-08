package org.litebridge.orm.spi;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseMetaData;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.BatchUpdateResult;
import org.litebridge.db.spi.update.Result;
import org.litebridge.orm.LitebridgeBuilder;
import org.litebridge.orm.LitebridgeCore;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class LitebridgeOverrideDatabaseProviderTest {

    @Test
    void litebridgeClass() {
        // Given
        final LitebridgeOverrideDatabaseProvider<?> override = new TestLitebridgeOverrideDatabaseProvider();

        // When
        final Class<?> result = override.litebridgeClass();

        // Then
        assertEquals(LitebridgeCore.class, result);
    }

    @Test
    void createLitebridge() {
        // Given
        final LitebridgeOverrideDatabaseProvider<?> override = new TestLitebridgeOverrideDatabaseProvider();
        final LitebridgeBuilder.ConstructorArgs constructorArgs = mock(LitebridgeBuilder.ConstructorArgs.class);

        // When/Then
        assertThrows(IllegalStateException.class, () -> override.createLitebridge(constructorArgs));
    }

    private static class TestLitebridgeOverrideDatabaseProvider implements LitebridgeOverrideDatabaseProvider<LitebridgeCore> {

        @Override
        public Class<LitebridgeCore> litebridgeClass() {
            return LitebridgeCore.class;
        }

        @Override
        public DatabaseProviderMetaData metaData() {
            throw new UnsupportedOperationException("Test");
        }

        @Override
        public DatabaseMetaData databaseMetaData(final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException("Test");
        }

        @Override
        public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException("Test");
        }

        @Override
        public <T extends Result> T executeUpdate(final PreparedSql preparedSql, final Class<T> resultType, final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException("Test");
        }

        @Override
        public BatchUpdateResult executeBatch(final List<PreparedSql> preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException("Test");
        }

        @Override
        public List<Row> executeQuery(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
            throw new UnsupportedOperationException("Test");
        }

        @Override
        public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
            throw new UnsupportedOperationException("Test");
        }

        @Override
        public TypeConverter typeConverter() {
            throw new UnsupportedOperationException("Test");
        }

        @Override
        public SequenceColumnValueGenerator sequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Test");
        }

        @Override
        public SqlFunctionRegistry sqlFunctionRegistry() {
            throw new UnsupportedOperationException("Test");
        }
    }
}