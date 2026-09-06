package org.litebridge.db.spi.impl;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.impl.engine.ExecutionEngine;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.impl.sql.SqlGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.db.spi.query.Select;
import org.slf4j.Logger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractDatabaseProviderTest {

    @Test
    void metaData() {
        // Given
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final MetaDataEngine metaDataEngine = mock(MetaDataEngine.class);
        final DatabaseProviderMetaData expected = new DatabaseProviderMetaData(true);
        when(sqlGenerator.metaDataEngine()).thenReturn(metaDataEngine);
        when(metaDataEngine.metaData()).thenReturn(expected);
        final TestProvider provider = new TestProvider(sqlGenerator, mock(ExecutionEngine.class));

        // When
        final DatabaseProviderMetaData result = provider.metaData();

        // Then
        assertSame(expected, result);
        verify(metaDataEngine).metaData();
    }

    @Test
    void databaseMetaData() throws Exception {
        // Given
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final MetaDataEngine metaDataEngine = mock(MetaDataEngine.class);
        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
        when(sqlGenerator.metaDataEngine()).thenReturn(metaDataEngine);
        final org.litebridge.db.spi.DatabaseMetaData expected = mock(org.litebridge.db.spi.DatabaseMetaData.class);
        when(metaDataEngine.databaseMetaData(connectionProvider)).thenReturn(expected);
        final TestProvider provider = new TestProvider(sqlGenerator, mock(ExecutionEngine.class));

        // When
        final org.litebridge.db.spi.DatabaseMetaData result = provider.databaseMetaData(connectionProvider);

        // Then
        assertSame(expected, result);
        verify(metaDataEngine).databaseMetaData(connectionProvider);
    }

    @Test
    void tableMetaData() throws Exception {
        // Given
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final MetaDataEngine metaDataEngine = mock(MetaDataEngine.class);
        final Table table = new Table("TEST_TABLE");
        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
        final TableMetaData expected = mock(TableMetaData.class);
        when(sqlGenerator.metaDataEngine()).thenReturn(metaDataEngine);
        when(metaDataEngine.ensureTableMetaData(table, connectionProvider)).thenReturn(expected);
        final TestProvider provider = new TestProvider(sqlGenerator, mock(ExecutionEngine.class));

        // When
        final TableMetaData result = provider.tableMetaData(table, connectionProvider);

        // Then
        assertSame(expected, result);
        verify(metaDataEngine).ensureTableMetaData(table, connectionProvider);
    }

    @Test
    void executeUpdate_insertResult() throws Exception {
        // Given
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final ExecutionEngine executionEngine = mock(ExecutionEngine.class);
        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
        final PreparedSql preparedSql = new PreparedSql("INSERT");
        final InsertResult expected = mock(InsertResult.class);
        when(sqlGenerator.metaDataEngine()).thenReturn(mock(MetaDataEngine.class));
        when(executionEngine.executeInsert(preparedSql, connectionProvider)).thenReturn(expected);
        final TestProvider provider = new TestProvider(sqlGenerator, executionEngine);

        // When
        final InsertResult result = provider.executeUpdate(preparedSql, InsertResult.class, connectionProvider);

        // Then
        assertSame(expected, result);
        verify(executionEngine).executeInsert(preparedSql, connectionProvider);
    }

    @Test
    void executeUpdate_updateResult() throws Exception {
        // Given
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final ExecutionEngine executionEngine = mock(ExecutionEngine.class);
        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
        final PreparedSql preparedSql = new PreparedSql("UPDATE");
        final UpdateResult expected = mock(UpdateResult.class);
        when(sqlGenerator.metaDataEngine()).thenReturn(mock(MetaDataEngine.class));
        when(executionEngine.executeUpdate(preparedSql, connectionProvider)).thenReturn(expected);
        final TestProvider provider = new TestProvider(sqlGenerator, executionEngine);

        // When
        final UpdateResult result = provider.executeUpdate(preparedSql, UpdateResult.class, connectionProvider);

        // Then
        assertSame(expected, result);
        verify(executionEngine).executeUpdate(preparedSql, connectionProvider);
    }

    @Test
    void executeQuery() throws Exception {
        // Given
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final ExecutionEngine executionEngine = mock(ExecutionEngine.class);
        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
        final PreparedSql preparedSql = new PreparedSql("SELECT");
        final List<Row> expected = List.of(mock(Row.class));
        when(sqlGenerator.metaDataEngine()).thenReturn(mock(MetaDataEngine.class));
        when(executionEngine.executeQuery(preparedSql, connectionProvider)).thenReturn(expected);
        final TestProvider provider = new TestProvider(sqlGenerator, executionEngine);

        // When
        final List<Row> result = provider.executeQuery(preparedSql, connectionProvider);

        // Then
        assertSame(expected, result);
        verify(executionEngine).executeQuery(preparedSql, connectionProvider);
    }

    @Test
    void typeConverter() {
        // Given
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final ExecutionEngine executionEngine = mock(ExecutionEngine.class);
        final TypeConverter expected = mock(TypeConverter.class);
        when(sqlGenerator.metaDataEngine()).thenReturn(mock(MetaDataEngine.class));
        when(executionEngine.typeConverter()).thenReturn(expected);
        final TestProvider provider = new TestProvider(sqlGenerator, executionEngine);

        // When
        final TypeConverter result = provider.typeConverter();

        // Then
        assertSame(expected, result);
    }

    @Test
    void aliasTransformer() {
        // Given
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final ExecutionEngine executionEngine = mock(ExecutionEngine.class);
        final AliasTransformer expected = mock(AliasTransformer.class);
        when(sqlGenerator.metaDataEngine()).thenReturn(mock(MetaDataEngine.class));
        when(executionEngine.aliasTransformer()).thenReturn(expected);
        final TestProvider provider = new TestProvider(sqlGenerator, executionEngine);

        // When
        final AliasTransformer result = provider.aliasTransformer();

        // Then
        assertSame(expected, result);
    }

    @Test
    void sequenceColumnValueGenerator() {
        // Given
        final TestProvider provider = newProvider();

        // When
        final String result = provider.sequenceColumnValueGenerator("TEST_SEQUENCE").generate(null);

        // Then
        org.junit.jupiter.api.Assertions.assertEquals("NEXT VALUE FOR TEST_SEQUENCE", result);
    }

    @Test
    void toSql() {
        // Given
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final Operation operation = new Select(new Table("TEST_TABLE"), List.of(), null, null, null, null, null, null);
        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
        when(sqlGenerator.metaDataEngine()).thenReturn(mock(MetaDataEngine.class));
        when(sqlGenerator.generateSql(operation, connectionProvider)).thenReturn("SQL");
        final TestProvider provider = new TestProvider(sqlGenerator, mock(ExecutionEngine.class));

        // When
        final String result = provider.toSql(operation, connectionProvider);

        // Then
        org.junit.jupiter.api.Assertions.assertEquals("SQL", result);
    }

    @Test
    void sqlFunctionRegistry_cachedValue() {
        // Given
        final TestProvider provider = newProvider();

        // When
        final SqlFunctionRegistry first = provider.sqlFunctionRegistry();
        final SqlFunctionRegistry second = provider.sqlFunctionRegistry();

        // Then
        assertNotNull(first);
        assertSame(first, second);
    }

    private static TestProvider newProvider() {
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        when(sqlGenerator.metaDataEngine()).thenReturn(mock(MetaDataEngine.class));
        when(sqlGenerator.selectSqlGenerator()).thenReturn(mock(SelectSqlGenerator.class));
        return new TestProvider(sqlGenerator, mock(ExecutionEngine.class));
    }

    private static final class TestProvider extends AbstractDatabaseProvider {
        private TestProvider(final SqlGenerator sqlGenerator, final ExecutionEngine executionEngine) {
            super(sqlGenerator, executionEngine);
        }

        @Override
        protected Logger getLogger() {
            return mock(Logger.class);
        }
    }
}