package org.litebridge.db.spi.impl.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseMetaData;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.ManagedConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultMetaDataEngineTest {

    private static final DatabaseProviderMetaData metadata =
            new DatabaseProviderMetaData(true, DatabaseProviderMetaData.MergeCapability.USING_VALUES, DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW);

    @Test
    void metaData() {
        // Given
        final DefaultMetaDataEngine engine = new DefaultMetaDataEngine(metadata);

        // When
        final DatabaseProviderMetaData result = engine.metaData();

        // Then
        assertSame(metadata, result);
    }

    @Test
    void databaseMetaData_cachedValue() throws Exception {
        // Given
        final DefaultMetaDataEngine engine = new DefaultMetaDataEngine(metadata);
        final ConnectionProvider provider = mock(ConnectionProvider.class);
        final Connection connection = mock(Connection.class);
        final java.sql.DatabaseMetaData jdbcMetaData = mock(java.sql.DatabaseMetaData.class);
        when(provider.connection()).thenReturn(new ManagedConnection(connection));
        when(connection.getMetaData()).thenReturn(jdbcMetaData);
        when(jdbcMetaData.getDatabaseProductName()).thenReturn("Database");
        when(jdbcMetaData.getDatabaseProductVersion()).thenReturn("1");
        when(jdbcMetaData.getDatabaseMajorVersion()).thenReturn(1);
        when(jdbcMetaData.getDatabaseMinorVersion()).thenReturn(2);
        when(jdbcMetaData.getDriverName()).thenReturn("Driver");
        when(jdbcMetaData.getDriverVersion()).thenReturn("2");
        when(jdbcMetaData.getDriverMajorVersion()).thenReturn(3);
        when(jdbcMetaData.getDriverMinorVersion()).thenReturn(4);

        // When
        final DatabaseMetaData first = engine.databaseMetaData(provider);
        final DatabaseMetaData second = engine.databaseMetaData(provider);

        // Then
        assertSame(first, second);
        assertEquals("Database", first.database().name());
        assertEquals("Driver", first.driver().name());
    }

    @Test
    void databaseMetaData_sqlException() throws Exception {
        // Given
        final DefaultMetaDataEngine engine = new DefaultMetaDataEngine(metadata);
        final ConnectionProvider provider = mock(ConnectionProvider.class);
        when(provider.connection()).thenThrow(new SQLException("connection"));

        // When
        final IllegalStateException result = assertThrows(IllegalStateException.class, () -> engine.databaseMetaData(provider));

        // Then
        assertEquals(SQLException.class, result.getCause().getClass());
    }

    @Test
    void ensureTableMetaData_cachedValue() throws Exception {
        // Given
        final DefaultMetaDataEngine engine = new DefaultMetaDataEngine(metadata);
        final Table table = new Table("CATALOG", "SCHEMA", "TEST_TABLE");
        final ConnectionProvider provider = configuredProvider(table);

        // When
        final TableMetaData first = engine.ensureTableMetaData(table, provider);
        final TableMetaData second = engine.ensureTableMetaData(table, provider);

        // Then
        assertSame(first, second);
        assertEquals(1, first.primaryKey().size());
        assertEquals("ID", first.primaryKey().getFirst().name());
        assertEquals("default", first.column("NAME").getDefaultValue());
        assertEquals(1, first.column("ID").getForeignKeyConstraints().size());
        assertEquals(1, first.column("ID").getForeignReferences().size());
    }

    @Test
    void ensureTableMetaData_schemaNotFound() throws Exception {
        // Given
        final DefaultMetaDataEngine engine = new DefaultMetaDataEngine(metadata);
        final Table table = new Table("CATALOG", "SCHEMA", "TEST_TABLE");
        final Connection connection = mock(Connection.class);
        final java.sql.DatabaseMetaData metadata = mock(java.sql.DatabaseMetaData.class);
        final ResultSet schemas = mock(ResultSet.class);
        when(schemas.next()).thenReturn(false);
        when(metadata.getSchemas(table.catalog(), table.schema())).thenReturn(schemas);
        when(connection.getMetaData()).thenReturn(metadata);
        final ConnectionProvider provider = mock(ConnectionProvider.class);
        when(provider.connection()).thenReturn(new ManagedConnection(connection));

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> engine.ensureTableMetaData(table, provider));

        // Then
        assertEquals("Schema not found: SCHEMA", result.getMessage());
    }

    @Test
    void ensureTableMetaData_tableNotFound() throws Exception {
        // Given
        final DefaultMetaDataEngine engine = new DefaultMetaDataEngine(metadata);
        final Table table = new Table("CATALOG", "SCHEMA", "TEST_TABLE");
        final Connection connection = mock(Connection.class);
        final java.sql.DatabaseMetaData metadata = mock(java.sql.DatabaseMetaData.class);
        final ResultSet schemas = mock(ResultSet.class);
        final ResultSet tables = mock(ResultSet.class);
        when(schemas.next()).thenReturn(true);
        when(schemas.getString("TABLE_SCHEM")).thenReturn("SCHEMA");
        when(tables.next()).thenReturn(false);
        when(metadata.getSchemas(table.catalog(), table.schema())).thenReturn(schemas);
        when(metadata.getTables(table.catalog(), table.schema(), table.name(), new String[]{"TABLE"})).thenReturn(tables);
        when(connection.getMetaData()).thenReturn(metadata);
        final ConnectionProvider provider = mock(ConnectionProvider.class);
        when(provider.connection()).thenReturn(new ManagedConnection(connection));

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> engine.ensureTableMetaData(table, provider));

        // Then
        assertEquals("Table not found: " + table, result.getMessage());
    }

    @Test
    void ensureTableMetaData_sqlException_wrappedInIllegalStateException() throws Exception {
        // Given
        final DefaultMetaDataEngine engine = new DefaultMetaDataEngine(metadata);
        final Table table = new Table("CATALOG", "SCHEMA", "TEST_TABLE");
        final Connection connection = mock(Connection.class);
        when(connection.getMetaData()).thenThrow(new SQLException("meta error"));
        final ConnectionProvider provider = mock(ConnectionProvider.class);
        when(provider.connection()).thenReturn(new ManagedConnection(connection));

        // When
        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> engine.ensureTableMetaData(table, provider));

        // Then
        assertEquals("Failed to get table metadata for table: " + table, ex.getMessage());
    }

    @Test
    void ensureTableMetaData_columnDefaultValueVariationsAndKeyMismatches() throws Exception {
        // Given
        final DefaultMetaDataEngine engine = new DefaultMetaDataEngine(metadata);
        final Table table = new Table("CATALOG", "SCHEMA", "TEST_TABLE");

        final Connection connection = mock(Connection.class);
        final java.sql.DatabaseMetaData jdbcMeta = mock(java.sql.DatabaseMetaData.class);
        final ResultSet schemas = resultSet(true, "TABLE_SCHEM", "SCHEMA");
        final ResultSet tables = resultSet(true, "TABLE_NAME", "TEST_TABLE");
        final ResultSet primaryKeys = resultSet(false, "COLUMN_NAME", "");

        final ResultSet columns = mock(ResultSet.class);
        when(columns.next()).thenReturn(true, true, true, true, true, true, false);
        when(columns.getString("COLUMN_NAME")).thenReturn("C_CHAR", "C_LONGVAR", "C_INT", "C_SHORT", "C_START_NOQ", "C_END_NOQ");
        when(columns.getBoolean("IS_NULLABLE")).thenReturn(false, true, true, true, true, true);
        when(columns.getInt("DATA_TYPE")).thenReturn(Types.CHAR, Types.LONGVARCHAR, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR);
        when(columns.getInt("COLUMN_SIZE")).thenReturn(1, 1000, 10, 5, 10, 10);
        when(columns.getBoolean("IS_AUTOINCREMENT")).thenReturn(false, false, false, false, false, false);
        when(columns.getInt("DECIMAL_DIGITS")).thenReturn(0, 0, 0, 0, 0, 0);
        when(columns.getString("COLUMN_DEF")).thenReturn("'X'", "'longtext'", "0", "a", "abc'", "'abc");

        final ResultSet importedKeys = mock(ResultSet.class);
        when(importedKeys.next()).thenReturn(true, false);
        when(importedKeys.getString("FK_NAME")).thenReturn("FK_OTHER");
        when(importedKeys.getString("PKTABLE_NAME")).thenReturn("PARENT");
        when(importedKeys.getString("PKCOLUMN_NAME")).thenReturn("ID");
        when(importedKeys.getString("FKTABLE_NAME")).thenReturn("OTHER_TABLE"); // doesn't match table name
        when(importedKeys.getString("FKCOLUMN_NAME")).thenReturn("ID");

        final ResultSet exportedKeys = mock(ResultSet.class);
        when(exportedKeys.next()).thenReturn(true, false);
        when(exportedKeys.getString("FK_NAME")).thenReturn("FK_OTHER");
        when(exportedKeys.getString("PKTABLE_NAME")).thenReturn("OTHER_TABLE"); // doesn't match table name
        when(exportedKeys.getString("PKCOLUMN_NAME")).thenReturn("ID");
        when(exportedKeys.getString("FKTABLE_NAME")).thenReturn("CHILD");
        when(exportedKeys.getString("FKCOLUMN_NAME")).thenReturn("PARENT_ID");

        when(jdbcMeta.getSchemas(table.catalog(), table.schema())).thenReturn(schemas);
        when(jdbcMeta.getTables(eq(table.catalog()), eq(table.schema()), eq(table.name()), any(String[].class))).thenReturn(tables);
        when(jdbcMeta.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(primaryKeys);
        when(jdbcMeta.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columns);
        when(jdbcMeta.getImportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(importedKeys);
        when(jdbcMeta.getExportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(exportedKeys);
        when(connection.getMetaData()).thenReturn(jdbcMeta);

        final ConnectionProvider provider = mock(ConnectionProvider.class);
        when(provider.connection()).thenReturn(new ManagedConnection(connection));

        // When
        final TableMetaData meta = engine.ensureTableMetaData(table, provider);

        // Then
        assertEquals("X", meta.column("C_CHAR").getDefaultValue());
        assertEquals("longtext", meta.column("C_LONGVAR").getDefaultValue());
        assertEquals("0", meta.column("C_INT").getDefaultValue());
        assertEquals("a", meta.column("C_SHORT").getDefaultValue());
        assertEquals("abc'", meta.column("C_START_NOQ").getDefaultValue());
        assertEquals("'abc", meta.column("C_END_NOQ").getDefaultValue());
    }

    @Test
    void ensureTableMetaData_skipsNonMatchingSchemaAndTableRowsFirst() throws Exception {
        // Given
        final DefaultMetaDataEngine engine = new DefaultMetaDataEngine(metadata);
        final Table table = new Table("CATALOG", "SCHEMA", "TEST_TABLE");

        final Connection connection = mock(Connection.class);
        final java.sql.DatabaseMetaData jdbcMeta = mock(java.sql.DatabaseMetaData.class);

        final ResultSet schemas = mock(ResultSet.class);
        when(schemas.next()).thenReturn(true, true, false);
        when(schemas.getString("TABLE_SCHEM")).thenReturn("OTHER_SCHEMA", "SCHEMA");

        final ResultSet tables = mock(ResultSet.class);
        when(tables.next()).thenReturn(true, true, false);
        when(tables.getString("TABLE_NAME")).thenReturn("OTHER_TABLE", "TEST_TABLE");

        final ResultSet primaryKeys = resultSet(false, "COLUMN_NAME", "");
        final ResultSet columns = resultSet(false, "COLUMN_NAME", "");
        final ResultSet importedKeys = resultSet(false, "FK_NAME", "");
        final ResultSet exportedKeys = resultSet(false, "FK_NAME", "");

        when(jdbcMeta.getSchemas(table.catalog(), table.schema())).thenReturn(schemas);
        when(jdbcMeta.getTables(eq(table.catalog()), eq(table.schema()), eq(table.name()), any(String[].class))).thenReturn(tables);
        when(jdbcMeta.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(primaryKeys);
        when(jdbcMeta.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columns);
        when(jdbcMeta.getImportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(importedKeys);
        when(jdbcMeta.getExportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(exportedKeys);
        when(connection.getMetaData()).thenReturn(jdbcMeta);

        final ConnectionProvider provider = mock(ConnectionProvider.class);
        when(provider.connection()).thenReturn(new ManagedConnection(connection));

        // When
        final TableMetaData meta = engine.ensureTableMetaData(table, provider);

        // Then
        assertNotNull(meta);
    }

    private static ConnectionProvider configuredProvider(final Table table) throws Exception {
        final Connection connection = mock(Connection.class);
        final java.sql.DatabaseMetaData metadata = mock(java.sql.DatabaseMetaData.class);
        final ResultSet schemas = resultSet(true, "TABLE_SCHEM", "SCHEMA");
        final ResultSet tables = resultSet(true, "TABLE_NAME", "TEST_TABLE");
        final ResultSet primaryKeys = resultSet(true, "COLUMN_NAME", "ID");
        final ResultSet columns = mock(ResultSet.class);
        final ResultSet importedKeys = mock(ResultSet.class);
        final ResultSet exportedKeys = mock(ResultSet.class);
        when(schemas.next()).thenReturn(true, false);
        when(tables.next()).thenReturn(true, false);
        when(primaryKeys.next()).thenReturn(true, false);
        when(columns.next()).thenReturn(true, true, false);
        when(columns.getString("COLUMN_NAME")).thenReturn("ID", "NAME");
        when(columns.getBoolean("IS_NULLABLE")).thenReturn(false, true);
        when(columns.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        when(columns.getInt("COLUMN_SIZE")).thenReturn(10, 100);
        when(columns.getBoolean("IS_AUTOINCREMENT")).thenReturn(true, false);
        when(columns.getInt("DECIMAL_DIGITS")).thenReturn(0, 0);
        when(columns.getString("COLUMN_DEF")).thenReturn(null, "'default'");
        when(importedKeys.next()).thenReturn(true, false);
        when(importedKeys.getString("FK_NAME")).thenReturn("FK_PARENT");
        when(importedKeys.getString("PKTABLE_NAME")).thenReturn("PARENT");
        when(importedKeys.getString("PKCOLUMN_NAME")).thenReturn("ID");
        when(importedKeys.getString("FKTABLE_NAME")).thenReturn("TEST_TABLE");
        when(importedKeys.getString("FKCOLUMN_NAME")).thenReturn("ID");
        when(exportedKeys.next()).thenReturn(true, false);
        when(exportedKeys.getString("FK_NAME")).thenReturn("FK_CHILD");
        when(exportedKeys.getString("PKTABLE_NAME")).thenReturn("TEST_TABLE");
        when(exportedKeys.getString("PKCOLUMN_NAME")).thenReturn("ID");
        when(exportedKeys.getString("FKTABLE_NAME")).thenReturn("CHILD");
        when(exportedKeys.getString("FKCOLUMN_NAME")).thenReturn("PARENT_ID");
        when(metadata.getSchemas(table.catalog(), table.schema())).thenReturn(schemas);
        when(metadata.getTables(eq(table.catalog()), eq(table.schema()), eq(table.name()), any(String[].class))).thenReturn(tables);
        when(metadata.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(primaryKeys);
        when(metadata.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columns);
        when(metadata.getImportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(importedKeys);
        when(metadata.getExportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(exportedKeys);
        when(connection.getMetaData()).thenReturn(metadata);
        final ConnectionProvider provider = mock(ConnectionProvider.class);
        when(provider.connection()).thenReturn(new ManagedConnection(connection));
        return provider;
    }

    private static ResultSet resultSet(final boolean next, final String column, final String value) throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(next, false);
        when(resultSet.getString(column)).thenReturn(value);
        return resultSet;
    }
}