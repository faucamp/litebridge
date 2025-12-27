package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AbstractDatabaseProvider implements DatabaseProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseProvider.class);
    private final Connection connection;
    private final TypeConverter typeConverter;
    private final Map<Table, TableMetaData> tableMetaDataCache = new ConcurrentHashMap<>();

    public AbstractDatabaseProvider(final Connection connection,
                                    final TypeConverter typeConverter) {
        this.connection = connection;
        this.typeConverter = ObjectUtils.requireNonNull(typeConverter, "No TypeConverter provided");
    }

    @Override
    public TableMetaData getTableMetaData(final Table table) throws SQLException {
        return ensureTableMetaData(table);
    }

    private TableMetaData ensureTableMetaData(final String schema, final String table) throws SQLException {
        return ensureTableMetaData(new Table("", schema, table));
    }

    private TableMetaData ensureTableMetaData(final Table table) throws SQLException {
        TableMetaData tableMetaData = this.tableMetaDataCache.get(table);

        if (tableMetaData == null) {
            tableMetaData = fetchTableMetaData(table);
            tableMetaDataCache.put(table, tableMetaData);
        }

        return tableMetaData;
    }

    protected TableMetaData fetchTableMetaData(final Table table) throws SQLException {
        final DatabaseMetaData databaseMetaData = connection.getMetaData();

        // Verify basic details
        verifySchemaAndTableExists(table, databaseMetaData);

        // Load table metadata
        final List<String> primaryKeys = getPrimaryKeyColumnNames(table, databaseMetaData);
        final List<ColumnMetaData> columns = getColumnNames(table, databaseMetaData);
        return new TableMetaData(table, primaryKeys, columns);
    }

    @Override
    public @Nullable List<Object> insert(final TableMetaData tableMetaData, final Map<String, Object> columnValueMap) throws SQLException {
        final StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableMetaData.schema())
                .append('.')
                .append(tableMetaData.name())
                .append(" (")
                .append(String.join(", ", columnValueMap.keySet()))
                .append(") VALUES (");

        final List<BindValue> bindValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : columnValueMap.entrySet()) {
            final ColumnMetaData column = tableMetaData.column(entry.getKey());
            final Object convertedValue = typeConverter.convert(entry.getValue(), column.getDataType());

            if (convertedValue == null) {
                if (!column.isNullable() && !column.isAutoIncrement() && column.getSequence() == null) {
                    throw new IllegalArgumentException("Attempting to insert NULL into non-nullable column: '%s'. Possible cause: column spec missing generator such as autoincrement/sequence (schema: '%s', table: '%s')".formatted(column.name(), tableMetaData.schema(), tableMetaData.name()));
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
    protected static String createSequenceNextValueForDirectInsert(final String sequence) {
        return "NEXT VALUE FOR %s, ".formatted(sequence);
    }

    @Override
    public @Nullable List<Object> update(final TableMetaData tableMetaData, final Map<String, Object> columnValueMap, final LinkedHashMap<String, Object> primaryKey) throws SQLException {
        final StringBuilder sql = new StringBuilder("UPDATE ")
                .append(tableMetaData.schema())
                .append('.')
                .append(tableMetaData.name())
                .append(" SET ");

        columnValueMap.keySet().forEach(columnName -> sql.append(columnName).append(" = ?, "));
        sql.delete(sql.length() - 2, sql.length());
        sql.append(" WHERE ");
        primaryKey.forEach((columnName, value) -> sql.append(columnName).append(" = ? AND "));
        sql.delete(sql.length() - 5, sql.length());

        final List<BindValue> bindValues = columnValueMap.entrySet().stream()
                .map(entry -> {
                    final ColumnMetaData column = tableMetaData.column(entry.getKey());
                    final Object convertedValue = typeConverter.convert(entry.getValue(), column.getDataType());
                    return new BindValue(convertedValue, column.getDataType());
                })
                .collect(Collectors.toCollection(ArrayList::new));
        primaryKey.forEach((columnName, value) -> {
            final ColumnMetaData column = tableMetaData.column(columnName);
            final Object convertedValue = typeConverter.convert(value, column.getDataType());
            bindValues.add(new BindValue(convertedValue, column.getDataType()));
        });

        return executeSqlUpdate(sql.toString(), bindValues, tableMetaData, false);
    }

    @Override
    public List<Row> select(final Select select) throws SQLException {
        final String sql = toSql(select);
        return executeSqlQuery(sql, select.columns(), select.where(), select.table());
    }

    @Override
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    protected String toSql(final Select select) {
        final StringBuilder sql = new StringBuilder("SELECT ");

        boolean first = true;

        // Select fields
        if (!CollectionUtils.isEmpty(select.columns())) {
            for (final Column column : select.columns()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }

                if (!StringUtils.isEmpty(column.table().alias())) {
                    sql.append(column.table().alias());
                } else {
                    sql.append(column.table().name());
                }

                sql.append('.').append(column.name());

                if (!StringUtils.isBlank(column.alias())) {
                    sql.append(" AS ").append(column.alias());
                }
            }
        } else {
            // Empty select clause; return all columns
            sql.append("*");
        }

        sql.append(" FROM ");

        if (!StringUtils.isBlank(select.table().schema())) {
            sql.append(select.table().schema()).append('.');
        }

        sql.append(select.table().name());

        if (select.table().alias() != null) {
            sql.append(" AS ").append(select.table().alias());
        }

        // Joins
        if (!CollectionUtils.isEmpty(select.joins())) {
            sql.append(" JOIN ");
            first = true;

            for (Join join : select.joins()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }

                sql.append(createJoin(join));
            }
        }

        // Where
        if (!CollectionUtils.isEmpty(select.where())) {
            sql.append(" WHERE ");

            first = true;
            for (Condition condition : select.where()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(" AND ");
                }

                sql.append(createCondition(condition));
            }
        }

        // Order by
        if (!CollectionUtils.isEmpty(select.orderBy())) {
            sql.append(" ORDER BY ");
            first = true;

            for (final OrderBy orderBy : select.orderBy()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }

                sql.append(orderBy.column()).append(orderBy.asc() ? " ASC" : " DESC");
            }
        }

        select.limit().ifPresent(limit -> {
            limit.limit().ifPresent(limitVal -> sql.append(" LIMIT ").append(limitVal));
            limit.offset().ifPresent(offset -> sql.append(" OFFSET ").append(offset));
        });

        return sql.toString();
    }

    protected String createJoin(final Join join) {
        final StringBuilder sb = new StringBuilder();

        if (!StringUtils.isBlank(join.table().schema())) {
            sb.append(join.table().schema()).append('.');
        }

        sb.append(join.table().name());

        if (join.table().alias() != null) {
            sb.append(" AS ").append(join.table().alias());
        }

        if (join.conditions().getFirst().operator() != Operator.USING) {
            sb.append(" ON ");
        } else {
            sb.append(' ');
        }

        boolean first = true;

        for (Condition condition : join.conditions()) {
            if (first) {
                first = false;
            } else {
                sb.append(" AND ");
            }

            sb.append(createCondition(condition));
        }

        return sb.toString();
    }

    protected String createCondition(final Condition condition) {
        final String column;

        if (!StringUtils.isEmpty(condition.column().table().alias())) {
            column = condition.column().table().alias() + '.' + condition.column().name();
        } else {
            column = condition.column().name();
        }

        if (condition.operator() == Operator.IS_NULL || condition.operator() == Operator.IS_NOT_NULL) {
            return "%s %s".formatted(column, mapOperator(condition.operator()));
        } else if (condition.operator() == Operator.USING) {
            return "%s (%s)".formatted(mapOperator(condition.operator()), condition.column().name());
        } else {
            return "%s %s ?".formatted(column, mapOperator(condition.operator()));
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
            case USING -> "USING";
        };
    }

    private @Nullable List<Object> executeSqlUpdate(final String sql, final List<BindValue> bindValues, final TableMetaData tableMetaData, final boolean returnGeneratedKeys) throws SQLException {
        final List<Object> generatedKeys;

        try (final PreparedStatement preparedStatement = createPreparedStatement(sql, bindValues, tableMetaData, true)) {
            final int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0 && returnGeneratedKeys) {
                generatedKeys = new ArrayList<>(tableMetaData.primaryKey().size());
                final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys();

                for (String pkColumnName : tableMetaData.primaryKey()) {
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

    private List<Row> executeSqlQuery(final String sql, final List<Column> columns, final List<Condition> conditions, final Table table) throws SQLException {
        final TableMetaData fromTable = ensureTableMetaData(table);
        final List<BindValue> bindValues = conditions.stream()
                .filter(condition -> condition.operator() != Operator.IS_NULL && condition.operator() != Operator.IS_NOT_NULL)
                .map(condition -> {
                    final ColumnMetaData column = fromTable.column(condition.column().name());
                    final Object convertedValue = typeConverter.convert(condition.value(), column.getDataType());
                    return new BindValue(convertedValue, column.getDataType());
                })
                .toList();

        try (final PreparedStatement preparedStatement = createPreparedStatement(sql, bindValues, fromTable, false)) {
            // Execute SQL query
            final ResultSet resultSet = preparedStatement.executeQuery();

            // Parse results
            final List<Row> rows = new ArrayList<>();

            while (resultSet.next()) {
                final Row row = new Row();
                final int columnCount = resultSet.getMetaData().getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    final String schemaName = resultSet.getMetaData().getSchemaName(i);
                    final String tableName = resultSet.getMetaData().getTableName(i);
                    final TableMetaData columnTable = ensureTableMetaData(schemaName, tableName);
                    final String columnName = resultSet.getMetaData().getColumnName(i);
                    final String alias = resultSet.getMetaData().getColumnLabel(i);
                    final ColumnMetaData column = columnTable.column(columnName).as(alias);

                    final Object value = typeConverter.convert(resultSet.getObject(columnName), column.getDataType());
                    row.withColumn(column, value);
                }

                rows.add(row);
            }

            return rows;
        }
    }

    protected List<ColumnMetaData> getColumnNames(final Table table, final DatabaseMetaData databaseMetaData) throws SQLException {
        final ResultSet dbColumns = databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null);
        final List<ColumnMetaData> columns = new ArrayList<>();

        while (dbColumns.next()) {
            final String name = dbColumns.getString("COLUMN_NAME");
            final boolean nullable = dbColumns.getBoolean("IS_NULLABLE");
            final int dataType = dbColumns.getInt("DATA_TYPE");
            final int size = dbColumns.getInt("COLUMN_SIZE");

            columns.add(new ColumnMetaData(table, name, nullable, dataType, size));
        }

        dbColumns.close();
        return columns;
    }

    protected List<String> getPrimaryKeyColumnNames(final Table table, final DatabaseMetaData databaseMetaData) throws SQLException {
        final ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name());
        final List<String> primaryKeyColumnNames = new ArrayList<>();

        while (primaryKeys.next()) {
            final String columnName = primaryKeys.getString("COLUMN_NAME");
            primaryKeyColumnNames.add(columnName);
        }

        primaryKeys.close();
        return primaryKeyColumnNames;
    }

    protected static void verifySchemaAndTableExists(final Table table, final DatabaseMetaData databaseMetaData) throws SQLException {
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

        final ResultSet tables = databaseMetaData.getTables(table.catalog(), table.schema(), table.name(), new String[]{"TABLE"});
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
