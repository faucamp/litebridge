package org.litebridge.db.sqlite.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.ManagedConnection;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLiteMetaDataEngineTest {

    @Test
    void fetchTableMetaData_whenTableExists_returnsSqliteCompatibleMetadata() throws SQLException {
        // Given
        final SQLiteMetaDataEngine sqLiteMetaDataEngine = new SQLiteMetaDataEngine();
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
        final TableMetaData result = sqLiteMetaDataEngine.fetchTableMetaData(table, mockConnectionProvider);

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
        final SQLiteMetaDataEngine sqLiteMetaDataEngine = new SQLiteMetaDataEngine();
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
                () -> sqLiteMetaDataEngine.fetchTableMetaData(table, mockConnectionProvider)
        );

        // Then
        assertEquals("Table not found: missing_table", exception.getMessage());
        verify(mockDatabaseMetaData, times(1)).getTables(null, null, "missing_table", null);
        verify(mockTables, times(1)).close();
    }

    @Test
    void getColumnMetaData_whenNoColumns_returnsEmptyList() throws SQLException {
        // Given
        final SQLiteMetaDataEngine sqLiteMetaDataEngine = new SQLiteMetaDataEngine();
        final Table table = new Table("catalog", "schema", "empty_table");

        final DatabaseMetaData mockDatabaseMetaData = mock(DatabaseMetaData.class);
        final ResultSet mockColumns = mock(ResultSet.class);

        when(mockDatabaseMetaData.getColumns(null, null, "empty_table", null)).thenReturn(mockColumns);
        when(mockColumns.next()).thenReturn(false);

        // When
        final List<ColumnMetaData> result = sqLiteMetaDataEngine.getColumnMetaData(table, mockDatabaseMetaData);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockDatabaseMetaData, times(1)).getColumns(null, null, "empty_table", null);
        verify(mockColumns, times(1)).close();
    }
}