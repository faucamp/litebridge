package org.litebridge.db.spi.impl.engine;

import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.commons.type.ConcurrentLazyFunction;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseMetaData;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.ForeignKeyConstraint;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMetaDataEngine implements MetaDataEngine {

    private static final String[] TYPES_TABLE = {"TABLE"};

    private final ConcurrentLazy<DatabaseProviderMetaData> databaseProviderMetaData = new ConcurrentLazy<>(this::createDatabaseProviderMetaData);
    private final ConcurrentLazyFunction<ConnectionProvider, DatabaseMetaData> databaseMetaData = new ConcurrentLazyFunction<>(this::createDatabaseMetaData);

    /**
     * Map of qualified table name -> table metadata.
     */
    private final Map<String, TableMetaData> tableMetaDataCache = new ConcurrentHashMap<>();

    @Override
    public DatabaseProviderMetaData metaData() {
        return databaseProviderMetaData.getOrThrow();
    }

    @Override
    public DatabaseMetaData databaseMetaData(final ConnectionProvider connectionProvider) {
        return databaseMetaData.getOrThrow(connectionProvider);
    }

    @Override
    public TableMetaData ensureTableMetaData(final Table table, final ConnectionProvider connectionProvider) {
        TableMetaData tableMetaData = this.tableMetaDataCache.get(table.qualifiedName());

        if (tableMetaData == null) {
            try {
                tableMetaData = fetchTableMetaData(table, connectionProvider);
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to get table metadata for table: " + table, ex);
            }

            tableMetaDataCache.put(table.qualifiedName(), tableMetaData);
        }

        return tableMetaData;
    }

    /**
     * Retrieve metadata for the specified table, including its primary keys and expressions.
     * <p>
     * This executes a database query to fetch database metadata.
     *
     * @param table              the table for which metadata is being fetched, containing schema, catalog, and table name details
     * @param connectionProvider the {@link ConnectionProvider} used to obtain a database connection.
     * @return a {@code TableMetaData} object containing details about the table's structure, primary keys, and column metadata
     * @throws SQLException if an error occurs while fetching database metadata
     */
    protected TableMetaData fetchTableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
        final List<String> primaryKeys;
        final List<ColumnMetaData> columns;

        try (final Connection connection = connectionProvider.connection()) {
            final java.sql.DatabaseMetaData databaseMetaData = connection.getMetaData();

            // Verify basic details
            verifySchemaAndTableExists(table, databaseMetaData);

            // Load table metadata
            primaryKeys = getPrimaryKeyColumnNames(table, databaseMetaData);
            columns = getColumnMetaData(table, databaseMetaData);

            try (ResultSet rs = databaseMetaData.getImportedKeys(table.catalog(), table.schema(), table.name())) {
                while (rs.next()) {
                    final String fkName = rs.getString("FK_NAME");

                    // Remote table
                    final String pkTable = rs.getString("PKTABLE_NAME");
                    final String pkColumn = rs.getString("PKCOLUMN_NAME");

                    // Local table
                    final String fkTable = rs.getString("FKTABLE_NAME");
                    final String fkColumn = rs.getString("FKCOLUMN_NAME");

                    if (table.name().equals(fkTable)) {
                        columns.stream()
                                .filter(column -> column.name().equals(fkColumn))
                                .forEach(column -> {
                                    final ForeignKeyConstraint constraint = new ForeignKeyConstraint(fkName, new Column(new Table(table.catalog(), table.schema(), pkTable), pkColumn));
                                    column.addForeignKeyConstraint(constraint);
                                });
                    }
                }
            }

            try (ResultSet rs = databaseMetaData.getExportedKeys(table.catalog(), table.schema(), table.name())) {
                while (rs.next()) {
                    final String fkName = rs.getString("FK_NAME");

                    // Local table
                    final String pkTable = rs.getString("PKTABLE_NAME");
                    final String pkColumn = rs.getString("PKCOLUMN_NAME");

                    // Remote table
                    final String fkTable = rs.getString("FKTABLE_NAME");
                    final String fkColumn = rs.getString("FKCOLUMN_NAME");

                    if (table.name().equals(pkTable)) {
                        columns.stream()
                                .filter(column -> column.name().equals(pkColumn))
                                .forEach(column -> {
                                    final ForeignKeyConstraint constraint = new ForeignKeyConstraint(fkName, new Column(new Table(table.catalog(), table.schema(), fkTable), fkColumn));
                                    column.addForeignReference(constraint);
                                });
                    }
                }
            }
        }

        return new TableMetaData(table, primaryKeys, columns);
    }

    /**
     * Verify that the specified schema and table exist in the database.
     *
     * @param table            the {@link Table} object representing the table to verify
     * @param databaseMetaData the {@link java.sql.DatabaseMetaData} object used to perform the verification
     * @throws SQLException             if an error occurs while performing the verification
     * @throws IllegalArgumentException if the schema or table is not found
     */
    protected static void verifySchemaAndTableExists(final Table table, final java.sql.DatabaseMetaData databaseMetaData) throws SQLException {
        final ResultSet schemas = databaseMetaData.getSchemas(table.catalog(), table.schema());
        boolean schemaExists = false;

        while (schemas.next()) {
            if (Objects.equals(table.schema(), schemas.getString("TABLE_SCHEM"))) {
                schemaExists = true;
                break;
            }
        }

        if (!schemaExists) {
            throw new IllegalArgumentException("Schema not found: " + table.schema());
        }

        final ResultSet tables = databaseMetaData.getTables(table.catalog(), table.schema(), table.name(), TYPES_TABLE);
        boolean tableExists = false;

        while (tables.next()) {
            if (Objects.equals(table.name(), tables.getString("TABLE_NAME"))) {
                tableExists = true;
                break;
            }
        }

        if (!tableExists) {
            throw new IllegalArgumentException("Table not found: " + table);
        }
    }

    /**
     * Retrieve column metadata for the specified table.
     *
     * @param table            the {@link Table} object representing the table
     * @param databaseMetaData the {@link java.sql.DatabaseMetaData} object used to retrieve column information
     * @return a list of {@link ColumnMetaData} objects representing the table's columns
     * @throws SQLException if an error occurs while retrieving column metadata
     */
    protected List<ColumnMetaData> getColumnMetaData(final Table table, final java.sql.DatabaseMetaData databaseMetaData) throws SQLException {
        try (final ResultSet dbColumns = databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)) {
            final List<ColumnMetaData> columns = new ArrayList<>();

            while (dbColumns.next()) {
                final String name = dbColumns.getString("COLUMN_NAME");
                final boolean nullable = dbColumns.getBoolean("IS_NULLABLE");
                final int dataType = dbColumns.getInt("DATA_TYPE");
                final int size = dbColumns.getInt("COLUMN_SIZE");
                final boolean isAutoincrement = dbColumns.getBoolean("IS_AUTOINCREMENT");
                final int decimalDigits = dbColumns.getInt("DECIMAL_DIGITS");
                String defaultValue = dbColumns.getString("COLUMN_DEF");

                // Remove the enclosing ' character from string defaults
                if (defaultValue != null
                        && defaultValue.length() >= 2
                        && (dataType == Types.VARCHAR || dataType == Types.CHAR || dataType == Types.LONGVARCHAR)
                        && defaultValue.charAt(0) == '\''
                        && defaultValue.charAt(defaultValue.length() - 1) == '\'') {
                    defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                }

                columns.add(new ColumnMetaData(table, name, nullable, dataType, size, decimalDigits, isAutoincrement, defaultValue, null));
            }

            return columns;
        }
    }

    /**
     * Retrieve the names of the primary key columns for the specified table.
     *
     * @param table            the {@link Table} object representing the table
     * @param databaseMetaData the {@link java.sql.DatabaseMetaData} object used to retrieve primary key information
     * @return a list of primary key column names
     * @throws SQLException if an error occurs while retrieving primary key information
     */
    protected List<String> getPrimaryKeyColumnNames(final Table table, final java.sql.DatabaseMetaData databaseMetaData) throws SQLException {
        try (final ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())) {
            final List<String> primaryKeyColumnNames = new ArrayList<>();

            while (primaryKeys.next()) {
                final String columnName = primaryKeys.getString("COLUMN_NAME");
                primaryKeyColumnNames.add(columnName);
            }

            return primaryKeyColumnNames;
        }
    }

    protected DatabaseProviderMetaData createDatabaseProviderMetaData() {
        return new DatabaseProviderMetaData(true);
    }

    protected DatabaseMetaData createDatabaseMetaData(final ConnectionProvider connectionProvider) {
        try (final Connection connection = connectionProvider.connection()) {
            final java.sql.DatabaseMetaData databaseMetaData = connection.getMetaData();
            return new DatabaseMetaData(
                    new DatabaseMetaData.Component(databaseMetaData.getDatabaseProductName(),
                            databaseMetaData.getDatabaseProductVersion(),
                            databaseMetaData.getDatabaseMajorVersion(),
                            databaseMetaData.getDatabaseMinorVersion()),
                    new DatabaseMetaData.Component(databaseMetaData.getDriverName(),
                            databaseMetaData.getDriverVersion(),
                            databaseMetaData.getDriverMajorVersion(),
                            databaseMetaData.getDriverMinorVersion()));
        } catch (SQLException sqlException) {
            throw new IllegalStateException(sqlException);
        }
    }
}
