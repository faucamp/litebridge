package org.litebridge.db.sqlite.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLiteExecutionEngineTest {

    private final TypeConverter typeConverter = new DefaultTypeConverter();
    private final AliasTransformer aliasTransformer = new UppercaseAliasTransformer();
    private final SQLiteExecutionEngine executionEngine = new SQLiteExecutionEngine(typeConverter, aliasTransformer);

    @Test
    void extractGeneratedKeys() throws SQLException {
        // Given
        final PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        final ResultSet mockResultSet = mock(ResultSet.class);
        final ColumnMetaData mockColumnMetaData = mock(ColumnMetaData.class);

        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getObject(1)).thenReturn(123L);
        when(mockColumnMetaData.isAutoIncrement()).thenReturn(true);
        when(mockColumnMetaData.name()).thenReturn("id");

        // When
        final Map<ColumnMetaData, Object> result = executionEngine.extractGeneratedKeys(List.of(mockColumnMetaData), mockPreparedStatement);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(123L, result.get(mockColumnMetaData));
    }

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

    @Test
    void extractGeneratedKeys_whenNoGeneratedKeysRow_returnsEmptyMap() throws SQLException {
        // Given
        final PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        final ResultSet mockResultSet = mock(ResultSet.class);
        final ColumnMetaData mockColumnMetaData = mock(ColumnMetaData.class);

        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // When
        final Map<ColumnMetaData, Object> result = executionEngine.extractGeneratedKeys(List.of(mockColumnMetaData), mockPreparedStatement);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void getLogger() {
        assertNotNull(executionEngine.getLogger());
    }
}