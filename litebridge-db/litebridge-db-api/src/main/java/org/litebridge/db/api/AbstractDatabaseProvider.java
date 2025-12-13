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

        final List<BindValue> bindValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : columnValueMap.entrySet()) {
            final Column column = tableMetaData.getColumns().get(entry.getKey());
            final Object convertedValue = typeConverter.convert(entry.getValue(), column.getDataType());

            if (convertedValue == null) {
                if (!column.isNullable() && !column.isAutoIncrement() && column.getSequence() == null) {
                    throw new IllegalArgumentException("Attempting to insert NULL into non-nullable column: '%s'. Possible cause: column spec missing generator such as autoincrement/sequence (schema: '%s', table: '%s')".formatted(column.getName(), tableMetaData.getSchema(), tableMetaData.getTable()));
                } else if (column.getSequence() != null) {
                    // Add the next value in the sequence directly to the statement
                    sql.append(createSequenceNextValueForDirectInsert(column.getSequence()));
                }
            } else {
                sql.append("?, ");
                bindValues.add(new BindValue(convertedValue, column.getDataType()));
            }
        }

        sql.delete(sql.length() - 2, sql.length());
        sql.append(")");

        final List<Object> generatedKeys = executeSqlUpdate(sql.toString(), bindValues, tableMetaData, true);
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

        final List<BindValue> bindValues = columnValueMap.entrySet().stream()
                .map(entry -> {
                    final Column column = tableMetaData.getColumns().get(entry.getKey());
                    final Object convertedValue = typeConverter.convert(entry.getValue(), column.getDataType());
                    return new BindValue(convertedValue, column.getDataType());
                })
                .collect(Collectors.toCollection(ArrayList::new));
        primaryKey.forEach((columnName, value) -> {
            final Column column = tableMetaData.getColumns().get(columnName);
            final Object convertedValue = typeConverter.convert(value, column.getDataType());
            bindValues.add(new BindValue(convertedValue, column.getDataType()));
        });

        return executeSqlUpdate(sql.toString(), bindValues, tableMetaData, false);
    }

    @Override
    public List<Map<String, Object>> select(final TableMetaData tableMetaData, final List<String> columns, final List<Condition> conditions, final List<String> orderBy) throws SQLException {
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

        if (!CollectionUtils.isEmpty(orderBy)) {
            sql.append(" ORDER BY ").append(String.join(", ", orderBy));
        }

        return executeSqlQuery(sql.toString(), columns, conditions, tableMetaData);
    }

    @Override
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    protected String createCondition(final Condition condition) {
        if (condition.getOperator() == Operator.IS_NULL || condition.getOperator() == Operator.IS_NOT_NULL) {
            return "%s %s".formatted(condition.getColumn(), mapOperator(condition.getOperator()));
        } else {
            return "%s %s ?".formatted(condition.getColumn(), mapOperator(condition.getOperator()));
        }
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
            case IS_NULL -> "IS NULL";
            case IS_NOT_NULL -> "IS NOT NULL";
        };
    }

    private @Nullable List<Object> executeSqlUpdate(final String sql, final List<BindValue> bindValues, final TableMetaData tableMetaData, final boolean returnGeneratedKeys) throws SQLException {
        final List<Object> generatedKeys;

        try (final PreparedStatement preparedStatement = createPreparedStatement(sql, bindValues, tableMetaData, true)) {
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

    private List<Map<String, Object>> executeSqlQuery(final String sql, final List<String> columns, final List<Condition> conditions, final TableMetaData tableMetaData) throws SQLException {
        final List<BindValue> bindValues = conditions.stream()
                .filter(condition -> condition.getOperator() != Operator.IS_NULL && condition.getOperator() != Operator.IS_NOT_NULL)
                .map(condition -> {
                    final Column column = tableMetaData.getColumns().get(condition.getColumn());
                    final Object convertedValue = typeConverter.convert(condition.getValue(), column.getDataType());
                    return new BindValue(convertedValue, column.getDataType());
                })
                .toList();

        try (final PreparedStatement preparedStatement = createPreparedStatement(sql, bindValues, tableMetaData, false)) {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<Map<String, Object>> resultList = new ArrayList<>();

            while (resultSet.next()) {
                final Map<String, Object> row = new LinkedHashMap<>();

                for (final String columnName : columns) {
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

    protected PreparedStatement createPreparedStatement(final String sql, final List<BindValue> bindValues, final TableMetaData tableMetaData, final boolean returnGeneratedKeys) throws SQLException {
        if (LOGGER.isTraceEnabled() && !CollectionUtils.isEmpty(bindValues)) {
            LOGGER.trace("Generated SQL: {} with bind parameters: {}", sql, bindValues.stream().map(BindValue::value).toList());
        } else {
            LOGGER.debug("Generated SQL: {}", sql);
        }

        final PreparedStatement preparedStatement;

        if (returnGeneratedKeys) {
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            preparedStatement = connection.prepareStatement(sql);
        }

        final int[] ordinal = {1};

        if (!CollectionUtils.isEmpty(bindValues)) {
            for (BindValue bindValue : bindValues) {
                if (bindValue == null) {
                    preparedStatement.setString(ordinal[0]++, null);
                }

                switch (bindValue.value) {
                    case Integer integer -> preparedStatement.setInt(ordinal[0]++, integer);
                    case Long longValue -> preparedStatement.setLong(ordinal[0]++, longValue);
                    case Short shortValue -> preparedStatement.setShort(ordinal[0]++, shortValue);
                    case Boolean bool -> preparedStatement.setBoolean(ordinal[0]++, bool);
                    case String string -> preparedStatement.setString(ordinal[0]++, string);
                    case Timestamp timestamp -> preparedStatement.setTimestamp(ordinal[0]++, timestamp);
                    default -> preparedStatement.setObject(ordinal[0]++, bindValue, bindValue.sqlDataType());
                }
            }
        }

        return preparedStatement;
    }

    protected Logger getLogger() {
        return LOGGER;
    }

    protected record BindValue(Object value, int sqlDataType) {
    }
}
