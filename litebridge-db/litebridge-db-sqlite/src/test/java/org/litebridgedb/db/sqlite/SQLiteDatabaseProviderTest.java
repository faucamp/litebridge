package org.litebridgedb.db.sqlite;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.tx.ManagedConnection;
import org.mockito.Mockito;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLiteDatabaseProviderTest {

    @Test
    void createPreparedStatementUsingConnection_withGeneratedKeys() throws SQLException {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        final ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final PreparedSql mockPreparedSql = new PreparedSql("SELECT * FROM test", null);
        final TableMetaData mockTableMetaData = mock(TableMetaData.class);

        when(mockConnection.prepareStatement(mockPreparedSql.sql(), PreparedStatement.RETURN_GENERATED_KEYS))
                .thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(
                mockPreparedSql, true, mockTableMetaData, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1))
                .prepareStatement(mockPreparedSql.sql(), PreparedStatement.RETURN_GENERATED_KEYS);
    }

    @Test
    void createPreparedStatementUsingConnection_withoutGeneratedKeys() throws SQLException {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        final ManagedConnection mockConnection = Mockito.mock(ManagedConnection.class);
        final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        final PreparedSql mockPreparedSql = new PreparedSql("SELECT * FROM test", null);
        final TableMetaData mockTableMetaData = mock(TableMetaData.class);

        when(mockConnection.prepareStatement(mockPreparedSql.sql())).thenReturn(mockPreparedStatement);

        // When
        final PreparedStatement result = provider.createPreparedStatementUsingConnection(
                mockPreparedSql, false, mockTableMetaData, mockConnection);

        // Then
        assertNotNull(result);
        verify(mockConnection, times(1))
                .prepareStatement(mockPreparedSql.sql());
    }

    @Test
    void extractGeneratedKeys() throws SQLException {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        final TableMetaData mockTableMetaData = mock(TableMetaData.class);
        final PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        final ResultSet mockResultSet = mock(ResultSet.class);
        final ColumnMetaData mockColumnMetaData = mock(ColumnMetaData.class);

        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getObject(1)).thenReturn(123L);
        when(mockTableMetaData.primaryKey()).thenReturn(List.of(mockColumnMetaData));
        when(mockColumnMetaData.isAutoIncrement()).thenReturn(true);
        when(mockColumnMetaData.name()).thenReturn("id");

        // When
        final Map<ColumnMetaData, Object> result = provider.extractGeneratedKeys(mockTableMetaData, mockPreparedStatement);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(123L, result.get(mockColumnMetaData));
    }

    @Test
    void extractGeneratedKeys_whenNoGeneratedKeysRow_returnsEmptyMap() throws SQLException {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        final TableMetaData mockTableMetaData = mock(TableMetaData.class);
        final PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        final ResultSet mockResultSet = mock(ResultSet.class);
        final ColumnMetaData mockColumnMetaData = mock(ColumnMetaData.class);

        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        when(mockTableMetaData.primaryKey()).thenReturn(List.of(mockColumnMetaData));

        // When
        final Map<ColumnMetaData, Object> result = provider.extractGeneratedKeys(mockTableMetaData, mockPreparedStatement);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockResultSet, times(1)).close();
    }

    @Test
    void fetchTableMetaData_whenTableExists_returnsSqliteCompatibleMetadata() throws SQLException {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        final Table table = new Table("catalog", "schema", "test");

        final ConnectionProvider mockConnectionProvider = mock(ConnectionProvider.class);
        final ManagedConnection mockConnection = mock(ManagedConnection.class);
        final DatabaseMetaData mockDatabaseMetaData = mock(DatabaseMetaData.class);
        final ResultSet mockTables = mock(ResultSet.class);
        final ResultSet mockPrimaryKeys = mock(ResultSet.class);
        final ResultSet mockColumns = mock(ResultSet.class);

        when(mockConnectionProvider.connection()).thenReturn(mockConnection);
        when(mockConnection.getMetaData()).thenReturn(mockDatabaseMetaData);

        when(mockDatabaseMetaData.getTables(null, null, "test", null)).thenReturn(mockTables);
        when(mockTables.next()).thenReturn(true);

        when(mockDatabaseMetaData.getPrimaryKeys(null, null, "test")).thenReturn(mockPrimaryKeys);
        when(mockPrimaryKeys.next()).thenReturn(true, false);
        when(mockPrimaryKeys.getString("COLUMN_NAME")).thenReturn("id");

        when(mockDatabaseMetaData.getColumns(null, null, "test", null)).thenReturn(mockColumns);
        when(mockColumns.next()).thenReturn(true, true, false);
        when(mockColumns.getString("COLUMN_NAME")).thenReturn("id", "name");
        when(mockColumns.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        when(mockColumns.getString("IS_NULLABLE")).thenReturn("NO", "YES");
        when(mockColumns.getString("IS_AUTOINCREMENT")).thenReturn("YES", "NO");
        when(mockColumns.getInt("COLUMN_SIZE")).thenReturn(10, 255);
        when(mockColumns.getInt("DECIMAL_DIGITS")).thenReturn(0, 0);

        // When
        final TableMetaData result = provider.fetchTableMetaData(table, mockConnectionProvider);

        // Then
        assertNotNull(result);
        assertEquals("catalog", result.catalog());
        assertEquals("schema", result.schema());
        assertEquals("test", result.name());
        assertEquals(2, result.columns().size());
        assertEquals(1, result.primaryKey().size());

        final ColumnMetaData idColumn = result.column("id");
        assertEquals("id", idColumn.name());
        assertFalse(idColumn.isNullable());
        assertEquals(Types.INTEGER, idColumn.getDataType());
        assertEquals(10, idColumn.getSize());
        assertEquals(0, idColumn.getDecimalDigits());
        assertTrue(idColumn.isAutoIncrement());

        final ColumnMetaData nameColumn = result.column("name");
        assertEquals("name", nameColumn.name());
        assertTrue(nameColumn.isNullable());
        assertEquals(Types.VARCHAR, nameColumn.getDataType());
        assertEquals(255, nameColumn.getSize());
        assertEquals(0, nameColumn.getDecimalDigits());
        assertFalse(nameColumn.isAutoIncrement());

        verify(mockDatabaseMetaData, times(1)).getTables(null, null, "test", null);
        verify(mockDatabaseMetaData, times(1)).getPrimaryKeys(null, null, "test");
        verify(mockDatabaseMetaData, times(1)).getColumns(null, null, "test", null);
        verify(mockTables, times(1)).close();
        verify(mockPrimaryKeys, times(1)).close();
        verify(mockColumns, times(1)).close();
    }

    @Test
    void fetchTableMetaData_whenTableDoesNotExist_throwsIllegalArgumentException() throws SQLException {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        final Table table = new Table("catalog", "schema", "missing_table");

        final ConnectionProvider mockConnectionProvider = mock(ConnectionProvider.class);
        final ManagedConnection mockConnection = mock(ManagedConnection.class);
        final DatabaseMetaData mockDatabaseMetaData = mock(DatabaseMetaData.class);
        final ResultSet mockTables = mock(ResultSet.class);

        when(mockConnectionProvider.connection()).thenReturn(mockConnection);
        when(mockConnection.getMetaData()).thenReturn(mockDatabaseMetaData);
        when(mockDatabaseMetaData.getTables(null, null, "missing_table", null)).thenReturn(mockTables);
        when(mockTables.next()).thenReturn(false);

        // When
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> provider.fetchTableMetaData(table, mockConnectionProvider)
        );

        // Then
        assertEquals("Table not found: missing_table", exception.getMessage());
        verify(mockDatabaseMetaData, times(1)).getTables(null, null, "missing_table", null);
        verify(mockTables, times(1)).close();
    }

    @Test
    void getColumnMetaData_whenNoColumns_returnsEmptyList() throws SQLException {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();
        final Table table = new Table("catalog", "schema", "empty_table");

        final DatabaseMetaData mockDatabaseMetaData = mock(DatabaseMetaData.class);
        final ResultSet mockColumns = mock(ResultSet.class);

        when(mockDatabaseMetaData.getColumns(null, null, "empty_table", null)).thenReturn(mockColumns);
        when(mockColumns.next()).thenReturn(false);

        // When
        final List<ColumnMetaData> result = provider.getColumnMetaData(table, mockDatabaseMetaData);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockDatabaseMetaData, times(1)).getColumns(null, null, "empty_table", null);
        verify(mockColumns, times(1)).close();
    }

    @Test
    void getSequenceColumnValueGenerator_alwaysThrowsUnsupportedOperationException() {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();

        // When
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> provider.getSequenceColumnValueGenerator("test_sequence")
        );

        // Then
        assertEquals("SQLite does not support sequences", exception.getMessage());
    }

    @Test
    void getLogger() {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();

        assertNotNull(provider.getLogger());
    }
}
