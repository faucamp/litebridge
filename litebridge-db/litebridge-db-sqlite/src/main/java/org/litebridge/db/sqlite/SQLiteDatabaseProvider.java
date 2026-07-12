package org.litebridge.db.sqlite;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite Database Provider for Litebridge.
 * <p>
 * This class provides SQLite-specific implementations for database interactions.
 */
public final class SQLiteDatabaseProvider extends AbstractDatabaseProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteDatabaseProvider.class);

    public SQLiteDatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    @Override
    protected PreparedStatement createPreparedStatementUsingConnection(final PreparedSql preparedSql,
                                                                       final boolean returnGeneratedKeys,
                                                                       final TableMetaData tableMetaData,
                                                                       final ManagedConnection connection) throws SQLException {
        if (returnGeneratedKeys) {
            return connection.prepareStatement(preparedSql.sql(), Statement.RETURN_GENERATED_KEYS);
        } else {
            return connection.prepareStatement(preparedSql.sql());
        }
    }

    @Override
    protected Map<ColumnMetaData, Object> extractGeneratedKeys(final TableMetaData tableMetaData, final PreparedStatement preparedStatement) throws SQLException {
        final Map<ColumnMetaData, Object> generatedKeys = new HashMap<>(tableMetaData.primaryKey().size());
        try (final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys()) {
            if (generatedKeysResultSet.next()) {
                // SQLite usually returns one generated key (rowid)
                int generatedKeyIndex = 1;
                for (ColumnMetaData pkColumn : getGeneratedPrimaryKeyColumns(tableMetaData)) {
                    final Object generatedId = generatedKeysResultSet.getObject(generatedKeyIndex++);
                    getLogger().debug("Generated ID for column '{}': {}", pkColumn.name(), generatedId);
                    generatedKeys.put(pkColumn, generatedId);
                }
            }
        }
        return generatedKeys;
    }

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
                final int dataType = rs.getInt("DATA_TYPE");
                final String isNullable = rs.getString("IS_NULLABLE");
                final String isAutoincrement = rs.getString("IS_AUTOINCREMENT");
                final int size = rs.getInt("COLUMN_SIZE");
                final int decimalDigits = rs.getInt("DECIMAL_DIGITS");

                columns.add(new ColumnMetaData(table, columnName, "YES".equals(isNullable), dataType, size, decimalDigits, "YES".equals(isAutoincrement), null));
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

    /**
     * SQLite does not support sequences. Throws an {@code UnsupportedOperationException} if called.
     *
     * @param sequence the sequence name
     * @return N/A; throws an {@code UnsupportedOperationException}
     * @throws UnsupportedOperationException if called
     */
    @Override
    public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("SQLite does not support sequences");
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
