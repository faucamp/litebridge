package org.litebridge.db.spi.impl.engine;

import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

abstract class AbstractExecutionEngine implements ExecutionEngine {

    private final TypeConverter typeConverter;
    private final AliasTransformer aliasTransformer;

    public AbstractExecutionEngine(final TypeConverter typeConverter, final AliasTransformer aliasTransformer) {
        this.typeConverter = typeConverter;
        this.aliasTransformer = aliasTransformer;
    }

    protected abstract Logger getLogger();

    protected abstract PreparedStatement prepareJdbcStatementReturnGeneratedKeys(final UpdateMetaData updateMetaData,
                                                                                 final PreparedSql preparedSql,
                                                                                 final ManagedConnection connection) throws SQLException;

    @Override
    public InsertResult executeInsert(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, connectionProvider)) {
            final int affectedRows = preparedStatement.executeUpdate();
            final UpdateMetaData updateMetaData = Objects.requireNonNull(preparedSql.updateMetaData());

            if (updateMetaData.returnGeneratedKeys() && affectedRows > 0) {
                final Map<ColumnMetaData, Object> generatedKeys = extractGeneratedKeys(updateMetaData.generatedKeys(), preparedStatement);
                return new InsertResult(affectedRows, generatedKeys);
            } else {
                return new InsertResult(affectedRows);
            }
        }
    }

    @Override
    public UpdateResult executeUpdate(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, connectionProvider)) {
            final int affectedRows = preparedStatement.executeUpdate();
            return new UpdateResult(affectedRows);
        }
    }

    @Override
    public List<Row> executeQuery(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        final Map<String, ColumnMetaData> columnLabelsToColumnMetaData;
        final Map<String, Table> columnAliasesToTable;
        final Class<?>[] typeOverrides;

        if (preparedSql.typeConversionMetaData() != null) {
            columnLabelsToColumnMetaData = preparedSql.typeConversionMetaData().columnLabelsToColumnMetaData();
            columnAliasesToTable = preparedSql.typeConversionMetaData().columnAliasesToTable();
            typeOverrides = preparedSql.typeConversionMetaData().typeOverrides();
        } else {
            columnLabelsToColumnMetaData = Collections.emptyMap();
            columnAliasesToTable = Collections.emptyMap();
            typeOverrides = new Class<?>[0];
        }

        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, connectionProvider)) {
            // Execute SQL query
            final ResultSet resultSet = preparedStatement.executeQuery();

            // Parse results
            final List<Row> rows = new ArrayList<>();

            while (resultSet.next()) {
                final Row row = new Row();
                final int columnCount = resultSet.getMetaData().getColumnCount();
                final Map<String, Table> seenTables = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    final String columnAlias = resultSet.getMetaData().getColumnLabel(i);
                    final String alias = Objects.requireNonNull(aliasTransformer.transformAlias(columnAlias));
                    final ColumnMetaData columnMetaData = columnLabelsToColumnMetaData.get(alias);
                    final int columnSqlType;
                    final Column column;

                    if (columnMetaData != null) {
                        // Use ORM-side metadata
                        columnSqlType = columnMetaData.getDataType();

                        final Table aliasedTable = columnAliasesToTable.get(alias);

                        if (aliasedTable != null) {
                            column = new Column(aliasedTable, columnMetaData.name(), alias);
                        } else {
                            column = columnMetaData.toColumn().as(alias);
                        }
                    } else {
                        // Read the metadata from the result
                        final String schemaName = resultSet.getMetaData().getSchemaName(i);
                        final String tableName = resultSet.getMetaData().getTableName(i);
                        final String columnName = resultSet.getMetaData().getColumnName(i);
                        columnSqlType = resultSet.getMetaData().getColumnType(i);
                        final String qualifiedTableName;

                        if (!StringUtils.isEmpty(tableName)) {
                            if (!StringUtils.isEmpty(schemaName)) {
                                qualifiedTableName = "%s.%s".formatted(schemaName, tableName);
                            } else {
                                qualifiedTableName = tableName;
                            }

                            final Table table = seenTables.computeIfAbsent(qualifiedTableName, key -> new Table(null, schemaName, tableName));
                            column = new Column(table, columnName, columnAlias);
                        } else {
                            column = new Column(columnName, columnAlias);
                        }
                    }

                    final Class<?> typeOverride = i <= typeOverrides.length ? typeOverrides[i - 1] : null;
                    final Object value;

                    if (typeOverride != null) {
                        // Override the data type
                        value = typeConverter.convert(resultSet.getObject(i), typeOverride);
                    } else {
                        // Use the column SQL data type
                        value = typeConverter.convert(resultSet.getObject(i), columnSqlType);
                    }

                    row.withColumn(column, value);
                }

                rows.add(row);
            }

            return rows;
        }
    }

    @Override
    public TypeConverter typeConverter() {
        return typeConverter;
    }

    @Override
    public AliasTransformer aliasTransformer() {
        return aliasTransformer;
    }

    /**
     * Prepare a {@link PreparedStatement} object based on the provided SQL and bind values.
     * <p>
     * Optionally, the statement can be configured to return generated keys.
     *
     * @param preparedSql        the {@link PreparedSql} object containing the SQL query and associated bind values.
     * @param connectionProvider the {@link ConnectionProvider} used to obtain a database connection.
     * @return a {@link PreparedStatement} that is ready to be executed based on the provided SQL and bind values.
     * @throws SQLException if a database access error occurs or the preparation of the SQL statement fails.
     */
    protected PreparedStatement prepareStatement(final PreparedSql preparedSql,
                                                 final ConnectionProvider connectionProvider) throws SQLException {
        if (getLogger().isTraceEnabled() && !CollectionUtils.isEmpty(preparedSql.bindValues())) {
            getLogger().trace("Generated SQL: {} with bind parameters: {}", preparedSql.sql(), preparedSql.bindValues().stream()
                    .filter(Objects::nonNull)
                    .map(bindValue -> bindValue.value() != null ? bindValue.value() : "<null>")
                    .toList());
        } else {
            getLogger().debug("Generated SQL: {}", preparedSql.sql());
        }

        try (ManagedConnection connection = connectionProvider.connection()) {
            final PreparedStatement preparedStatement = prepareJdbcStatement(preparedSql, connection);

            final int[] ordinal = {1};

            if (!CollectionUtils.isEmpty(preparedSql.bindValues())) {
                for (BindValue bindValue : preparedSql.bindValues()) {
                    if (bindValue == null) {
                        preparedStatement.setString(ordinal[0]++, null);
                        continue;
                    } else if (bindValue.value() == null) {
                        preparedStatement.setNull(ordinal[0]++, bindValue.sqlDataType());
                        continue;
                    }

                    if (bindValue.sqlDataType() == Types.BLOB
                            && bindValue.value() instanceof byte[] bytes) {
                        preparedStatement.setBinaryStream(ordinal[0]++, new ByteArrayInputStream(bytes));
                        continue;
                    }

                    switch (bindValue.value()) {
                        case Integer integer -> preparedStatement.setInt(ordinal[0]++, integer);
                        case Long longValue -> preparedStatement.setLong(ordinal[0]++, longValue);
                        case Short shortValue -> preparedStatement.setShort(ordinal[0]++, shortValue);
                        case Double doubleValue -> preparedStatement.setDouble(ordinal[0]++, doubleValue);
                        case Float floatValue -> preparedStatement.setFloat(ordinal[0]++, floatValue);
                        case BigDecimal bigDecimal -> preparedStatement.setBigDecimal(ordinal[0]++, bigDecimal);
                        case Boolean bool -> preparedStatement.setBoolean(ordinal[0]++, bool);
                        case String string -> preparedStatement.setString(ordinal[0]++, string);
                        case Timestamp timestamp -> preparedStatement.setTimestamp(ordinal[0]++, timestamp);
                        case byte[] bytes -> preparedStatement.setBytes(ordinal[0]++, bytes);
                        default ->
                                preparedStatement.setObject(ordinal[0]++, bindValue.value(), bindValue.sqlDataType());
                    }
                }
            }

            return preparedStatement;
        }
    }

    /**
     * Creates a {@link PreparedStatement} using the provided connection and prepared SQL.
     *
     * @param preparedSql the SQL and bind values to use
     * @param connection  the connection to use for preparing the statement
     * @return the created prepared statement
     * @throws SQLException if a database access error occurs
     */
    protected PreparedStatement prepareJdbcStatement(final PreparedSql preparedSql,
                                                     final ManagedConnection connection) throws SQLException {
        final UpdateMetaData updateMetaData = preparedSql.updateMetaData();

        if (updateMetaData == null) {
            return connection.prepareStatement(preparedSql.sql());
        }

        if (updateMetaData.returnGeneratedKeys()) {
            return prepareJdbcStatementReturnGeneratedKeys(updateMetaData, preparedSql, connection);
        } else {
            return connection.prepareStatement(preparedSql.sql());
        }
    }

    /**
     * Extract the generated primary key values from the provided prepared statement.
     *
     * @param generatedPrimaryKeys the list of {@link ColumnMetaData} objects representing the generated primary key columns
     * @param preparedStatement    the executed {@link PreparedStatement} containing any generated keys
     * @return a map of {@link ColumnMetaData} to the generated key value
     * @throws SQLException if an error occurs while retrieving the generated keys
     */
    protected Map<ColumnMetaData, Object> extractGeneratedKeys(final List<ColumnMetaData> generatedPrimaryKeys, final PreparedStatement preparedStatement) throws SQLException {
        final Map<ColumnMetaData, Object> generatedKeys = new HashMap<>(generatedPrimaryKeys.size());

        try (final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys()) {
            while (generatedKeysResultSet.next()) {
                for (ColumnMetaData pkColumn : generatedPrimaryKeys) {
                    final Object generatedId = generatedKeysResultSet.getObject(pkColumn.name());
                    getLogger().debug("Generated ID for column '{}': {}", pkColumn.name(), generatedId);
                    generatedKeys.put(pkColumn, generatedId);
                }
            }

            return generatedKeys;
        }
    }
}
