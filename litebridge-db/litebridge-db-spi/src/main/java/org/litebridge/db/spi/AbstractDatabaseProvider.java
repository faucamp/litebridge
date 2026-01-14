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
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.RowValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDatabaseProvider implements DatabaseProvider {

    static final String[] TYPES_TABLE = {"TABLE"};
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

    private TableMetaData ensureTableMetaData(final String catalog, final String schema, final String table) throws SQLException {
        return ensureTableMetaData(new Table(catalog, schema, table));
    }

    private TableMetaData ensureTableMetaData(final Table table) throws SQLException {
        if (table instanceof TableMetaData tableMetaData) {
            return tableMetaData;
        }

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
    public InsertResult insert(final Insert insert) throws SQLException {
        final PreparedSql preparedSql = prepareSql(insert);
        return executeSqlInsert(preparedSql, insert.table());
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
        return "NEXT VALUE FOR %s".formatted(sequence);
    }

    @Override
    public UpdateResult update(final Update update) throws SQLException {
        final PreparedSql preparedSql = prepareSql(update);
        return executeSqlUpdate(preparedSql, update.table());
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

    @Override
    public String toSql(final Select select) {
        final StringBuilder sql = new StringBuilder("SELECT ");

        boolean first = true;

        // Select columns
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

        // From table
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

    protected PreparedSql prepareSql(final Insert insert) {
        final List<String> columnNames = insert.columns().stream().map(ColumnMetaData::name).toList();

        final StringBuilder sql = new StringBuilder("INSERT INTO ");

        if (!StringUtils.isBlank(insert.table().schema())) {
            sql.append(insert.table().schema()).append('.');
        }

        sql.append(insert.table().name()).append(" (")
                .append(String.join(", ", columnNames))
                .append(") VALUES ");

        final List<BindValue> bindValues = new ArrayList<>(insert.rows().size() * columnNames.size());

        boolean first = true;

        for (RowValue row : insert.rows()) {
            final PreparedRow preparedRow = prepareRow(row);
            sql.append('(').append(String.join(", ", preparedRow.valueSpecifiers())).append(')');
            bindValues.addAll(preparedRow.bindValues());

            if (first) {
                first = false;
            } else {
                sql.append(", ");
            }
        }

        return new PreparedSql(sql.toString(), bindValues);
    }

    protected PreparedSql prepareSql(final Update update) {
        final StringBuilder sql = new StringBuilder("UPDATE ");

        if (!StringUtils.isBlank(update.table().schema())) {
            sql.append(update.table().schema()).append('.');
        }

        sql.append(update.table().name()).append(" SET ");

        final List<BindValue> bindValues = new ArrayList<>(update.columnValues().size());

        boolean first = true;

        for (ColumnValue columnValue : update.columnValues()) {
            if (first) {
                first = false;
            } else {
                sql.append(", ");
            }

            sql.append(columnValue.column().name()).append(" = ?");

            final Object convertedValue = typeConverter.convert(columnValue.value(), columnValue.column().getDataType());
            bindValues.add(new BindValue(convertedValue, columnValue.column().getDataType()));
        }

        if (!update.where().isEmpty()) {
            sql.append(" WHERE ");

            first = true;

            for (Condition condition : update.where()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(" AND ");
                }

                sql.append(createCondition(condition));

                if (condition.value() != null) {
                    bindValues.add(new BindValue(condition.value(), ((ColumnMetaData) condition.column()).getDataType()));
                }
            }
        }

        return new PreparedSql(sql.toString(), bindValues);
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

    protected InsertResult executeSqlInsert(final PreparedSql preparedSql, final TableMetaData tableMetaData) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, true)) {
            final int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                final List<Object> generatedKeys = new ArrayList<>(tableMetaData.primaryKey().size());
                final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys();

                for (ColumnMetaData pkColumn : tableMetaData.primaryKey()) {
                    if (generatedKeysResultSet.next()) {
                        final Object generatedId = generatedKeysResultSet.getObject(pkColumn.name());
                        LOGGER.debug("Generated ID for column '{}': {}", pkColumn.name(), generatedId);
                        generatedKeys.add(generatedId);
                    }
                }

                generatedKeysResultSet.close();
                return new InsertResult(affectedRows, generatedKeys);
            } else {
                return new InsertResult(0);
            }
        }
    }

    protected UpdateResult executeSqlUpdate(final PreparedSql preparedSql, final TableMetaData tableMetaData) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, false)) {
            final int affectedRows = preparedStatement.executeUpdate();
            return new UpdateResult(affectedRows);
        }
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

        try (final PreparedStatement preparedStatement = prepareStatement(new PreparedSql(sql, bindValues), false)) {
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
                    final TableMetaData columnTable = ensureTableMetaData(fromTable.catalog(), schemaName, tableName);
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

    protected PreparedStatement prepareStatement(final PreparedSql preparedSql, final boolean returnGeneratedKeys) throws SQLException {
        if (LOGGER.isTraceEnabled() && !CollectionUtils.isEmpty(preparedSql.bindValues)) {
            LOGGER.trace("Generated SQL: {} with bind parameters: {}", preparedSql.sql(), preparedSql.bindValues.stream().map(BindValue::value).toList());
        } else {
            LOGGER.debug("Generated SQL: {}", preparedSql.sql());
        }

        final PreparedStatement preparedStatement;

        if (returnGeneratedKeys) {
            preparedStatement = connection.prepareStatement(preparedSql.sql(), Statement.RETURN_GENERATED_KEYS);
        } else {
            preparedStatement = connection.prepareStatement(preparedSql.sql());
        }

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
                    default -> preparedStatement.setObject(ordinal[0]++, bindValue.value(), bindValue.sqlDataType());
                }
            }
        }

        return preparedStatement;
    }

    protected Logger getLogger() {
        return LOGGER;
    }

    protected PreparedRow prepareRow(final RowValue rowValue) {
        final List<String> valueSpecifiers = new ArrayList<>(rowValue.columns().size());
        final List<BindValue> bindValues = new ArrayList<>(rowValue.columns().size());

        for (final ColumnValue columnValue : rowValue.columns()) {
            final ColumnMetaData column = columnValue.column();
            final Object convertedValue = typeConverter.convert(columnValue.value(), column.getDataType());

            if (convertedValue == null) {
                if (!column.isNullable() && !column.isAutoIncrement() && column.getSequence() == null) {
                    throw new IllegalArgumentException("Attempting to insert NULL into non-nullable column: '%s'. Possible cause: column spec missing generator such as autoincrement/sequence".formatted(column.name()));
                } else if (column.getSequence() != null) {
                    // Add the next value in the sequence directly to the statement
                    valueSpecifiers.add(createSequenceNextValueForDirectInsert(column.getSequence()));
                }
            } else {
                valueSpecifiers.add("?");
                bindValues.add(new BindValue(convertedValue, column.getDataType()));
            }
        }

        return new PreparedRow(valueSpecifiers, bindValues);
    }

    protected record BindValue(@Nullable Object value, int sqlDataType) {
    }

    protected record PreparedSql(String sql, List<@Nullable BindValue> bindValues) {
    }

    protected record PreparedRow(List<String> valueSpecifiers, List<BindValue> bindValues) {
    }
}
