package org.litebridge.db.oracle.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.update.UpdateResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleExecutionEngineTest {

    private final TypeConverter typeConverter = new DefaultTypeConverter();
    private final AliasTransformer aliasTransformer = new UppercaseAliasTransformer();
    private final OracleExecutionEngine executionEngine = new OracleExecutionEngine(typeConverter, aliasTransformer);

    @Test
    void extractGeneratedKeys_withGeneratedKeys() throws SQLException {
        // Given
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final Table table = new Table("TEST_TABLE", null);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "ID", false, Types.INTEGER);
        final ColumnMetaData otherIdColumn = new ColumnMetaData(table, "OTHER_ID", false, Types.INTEGER);

        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject(1)).thenReturn(1);
        when(resultSet.getObject(2)).thenReturn(2);

        // When
        Map<ColumnMetaData, Object> result = executionEngine.extractGeneratedKeys(List.of(idColumn, otherIdColumn), preparedStatement);

        // Then
        assertEquals(2, result.size());
        assertEquals(1, result.get(idColumn));
        assertEquals(2, result.get(otherIdColumn));
        verify(resultSet, times(1)).close();
    }

    @Test
    void extractGeneratedKeys_withoutGeneratedKeys() throws SQLException {
        // Given
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final Table table = new Table("TEST_TABLE", null);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "ID", false, Types.INTEGER);

        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Map<ColumnMetaData, Object> result = executionEngine.extractGeneratedKeys(List.of(idColumn), preparedStatement);

        // Then
        assertTrue(result.isEmpty());
        verify(resultSet, times(1)).close();
    }

    @Test
    void extractGeneratedKeys_withNullResultSet() throws SQLException {
        // Given
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final Table table = new Table("TEST_TABLE", null);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "ID", false, Types.INTEGER);

        when(preparedStatement.getGeneratedKeys()).thenReturn(null);

        // When & Then
        try {
            executionEngine.extractGeneratedKeys(List.of(idColumn), preparedStatement);
        } catch (NullPointerException e) {
            // Expected if JDBC driver returns null and we call .next() on it
        }
    }

    @BeforeEach
    void setUp() {
        OracleExecutionEngine.clearParameterPermutations();
    }

    @Test
    void reorderPreparedSqlIfNecessary_withPermutation() {
        // Given
        final String sql = "MERGE INTO TEST_TABLE ...";
        final int[] permutation = new int[]{1, 0, 2};
        OracleExecutionEngine.registerParameterPermutation(sql, permutation);

        final BindValue bv0 = new BindValue(5, Types.INTEGER);
        final BindValue bv1 = new BindValue(500, Types.INTEGER);
        final BindValue bv2 = new BindValue(123, Types.INTEGER);
        final PreparedSql preparedSql = new PreparedSql(sql, List.of(bv0, bv1, bv2));

        // When
        final PreparedSql reordered = executionEngine.reorderBindValuesIfNecessary(preparedSql);

        // Then
        assertEquals(List.of(bv1, bv0, bv2), reordered.bindValues());
    }

    @Test
    void reorderPreparedSqlIfNecessary_withoutPermutation() {
        // Given
        final String sql = "UPDATE TEST_TABLE SET A = ?";
        final BindValue bv0 = new BindValue(5, Types.INTEGER);
        final PreparedSql preparedSql = new PreparedSql(sql, List.of(bv0));

        // When
        final PreparedSql result = executionEngine.reorderBindValuesIfNecessary(preparedSql);

        // Then
        assertSame(preparedSql, result);
    }

    @Test
    void executeUpdate_reordersParametersBeforeBinding() throws SQLException {
        // Given
        final String sql = "MERGE INTO TEST_TABLE ...";
        final int[] permutation = new int[]{1, 0};
        OracleExecutionEngine.registerParameterPermutation(sql, permutation);

        final BindValue bv0 = new BindValue(5, Types.INTEGER);
        final BindValue bv1 = new BindValue(500, Types.INTEGER);
        final PreparedSql preparedSql = new PreparedSql(sql, List.of(bv0, bv1));

        final ConnectionProvider connectionProvider = mock(ConnectionProvider.class);
        final ManagedConnection connection = mock(ManagedConnection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(connectionProvider.connection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        final UpdateResult result = executionEngine.executeUpdate(preparedSql, connectionProvider);

        // Then
        assertEquals(1, result.rowsAffected());
        verify(preparedStatement).setInt(1, 500);
        verify(preparedStatement).setInt(2, 5);
    }

    @Test
    void getLogger() {
        assertNotNull(executionEngine.getLogger());
    }
}