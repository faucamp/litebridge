package org.litebridge.db.api;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDatabaseProvider implements DatabaseProvider{

    private final Connection connection;

    public AbstractDatabaseProvider(Connection connection) {
        this.connection = connection;
    }

    @Override
    public TableMetaData getTableMetaData(String catalog, String schema, String table) throws SQLException {
        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        final List<String> primaryKeys = getPrimaryKeyColumnNames(catalog, schema, table, databaseMetaData);
        final List<Column> columns = getColumnNames(catalog, schema, table, databaseMetaData);
        return new TableMetaData(table, primaryKeys, columns);
    }

    protected List<Column> getColumnNames(final String catalog, final String schema, final String table, final DatabaseMetaData databaseMetaData) throws SQLException {
        final ResultSet dbColumns = databaseMetaData.getColumns(catalog, schema, table, null);
        final List<Column> columns = new ArrayList<>();

        while (dbColumns.next()) {
            final String name = dbColumns.getString("COLUMN_NAME");
            final boolean nullable = dbColumns.getBoolean("IS_NULLABLE");
            final int dataType = dbColumns.getInt("DATA_TYPE");
            final int size = dbColumns.getInt("COLUMN_SIZE");

            columns.add(new Column(name, nullable, dataType, size));
        }

        dbColumns.close();
        return columns;
    }

    protected List<String> getPrimaryKeyColumnNames(final String catalog, final String schema, final String table, final DatabaseMetaData databaseMetaData) throws SQLException {
        final ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(catalog, schema, table);
        final List<String> primaryKeyColumnNames = new ArrayList<>();

        while (primaryKeys.next()) {
            final String columnName = primaryKeys.getString("COLUMN_NAME");
            primaryKeyColumnNames.add(columnName);
        }

        primaryKeys.close();
        return primaryKeyColumnNames;
    }
}
