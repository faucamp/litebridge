package org.litebridge.db.api;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.api.convert.TypeConverter;
import org.litebridge.db.api.query.Condition;
import org.litebridge.db.api.query.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractDatabaseProvider implements DatabaseProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseProvider.class);
    private final Connection connection;
    private final TypeConverter typeConverter;

    public AbstractDatabaseProvider(final Connection connection,
                                    final TypeConverter typeConverter) {
        this.connection = connection;
        this.typeConverter = ObjectUtils.requireNonNull(typeConverter, "No TypeConverter provided");
    }

    @Override
    public TableMetaData getTableMetaData(final String catalog, final String schema, final String table) throws SQLException {
        final DatabaseMetaData databaseMetaData = connection.getMetaData();

        // Verify basic details
        verifySchemaAndTableExists(catalog, schema, table, databaseMetaData);

        // Load table metadata
        final List<String> primaryKeys = getPrimaryKeyColumnNames(catalog, schema, table, databaseMetaData);
        final List<Column> columns = getColumnNames(catalog, schema, table, databaseMetaData);
        return new TableMetaData(catalog, schema, table, primaryKeys, columns);
    }

    @Override
    public @Nullable List<Object> insert(final TableMetaData tableMetaData, final Map<String, Object> columnValueMap) throws SQLException {
        final StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableMetaData.getSchema())
                .append('.')
                .append(tableMetaData.getTable())
                .append(" (")
                .append(String.join(", ", columnValueMap.keySet()))
                .append(") VALUES (");

        final Iterator<Map.Entry<String, Object>> columnValueIterator = columnValueMap.entrySet().iterator();

        while (columnValueIterator.hasNext()) {
            final Map.Entry<String, Object> entry = columnValueIterator.next();

            final Column column = tableMetaData.getColumns().get(entry.getKey());
            final Object convertedValue = typeConverter.convert(entry.getValue(), column.getDataType());

            if (convertedValue == null && column.getSequence() != null) {
                // Add the next value in the sequence directly to the statement
                sql.append(createSequenceNextValueForDirectInsert(column.getSequence()));
                columnValueIterator.remove();
            } else {
                sql.append("?, ");
            }
        }

        sql.delete(sql.length() - 2, sql.length());
        sql.append(")");
        LOGGER.trace("Generated SQL: {}; raw column value map: {}", sql, columnValueMap);

        final List<Object> generatedKeys = executeSqlUpdate(sql.toString(), columnValueMap, tableMetaData, true);

        return generatedKeys;
    }

    /**
     * Generates a SQL fragment to retrieve the next value from a sequence for direct use in an INSERT or UPDATE statement,
     * e.g. to generate "INSERT INTO LB.ACCOUNT(ACCOUNT_ID, ACCOUNT_NAME) VALUES (NEXT VALUE FOR sequence_name, ?)",
     * this method returns "NEXT VALUE FOR sequence_name".
     *
     * @param sequence the name of the database sequence to generate the next value from
     * @return a formatted SQL string representing the next sequence value for direct insertion
     */
    protected static @Nonnull String createSequenceNextValueForDirectInsert(final String sequence) {
        return "NEXT VALUE FOR %s, ".formatted(sequence);
    }

    @Override
    public @Nullable List<Object> update(final TableMetaData tableMetaData, final Map<String, Object> columnValueMap, final LinkedHashMap<String, Object> primaryKey) throws SQLException {
        final StringBuilder sql = new StringBuilder("UPDATE ")
                .append(tableMetaData.getSchema())
                .append('.')
                .append(tableMetaData.getTable())
                .append(" SET ");

        columnValueMap.keySet().forEach(columnName -> sql.append(columnName).append(" = ?, "));
        sql.delete(sql.length() - 2, sql.length());
        sql.append(" WHERE ");
        primaryKey.forEach((columnName, value) -> sql.append(columnName).append(" = ? AND "));
        sql.delete(sql.length() - 5, sql.length());

        final LinkedHashMap<String, Object> columnValueMapWithPrimaryKey = new LinkedHashMap<>(columnValueMap);
        columnValueMapWithPrimaryKey.putAll(primaryKey);

        return executeSqlUpdate(sql.toString(), columnValueMapWithPrimaryKey, tableMetaData, false);
    }

    @Override
    public List<Map<String, Object>> select(final TableMetaData tableMetaData, final List<String> columns, final List<Condition> conditions) throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT ")
                .append(String.join(", ", columns))
                .append(" FROM ")
                .append(tableMetaData.getSchema())
                .append('.')
                .append(tableMetaData.getTable());

        if (!CollectionUtils.isEmpty(conditions)) {
            sql.append(" WHERE ");
            conditions.forEach(condition -> sql.append(createCondition(condition)).append(" AND "));
            sql.delete(sql.length() - 5, sql.length());
        }

        final LinkedHashMap<String, Object> columnValueMap = conditions.stream()
                .collect(Collectors.toMap(Condition::getColumn,
                        Condition::getValue,
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));
        LOGGER.trace("Generated SQL: {} with bind parameters: {}", sql, columnValueMap);

        return executeSqlQuery(sql.toString(), columns, columnValueMap, tableMetaData);
    }

    @Override
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    protected String createCondition(final Condition condition) {
        return "%s %s ?".formatted(condition.getColumn(), mapOperator(condition.getOperator()));
    }

    protected String mapOperator(final Operator operator) {
        return switch (operator) {
            case EQ -> "=";
            case GT -> ">";
            case GTE -> ">=";
            case LT -> "<";
            case LTE -> "<=";
            case NEQ -> "<>";
            case IN -> "IN";
        };
    }

    private @Nullable List<Object> executeSqlUpdate(final String sql, final Map<String, Object> columnValueMap, final TableMetaData tableMetaData, final boolean returnGeneratedKeys) throws SQLException {
        final List<Object> generatedKeys;

        try (final PreparedStatement preparedStatement = createPreparedStatement(sql, columnValueMap, tableMetaData, true)) {
            final int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0 && returnGeneratedKeys) {
                generatedKeys = new ArrayList<>(tableMetaData.getPrimaryKey().size());
                final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys();

                for (String pkColumnName : tableMetaData.getPrimaryKey()) {
                    if (generatedKeysResultSet.next()) {
                        final Object generatedId = generatedKeysResultSet.getLong(pkColumnName);
                        LOGGER.debug("Generated ID for column '{}': {}", pkColumnName, generatedId);
                        generatedKeys.add(generatedId);
                    }
                }

                generatedKeysResultSet.close();
            } else {
                generatedKeys = null;
            }
        }

        return generatedKeys;
    }

    private List<Map<String, Object>> executeSqlQuery(final String sql, final List<String> columns, final Map<String, Object> columnValueMap, final TableMetaData tableMetaData) throws SQLException {
        try (final PreparedStatement preparedStatement = createPreparedStatement(sql, columnValueMap, tableMetaData, false)) {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<Map<String, Object>> resultList = new ArrayList<>();

            while (resultSet.next()) {
                final Map<String, Object> row = new LinkedHashMap<>();

                for (String columnName : tableMetaData.getColumns().keySet()) {
                    final Column column = tableMetaData.getColumns().get(columnName);
                    row.put(columnName, typeConverter.convert(resultSet.getObject(columnName), column.getDataType()));
                }

                resultList.add(row);
            }

            return resultList;
        }
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

    protected static void verifySchemaAndTableExists(final String catalog, final String schema, final String table, final DatabaseMetaData databaseMetaData) throws SQLException {
        final ResultSet schemas = databaseMetaData.getSchemas(catalog, schema);
        boolean schemaExists = false;

        while (schemas.next()) {
            if (Objects.equals(schema, schemas.getString("TABLE_SCHEM"))) {
                schemaExists = true;
                break;
            }
        }

        if (!schemaExists) {
            throw new IllegalArgumentException("Schema not found: " + schema);
        }

        final ResultSet tables = databaseMetaData.getTables(catalog, schema, table, new String[]{"TABLE"});
        boolean tableExists = false;

        while (tables.next()) {
            if (Objects.equals(table, tables.getString("TABLE_NAME"))) {
                tableExists = true;
                break;
            }
        }

        if (!tableExists) {
            throw new IllegalArgumentException("Table not found: " + table);
        }
    }

    protected PreparedStatement createPreparedStatement(final String sql, final Map<String, Object> columnValueMap, final TableMetaData tableMetaData, final boolean returnGeneratedKeys) throws SQLException {
        final Logger logger = getLogger();
        final List<Object> bindValues;

        if (logger.isTraceEnabled()) {
            bindValues = new ArrayList<>(columnValueMap.size());
        } else {
            bindValues = null;
        }

        final PreparedStatement preparedStatement;

        if (returnGeneratedKeys) {
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            preparedStatement = connection.prepareStatement(sql);
        }

        final int[] ordinal = {1};

        for (Map.Entry<String, Object> entry : columnValueMap.entrySet()) {
            final Column column = tableMetaData.getColumns().get(entry.getKey());
            final Object convertedValue = typeConverter.convert(entry.getValue(), column.getDataType());

            if (logger.isTraceEnabled()) {
                assert bindValues != null;
                bindValues.add(convertedValue);
            }

            if (convertedValue == null) {
                if (column.isNullable()) {
                    preparedStatement.setNull(ordinal[0]++, column.getDataType());
                } else {
                    throw new IllegalArgumentException("Attempting to insert NULL into non-nullable column: '%s'. Possible cause: column spec missing generator such as autoincrement/sequence (schema: '%s', table: '%s')".formatted(column.getName(), tableMetaData.getSchema(), tableMetaData.getTable()));
                }
                continue;
            }

            switch (convertedValue) {
                case Integer integer -> preparedStatement.setInt(ordinal[0]++, integer);
                case Long longValue -> preparedStatement.setLong(ordinal[0]++, longValue);
                case Short shortValue -> preparedStatement.setShort(ordinal[0]++, shortValue);
                case Boolean bool -> preparedStatement.setBoolean(ordinal[0]++, bool);
                case String string -> preparedStatement.setString(ordinal[0]++, string);
                case Timestamp timestamp -> preparedStatement.setTimestamp(ordinal[0]++, timestamp);
                default -> preparedStatement.setObject(ordinal[0]++, convertedValue, column.getDataType());
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Prepared SQL: {} with bind parameters: {}", sql, bindValues);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Prepared SQL: {}", sql);
        }

        return preparedStatement;
    }

    protected Logger getLogger() {
        return LOGGER;
    }
}
