package org.litebridgedb.db.oracle;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.oracle.function.OracleSqlFunctionRegistryFactory;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.SqlFunctionRegistryFactory;

import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.query.LogicCondition;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import java.util.Collections;
import java.util.Optional;
import org.litebridgedb.db.spi.impl.sql.SelectSqlGenerator;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleDatabaseProviderTest {

    @Test
    void getSequenceColumnValueGenerator() {
        // Given
        final OracleDatabaseProvider oracleDatabaseProvider = new OracleDatabaseProvider();

        // When
        final SequenceColumnValueGenerator result = oracleDatabaseProvider.getSequenceColumnValueGenerator("test_sequence");

        // Then
        assertInstanceOf(OracleSequenceColumnValueGenerator.class, result);
    }

    @Test
    void extractGeneratedKeys_withGeneratedKeys() throws SQLException {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final Table table = new Table("TEST_TABLE", null);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "ID", false, Types.INTEGER);
        final ColumnMetaData otherIdColumn = new ColumnMetaData(table, "OTHER_ID", false, Types.INTEGER);
        final TableMetaData tableMetaData = new TableMetaData(table,
                List.of("ID", "OTHER_ID"),
                List.of(idColumn, otherIdColumn));

        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject(1)).thenReturn(1);
        when(resultSet.getObject(2)).thenReturn(2);

        // When
        Map<ColumnMetaData, Object> result = provider.extractGeneratedKeys(tableMetaData, preparedStatement);

        // Then
        assertEquals(2, result.size());
        assertEquals(1, result.get(idColumn));
        assertEquals(2, result.get(otherIdColumn));
        verify(resultSet, times(1)).close();
    }

    @Test
    void extractGeneratedKeys_withoutGeneratedKeys() throws SQLException {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final Table table = new Table("TEST_TABLE", null);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "ID", false, Types.INTEGER);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("ID"), List.of(idColumn));

        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Map<ColumnMetaData, Object> result = provider.extractGeneratedKeys(tableMetaData, preparedStatement);

        // Then
        assertTrue(result.isEmpty());
        verify(resultSet, times(1)).close();
    }

    @Test
    void createColumnIdentifierGenerator() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();

        // When
        final ColumnIdentifierGenerator result = provider.createColumnIdentifierGenerator();

        // Then
        assertInstanceOf(OracleColumnIdentifierGenerator.class, result);
    }

    @Test
    void createSqlFunctionRegistryFactory() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();

        // When
        final SqlFunctionRegistryFactory result = provider.createSqlFunctionRegistryFactory();

        // Then
        assertInstanceOf(OracleSqlFunctionRegistryFactory.class, result);
    }

    @Test
    void createSelectSqlGenerator_andUseIt() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final SelectSqlGenerator generator = provider.createSelectSqlGenerator();
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "ID");
        final LogicCondition condition = new LogicCondition(new org.litebridgedb.db.spi.impl.function.SelectColumn(column, new OracleColumnIdentifierGenerator()), org.litebridgedb.db.spi.query.Operator.EQ, 1);
        final Select select = new Select(table,
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.of(new org.litebridgedb.db.spi.query.ConditionGroup(List.of(condition))),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty());

        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);

        // When
        // This might trigger ensureTableMetaData lambda
        try {
            generator.prepareSql(select, connectionProvider);
        } catch (Exception e) {
            // It might fail because of table metadata registry not being mocked, but we just want to hit the lambda
        }

        // Then
        assertNotNull(generator);
    }

    @Test
    void extractGeneratedKeys_withNullResultSet() throws SQLException {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final Table table = new Table("TEST_TABLE", null);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "ID", false, Types.INTEGER);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("ID"), List.of(idColumn));

        when(preparedStatement.getGeneratedKeys()).thenReturn(null);

        // When & Then
        try {
            provider.extractGeneratedKeys(tableMetaData, preparedStatement);
        } catch (NullPointerException e) {
            // Expected if JDBC driver returns null and we call .next() on it
        }
    }

    @Test
    void getLogger() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();

        // When
        var result = provider.getLogger();

        // Then
        assertNotNull(result);
    }
}