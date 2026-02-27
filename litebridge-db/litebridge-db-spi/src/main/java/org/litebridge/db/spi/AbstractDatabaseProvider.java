package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
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
import org.litebridge.db.spi.util.SqlReservedWords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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

/**
 * An abstract implementation of the {@link DatabaseProvider} interface that provides a framework for interacting
 * with a database by managing SQL queries, metadata retrieval, and type conversions. This class serves as a base
 * for specific database implementations, handling common functionality while leaving database-specific details
 * to subclasses.
 * <p>
 * This class includes utility methods for preparing and executing SQL statements, fetching table metadata, and
 * performing insert, update, and select operations. It uses a caching mechanism for table metadata to improve
 * efficiency and ensures type conversion using a pluggable {@link TypeConverter}.
 */
public abstract class AbstractDatabaseProvider implements DatabaseProvider {

    static final String[] TYPES_TABLE = {"TABLE"};
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseProvider.class);
    private final Connection connection;
    private final TypeConverter typeConverter;
    /**
     * Map of qualified table name -> table metadata.
     */
    private final Map<String, TableMetaData> tableMetaDataCache = new ConcurrentHashMap<>();

    public AbstractDatabaseProvider(final Connection connection,
                                    final TypeConverter typeConverter) {
        this.connection = connection;
        this.typeConverter = Objects.requireNonNull(typeConverter, "No TypeConverter provided");
    }

    @Override
    public TableMetaData getTableMetaData(final Table table) throws SQLException {
        return ensureTableMetaData(table);
    }

    @Override
    public InsertResult insert(final Insert insert) throws SQLException {
        final PreparedSql preparedSql = prepareSql(insert);
        return executeSqlInsert(preparedSql, insert.table(), insert.returnGeneratedKeys());
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

                appendColumn(sql, column);
            }
        } else {
            // Empty select clause; return all columns
            sql.append("*");
        }

        // From table
        sql.append(" FROM ");
        appendTable(sql, select.table());

        if (select.table().alias() != null) {
            sql.append(" AS ").append(quoteIdentifier(select.table().alias()));
        }

        // Joins
        if (!CollectionUtils.isEmpty(select.joins())) {
            for (Join join : select.joins()) {
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

                if (orderBy.column().table().alias() != null) {
                    sql.append(quoteIdentifier(orderBy.column().table().alias())).append('.');
                }

                sql.append(quoteIdentifier(orderBy.column().name())).append(orderBy.asc() ? " ASC" : " DESC");
            }
        }

        select.limit().ifPresent(limit -> {
            limit.limit().ifPresent(limitVal -> sql.append(" LIMIT ").append(limitVal));
            limit.offset().ifPresent(offset -> sql.append(" OFFSET ").append(offset));
        });

        return sql.toString();
    }

    /**
     * Generate a SQL fragment to retrieve the next value from a sequence for direct use in an INSERT or UPDATE statement,
     * e.g. to generate "INSERT INTO LB.ACCOUNT(ACCOUNT_ID, ACCOUNT_NAME) VALUES (NEXT VALUE FOR sequence_name, ?)",
     * this method returns "NEXT VALUE FOR sequence_name".
     *
     * @param sequence the name of the database sequence to generate the next value from
     * @return a formatted SQL string representing the next sequence value for direct insertion
     */
    protected static String createSequenceNextValueForDirectInsert(final String sequence) {
        return "NEXT VALUE FOR %s".formatted(sequence);
    }

    /**
     * Prepare a SQL INSERT statement along with its bind values for execution.
     * <p>
     * This method constructs the SQL query string based on the provided {@link Insert} object,
     * which contains the table's metadata, columns, and rows to be inserted.
     * The bind values are derived from the rows and included in the returned {@link  PreparedSql}.
     *
     * @param insert the {@link Insert} object containing the table metadata, columns, and rows for the SQL INSERT operation
     * @return a {@link PreparedSql} object containing the generated SQL query string and the list of bind values
     */
    protected PreparedSql prepareSql(final Insert insert) {
        final List<String> columnNames = insert.columns().stream().map(ColumnMetaData::name).toList();

        final StringBuilder sql = appendTable(new StringBuilder("INSERT INTO "), insert.table())
                .append(" (")
                .append(String.join(", ", columnNames.stream().map(this::quoteIdentifier).toList()))
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

    /**
     * Prepare a SQL UPDATE statement along with its bind values for execution.
     * <p>
     * This method constructs the SQL query string based on the provided {@link Update} object,
     * which contains the table's metadata, column-value pairs, and conditions for the WHERE clause.
     * It ensures proper formatting of the SQL query and converts values as needed using a type converter.
     * The resulting SQL query and its associated bind values are encapsulated in a {@link PreparedSql} object.
     *
     * @param update the {@link Update} object containing table metadata, column-value pairs for the SET clause,
     *               and conditions for the WHERE clause to specify target rows.
     * @return a {@link PreparedSql} object containing the generated SQL query string and the list of bind values.
     */
    protected PreparedSql prepareSql(final Update update) {
        final StringBuilder sql = appendTable(new StringBuilder("UPDATE "), update.table())
                .append(" SET ");

        final List<BindValue> bindValues = new ArrayList<>(update.columnValues().size());

        boolean first = true;

        for (ColumnValue columnValue : update.columnValues()) {
            if (first) {
                first = false;
            } else {
                sql.append(", ");
            }

            sql.append(quoteIdentifier(columnValue.column().name())).append(" = ?");

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
                    bindValues.add(new BindValue(condition.value(),
                            ensureTableMetaData(condition.column().table())
                                    .column(condition.column().name())
                                    .getDataType()));
                }
            }
        }

        return new PreparedSql(sql.toString(), bindValues);
    }

    /**
     * Create a SQL JOIN clause based on the provided {@link Join} object.
     * <p>
     * The join clause is constructed by specifying the target table, optional schema,
     * and any associated conditions for the join operation. Conditional logic is applied
     * to determine the join type (e.g., ON or USING) and format the resulting SQL string.
     *
     * @param join the {@link Join} object containing the target table information and the list
     *             of conditions defining the join relationship
     * @return a {@code String} representing the constructed SQL join clause
     */
    protected String createJoin(final Join join) {
        final StringBuilder sb = appendTable(new StringBuilder(" JOIN "), join.table());

        if (join.table().alias() != null) {
            sb.append(" AS ").append(quoteIdentifier(join.table().alias()));
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

    /**
     * Generate a SQL condition string based on the given {@link Condition}.
     * This method constructs the SQL fragment by combining the column, operator,
     * and value (if applicable) for the provided condition.
     *
     * @param condition the {@link Condition} object specifying the column, operator,
     *                  and value for the SQL condition
     * @return a {@code String} representing the constructed SQL condition fragment
     */
    protected String createCondition(final Condition condition) {
        final String column;

        if (!StringUtils.isEmpty(condition.column().table().alias())) {
            column = quoteIdentifier(condition.column().table().alias()) + '.' + quoteIdentifier(condition.column().name());
        } else {
            column = quoteIdentifier(condition.column().name());
        }

        if (condition.operator() == Operator.IS_NULL || condition.operator() == Operator.IS_NOT_NULL) {
            return "%s %s".formatted(column, mapOperator(condition.operator()));
        } else if (condition.operator() == Operator.USING) {
            return "%s (%s)".formatted(mapOperator(condition.operator()), condition.column().name());
        } else {
            // If the target value is a column, reference that
            if (condition.value() instanceof Column targetColumn) {
                return "%s %s %s.%s".formatted(column, mapOperator(condition.operator()), quoteIdentifier(targetColumn.table().aliasOrName()), quoteIdentifier(targetColumn.name()));
            } else {
                return "%s %s ?".formatted(column, mapOperator(condition.operator()));
            }
        }
    }

    protected StringBuilder appendTable(final StringBuilder sql, final Table table) {
        return appendTable(sql, table.schema(), table.name());
    }

    protected StringBuilder appendTable(final StringBuilder sql, final TableMetaData table) {
        return appendTable(sql, table.schema(), table.name());
    }

    protected StringBuilder appendTable(final StringBuilder sql, final String schema, final String table) {
        if (!StringUtils.isBlank(schema)) {
            sql.append(quoteIdentifier(schema)).append('.');
        }

        sql.append(quoteIdentifier(table));
        return sql;
    }

    private void appendColumn(final StringBuilder sql, final Column column) {
        if (!StringUtils.isEmpty(column.table().alias())) {
            sql.append(quoteIdentifier(column.table().alias()));
        } else {
            sql.append(quoteIdentifier(column.table().name()));
        }

        sql.append('.').append(quoteIdentifier(column.name()));

        if (!StringUtils.isBlank(column.alias())) {
            sql.append(" AS ").append(quoteIdentifier(column.alias()));
        }
    }

    /**
     * Map an {@link Operator} enum to its corresponding string representation used in logical or database operations.
     *
     * @param operator the Operator enum to be mapped
     * @return the string representation of the provided Operator
     */
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

    /**
     * Execute a SQL INSERT operation using the provided prepared SQL statement and table metadata.
     * <p>
     * This method executes the prepared statement, retrieves any generated primary key values,
     * and wraps the results in an {@link InsertResult} object.
     *
     * @param preparedSql         the {@link PreparedSql} object containing the SQL query string and bind values to be executed
     * @param tableMetaData       the {@link TableMetaData} object containing the metadata of the target table, including primary key information
     * @param returnGeneratedKeys a boolean indicating whether the statement should return generated keys.
     *                            Pass {@code true} to configure the statement to return generated keys,
     *                            or {@code false} otherwise.
     * @return an {@link InsertResult} object encapsulating the number of affected rows and a list of generated keys (if any)
     * @throws SQLException if an error occurs while executing the SQL insert or retrieving the generated keys
     */
    protected InsertResult executeSqlInsert(final PreparedSql preparedSql, final TableMetaData tableMetaData, final boolean returnGeneratedKeys) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, returnGeneratedKeys)) {
            final int affectedRows = preparedStatement.executeUpdate();

            if (returnGeneratedKeys && affectedRows > 0) {
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
                return new InsertResult(affectedRows);
            }
        }
    }

    /**
     * Execute a SQL UPDATE operation using the provided prepared SQL statement and table metadata.
     * <p>
     * This method performs the execution of a prepared update statement and wraps the number
     * of affected rows in an {@link UpdateResult} object.
     *
     * @param preparedSql   the {@link PreparedSql} object containing the SQL query string and bind values to be executed
     * @param tableMetaData the {@link TableMetaData} object containing the metadata of the target table
     * @return an {@link UpdateResult} object encapsulating the number of rows affected by the update operation
     * @throws SQLException if an error occurs while executing the SQL update
     */
    protected UpdateResult executeSqlUpdate(final PreparedSql preparedSql, final TableMetaData tableMetaData) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, false)) {
            final int affectedRows = preparedStatement.executeUpdate();
            return new UpdateResult(affectedRows);
        }
    }

    /**
     * Execute the given SQL query with specified columns, conditions, and table, and returns the result as a list of rows.
     *
     * @param sql        the SQL query to be executed
     * @param columns    the list of columns to include in the query
     * @param conditions the list of conditions to apply in the WHERE clause of the query
     * @param table      the table from which data is queried
     * @return a list of {@code Row} objects representing the query results
     * @throws SQLException if an SQL error occurs while executing the query
     */
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
                    final String alias = transformAlias(resultSet.getMetaData().getColumnLabel(i));
                    final ColumnMetaData columnMetaData = columnTable.column(columnName);
                    final Column column = columnMetaData.toColumn().as(alias);

                    final Object value = typeConverter.convert(resultSet.getObject(columnName), columnMetaData.getDataType());
                    row.withColumn(column, value);
                }

                rows.add(row);
            }

            return rows;
        }
    }

    protected @Nullable String transformAlias(final @Nullable String dbAlias) {
        return dbAlias;
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

    /**
     * Prepare a {@link PreparedStatement} object based on the provided SQL and bind values.
     * <p>
     * Optionally, the statement can be configured to return generated keys.
     *
     * @param preparedSql         the {@link PreparedSql} object containing the SQL query and associated bind values.
     * @param returnGeneratedKeys a boolean indicating whether the statement should return generated keys.
     *                            Pass {@code true} to configure the statement to return generated keys,
     *                            or {@code false} otherwise.
     * @return a {@link PreparedStatement} that is ready to be executed based on the provided SQL and bind values.
     * @throws SQLException if a database access error occurs or the preparation of the SQL statement fails.
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
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

    /**
     * Return the logger instance for this database provider.
     *
     * @return the logger instance
     */
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Prepare a row for insertion based on the provided row value. This includes
     * processing column values, converting them to a suitable format, and generating
     * value specifiers and bind values for the prepared row. Handles nullable columns,
     * auto-increment columns, and sequence-based value generation as necessary.
     *
     * @param rowValue the row value object containing the column definitions and their values
     * @return a PreparedRow instance containing processed value specifiers and bind values
     * @throws IllegalArgumentException if a non-nullable column without an auto-increment or sequence value is attempted to be set to NULL
     */
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

    private TableMetaData ensureTableMetaData(final String catalog, final String schema, final String table) throws SQLException {
        return ensureTableMetaData(new Table(catalog, schema, table));
    }

    private TableMetaData ensureTableMetaData(final Table table) {
        TableMetaData tableMetaData = this.tableMetaDataCache.get(table.qualifiedName());

        if (tableMetaData == null) {
            try {
                tableMetaData = fetchTableMetaData(table);
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to get table metadata for table: " + table, ex);
            }

            tableMetaDataCache.put(table.qualifiedName(), tableMetaData);
        }

        return tableMetaData;
    }

    /**
     * Retrieve metadata for the specified table, including its primary keys and columns.
     * <p>
     * This executes a database query to fetch database metadata.
     *
     * @param table the table for which metadata is being fetched, containing schema, catalog, and table name details
     * @return a {@code TableMetaData} object containing details about the table's structure, primary keys, and column metadata
     * @throws SQLException if an error occurs while fetching database metadata
     */
    protected TableMetaData fetchTableMetaData(final Table table) throws SQLException {
        final DatabaseMetaData databaseMetaData = connection.getMetaData();

        // Verify basic details
        verifySchemaAndTableExists(table, databaseMetaData);

        // Load table metadata
        final List<String> primaryKeys = getPrimaryKeyColumnNames(table, databaseMetaData);
        final List<ColumnMetaData> columns = getColumnNames(table, databaseMetaData);
        return new TableMetaData(table, primaryKeys, columns);
    }

    protected @Nullable String quoteIdentifier(final @Nullable String identifier) {
        if (identifier == null) {
            return null;
        }

        if (SqlReservedWords.contains(identifier)) {
            return "\"%s\"".formatted(identifier);
        } else {
            return identifier;
        }
    }

    /**
     * A binding value and its associated SQL data type.
     * <p>
     * This record is used to pair a value with its corresponding SQL type,
     * ensuring that the value can be appropriately converted in database operations.
     *
     * @param value       The object value to be bound, which may be null if representing a SQL NULL.
     * @param sqlDataType The integer value indicating the SQL data type of the bound value,
     *                    corresponding to values in {@link java.sql.Types}.
     */
    protected record BindValue(@Nullable Object value, int sqlDataType) {
    }

    /**
     * A prepared SQL statement along with its associated bind values.
     * <p>
     * This record encapsulates the SQL query string and the list of values to be
     * bound to the query parameters.
     * <p>
     * Instances of this record are immutable and can be used to safely pass
     * SQL queries and their bindings within the application.
     *
     * @param sql        The SQL query string that may contain placeholders for
     *                   parameterized values.
     * @param bindValues The list of bind values corresponding to the placeholders
     *                   in the SQL query. Each value can be nullable, represented
     *                   by the {@link BindValue} type.
     */
    protected record PreparedSql(String sql, List<@Nullable BindValue> bindValues) {
    }

    /**
     * A prepared row with associated value specifiers and bound values.
     * <p>
     * This record is a data structure that holds information about a row in which
     * each element is defined by a list of value specifiers and a corresponding
     * list of bind values. Commonly used in scenarios involving prepared statements
     * or database row mappings.
     * <p>
     * The {@code valueSpecifiers} list contains the string representations or placeholders
     * defining the schema or format for the data in the row.
     * <p>
     * The {@code bindValues} list contains the bound or parameterized values that align
     * with the associated specifiers.
     * <p>
     * It is the caller's responsibility to ensure that the {@code valueSpecifiers} and
     * {@code bindValues} lists are properly aligned, with each value specifier corresponding
     * to its respective bind value.
     * <p>
     * This class is immutable and thread-safe by design.
     *
     * @param valueSpecifiers the list of specifiers defining data format or schema
     * @param bindValues      the list of bound values corresponding to the specifiers
     */
    protected record PreparedRow(List<String> valueSpecifiers, List<BindValue> bindValues) {
    }
}
