package org.litebridge.db.sqlite.engine;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.engine.DefaultMetaDataEngine;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class SQLiteMetaDataEngine extends DefaultMetaDataEngine {

    @Override
    protected TableMetaData fetchTableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
        final List<String> primaryKeys;
        final List<ColumnMetaData> columns;

        try (Connection connection = connectionProvider.connection()) {
            final DatabaseMetaData databaseMetaData = connection.getMetaData();

            // Verify basic details
            verifyTableExists(table, databaseMetaData);

            // Load table metadata using table name only for SQLite compatibility
            final Table tableNoSchema = new Table("", "", table.name());
            primaryKeys = getPrimaryKeyColumnNames(tableNoSchema, databaseMetaData);
            columns = getColumnMetaData(table, databaseMetaData);
        }

        return new TableMetaData(table, primaryKeys, columns);
    }

    @Override
    protected List<ColumnMetaData> getColumnMetaData(final Table table, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (ResultSet rs = databaseMetaData.getColumns(null, null, table.name(), null)) {
            final List<ColumnMetaData> columns = new java.util.ArrayList<>();
            while (rs.next()) {
                final String columnName = rs.getString("COLUMN_NAME");
                final String defaultValue = rs.getString("COLUMN_DEF");
                final int dataType = rs.getInt("DATA_TYPE");
                final String isNullable = rs.getString("IS_NULLABLE");
                final String isAutoincrement = rs.getString("IS_AUTOINCREMENT");
                final int size = rs.getInt("COLUMN_SIZE");
                final int decimalDigits = rs.getInt("DECIMAL_DIGITS");

                columns.add(new ColumnMetaData(table, columnName, "YES".equals(isNullable), dataType, size, decimalDigits, "YES".equals(isAutoincrement), defaultValue, null));
            }
            return columns;
        }
    }

    private void verifyTableExists(final Table table, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (ResultSet tables = databaseMetaData.getTables(null, null, table.name(), null)) {
            if (!tables.next()) {
                throw new IllegalArgumentException("Table not found: " + table.name());
            }
        }
    }
}
