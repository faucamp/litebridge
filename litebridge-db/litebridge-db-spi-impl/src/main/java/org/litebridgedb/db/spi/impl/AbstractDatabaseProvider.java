package org.litebridgedb.db.spi.impl;

import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.commons.type.ConcurrentLazy;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.alias.AliasTransformer;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.ConvertExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridgedb.db.spi.impl.function.SqlFunctionRegistryFactory;
import org.litebridgedb.db.spi.impl.sql.DeleteSqlGenerator;
import org.litebridgedb.db.spi.impl.sql.InsertSqlGenerator;
import org.litebridgedb.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridgedb.db.spi.impl.sql.UpdateSqlGenerator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.sql.BindValue;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.tx.ManagedConnection;
import org.litebridgedb.db.spi.update.Delete;
import org.litebridgedb.db.spi.update.Insert;
import org.litebridgedb.db.spi.update.InsertResult;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
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
    protected final TypeConverter typeConverter;
    /**
     * Map of qualified table name -> table metadata.
     */
    private final Map<String, TableMetaData> tableMetaDataCache = new ConcurrentHashMap<>();
    private final ConcurrentLazy<SqlFunctionRegistry> sqlFunctionRegistry = new ConcurrentLazy<>(() -> createSqlFunctionRegistryFactory().create());
    private final ConcurrentLazy<AliasTransformer> aliasTransformer = new ConcurrentLazy<>(this::createAliasTransformer);
    protected final ConcurrentLazy<ColumnIdentifierGenerator> columnIdentifierGenerator = new ConcurrentLazy<>(this::createColumnIdentifierGenerator);
    protected final ConcurrentLazy<SelectSqlGenerator> selectSqlGenerator = new ConcurrentLazy<>(this::createSelectSqlGenerator);
    protected final ConcurrentLazy<InsertSqlGenerator> insertSqlGenerator = new ConcurrentLazy<>(this::createInsertSqlGenerator);
    protected final ConcurrentLazy<UpdateSqlGenerator> updateSqlGenerator = new ConcurrentLazy<>(this::createUpdateSqlGenerator);
    protected final ConcurrentLazy<DeleteSqlGenerator> deleteSqlGenerator = new ConcurrentLazy<>(this::createDeleteSqlGenerator);

    public AbstractDatabaseProvider(final TypeConverter typeConverter) {
        this.typeConverter = Objects.requireNonNull(typeConverter, "No TypeConverter provided");
    }

    @Override
    public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
        return ensureTableMetaData(table, connectionProvider);
    }

    @Override
    public InsertResult insert(final Insert insert, final ConnectionProvider connectionProvider) throws SQLException {
        final PreparedSql preparedSql = insertSqlGenerator.orThrow().prepareSql(insert, connectionProvider);
        final TableMetaData tableMetaData = ensureTableMetaData(insert.table(), connectionProvider);
        return executeSqlInsert(preparedSql, tableMetaData, insert.returnGeneratedKeys(), connectionProvider);
    }

    @Override
    public UpdateResult update(final Update update, final ConnectionProvider connectionProvider) throws SQLException {
        final PreparedSql preparedSql = updateSqlGenerator.orThrow().prepareSql(update, connectionProvider);
        final TableMetaData tableMetaData = ensureTableMetaData(update.table(), connectionProvider);
        return executeSqlUpdate(preparedSql, tableMetaData, connectionProvider);
    }

    @Override
    public List<Row> select(final Select select, final ConnectionProvider connectionProvider) throws SQLException {
        return executeSqlQuery(select, connectionProvider);
    }

    @Override
    public UpdateResult delete(final Delete delete, final ConnectionProvider connectionProvider) throws SQLException {
        final PreparedSql preparedSql = deleteSqlGenerator.orThrow().prepareSql(delete, connectionProvider);
        final TableMetaData tableMetaData = ensureTableMetaData(delete.table(), connectionProvider);
        return executeSqlUpdate(preparedSql, tableMetaData, connectionProvider);
    }

    @Override
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    @Override
    public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return new DefaultSequenceColumnValueGenerator(sequence);
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
    protected InsertResult executeSqlInsert(final PreparedSql preparedSql, final TableMetaData tableMetaData, final boolean returnGeneratedKeys, final ConnectionProvider connectionProvider) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, returnGeneratedKeys, tableMetaData, connectionProvider)) {
            final int affectedRows = preparedStatement.executeUpdate();

            if (returnGeneratedKeys && affectedRows > 0) {
                final Map<ColumnMetaData, Object> generatedKeys = extractGeneratedKeys(tableMetaData, preparedStatement);
                return new InsertResult(affectedRows, generatedKeys);
            } else {
                return new InsertResult(affectedRows);
            }
        }
    }

    protected List<ColumnMetaData> getGeneratedPrimaryKeyColumns(final TableMetaData tableMetaData) {
        return tableMetaData.primaryKey().stream()
                .filter(columnMetadata -> columnMetadata.isAutoIncrement()
                        || (columnMetadata.getGenerator() != null && SequenceColumnValueGenerator.class.isAssignableFrom(columnMetadata.getGenerator().getClass())))
                .toList();
    }

    protected Map<ColumnMetaData, Object> extractGeneratedKeys(final TableMetaData tableMetaData, final PreparedStatement preparedStatement) throws SQLException {
        final List<ColumnMetaData> generatedPrimaryKeys = getGeneratedPrimaryKeyColumns(tableMetaData);
        final Map<ColumnMetaData, Object> generatedKeys = new HashMap<>(tableMetaData.primaryKey().size());

        try (final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys()) {
            while (generatedKeysResultSet.next()) {
                for (ColumnMetaData pkColumn : generatedPrimaryKeys) {
                    final Object generatedId = generatedKeysResultSet.getObject(pkColumn.name());
                    getLogger().debug("Generated ID for lhs '{}': {}", pkColumn.name(), generatedId);
                    generatedKeys.put(pkColumn, generatedId);
                }
            }

            return generatedKeys;
        }
    }

    /**
     * Execute a SQL UPDATE operation using the provided prepared SQL statement and table metadata.
     * <p>
     * This method performs the execution of a prepared update statement and wraps the number
     * of affected rows in an {@link UpdateResult} object.
     *
     * @param preparedSql        the {@link PreparedSql} object containing the SQL query string and bind values to be executed
     * @param connectionProvider the {@link ConnectionProvider} used to obtain a database connection.
     * @return an {@link UpdateResult} object encapsulating the number of rows affected by the update operation
     * @throws SQLException if an error occurs while executing the SQL update
     */
    protected UpdateResult executeSqlUpdate(final PreparedSql preparedSql, final TableMetaData tableMetaData, final ConnectionProvider connectionProvider) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, false, tableMetaData, connectionProvider)) {
            final int affectedRows = preparedStatement.executeUpdate();
            return new UpdateResult(affectedRows);
        }
    }

    /**
     * Execute the given SQL query with specified expressions, conditions, and table, and returns the result as a list of rows.
     *
     * @param select             the SQL select query to be executed
     * @param connectionProvider the {@link ConnectionProvider} used to obtain a database connection.
     * @return a list of {@code Row} objects representing the query results
     * @throws SQLException if an SQL error occurs while executing the query
     */
    private List<Row> executeSqlQuery(final Select select, final ConnectionProvider connectionProvider) throws SQLException {
        final PreparedSql preparedSql = selectSqlGenerator.orThrow().prepareSql(select, connectionProvider);
        final Map<String, ColumnMetaData> columnLabelsToColumnMetaData = new HashMap<>(select.expressions().size());
        final Class<?>[] typeOverrides = new Class<?>[select.expressions().size()];

        for (int i = 0; i < select.expressions().size(); i++) {
            SelectExpression expression = select.expressions().get(i);

            if (expression instanceof ConvertExpression convertExpression) {
                typeOverrides[i] = convertExpression.typeOverride();
                // Process the nested expression (in case it targets a lhs)
                expression = convertExpression.target();
            }

            if (expression instanceof ColumnExpression columnExpression) {
                final Column column = columnExpression.column();
                final String key = aliasTransformer.orThrow().transformAlias(column.alias() != null ? column.alias() : column.name());
                final TableMetaData table = ensureTableMetaData(column.table(), connectionProvider);
                final ColumnMetaData columnMetaData = table.column(column.name());
                columnLabelsToColumnMetaData.put(key, columnMetaData);
            }
        }

        final TableMetaData fromTable = ensureTableMetaData(select.table(), connectionProvider);

        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, false, fromTable, connectionProvider)) {
            // Execute SQL query
            final ResultSet resultSet = preparedStatement.executeQuery();

            // Parse results
            final List<Row> rows = new ArrayList<>();

            while (resultSet.next()) {
                final Row row = new Row();
                final int columnCount = resultSet.getMetaData().getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    final String alias = aliasTransformer.orThrow().transformAlias(resultSet.getMetaData().getColumnLabel(i));
                    final ColumnMetaData columnMetaData = columnLabelsToColumnMetaData.get(alias);
                    final int columnSqlType;
                    final Column column;

                    if (columnMetaData != null) {
                        // Use ORM-side metadata
                        columnSqlType = columnMetaData.getDataType();
                        column = columnMetaData.toColumn().as(alias);
                    } else {
                        // Read the metadata from the result
                        final String schemaName = resultSet.getMetaData().getSchemaName(i);
                        final String tableName = resultSet.getMetaData().getTableName(i);
                        final String columnName = resultSet.getMetaData().getColumnName(i);
                        final String columnAlias = resultSet.getMetaData().getColumnLabel(i);
                        columnSqlType = resultSet.getMetaData().getColumnType(i);

                        final Table table = new Table(null, schemaName, tableName);
                        column = new Column(table, columnName, columnAlias);
                    }

                    final Class<?> typeOverride = i <= typeOverrides.length ? typeOverrides[i - 1] : null;
                    final Object value;

                    if (typeOverride != null) {
                        // Override the data type
                        value = typeConverter.convert(resultSet.getObject(i), typeOverride);
                    } else {
                        // Use the lhs SQL data type
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
    public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
        return switch (operation) {
            case Select select -> selectSqlGenerator.orThrow().prepareSql(select, connectionProvider).sql();
            case Insert insert -> insertSqlGenerator.orThrow().prepareSql(insert, connectionProvider).sql();
            case Update update -> updateSqlGenerator.orThrow().prepareSql(update, connectionProvider).sql();
            case Delete delete -> deleteSqlGenerator.orThrow().prepareSql(delete, connectionProvider).sql();
        };
    }

    @Override
    public SqlFunctionRegistry getSqlFunctionRegistry() {
        return sqlFunctionRegistry.orThrow();
    }

    @Override
    public AliasTransformer getAliasTransformer() {
        return aliasTransformer.orThrow();
    }

    protected List<ColumnMetaData> getColumnMetaData(final Table table, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (final ResultSet dbColumns = databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)) {
            final List<ColumnMetaData> columns = new ArrayList<>();

            while (dbColumns.next()) {
                final String name = dbColumns.getString("COLUMN_NAME");
                final boolean nullable = dbColumns.getBoolean("IS_NULLABLE");
                final int dataType = dbColumns.getInt("DATA_TYPE");
                final int size = dbColumns.getInt("COLUMN_SIZE");
                final boolean isAutoincrement = dbColumns.getBoolean("IS_AUTOINCREMENT");
                final int decimalDigits = dbColumns.getInt("DECIMAL_DIGITS");

                columns.add(new ColumnMetaData(table, name, nullable, dataType, size, decimalDigits, isAutoincrement, null));
            }

            return columns;
        }
    }

    protected List<String> getPrimaryKeyColumnNames(final Table table, final DatabaseMetaData databaseMetaData) throws SQLException {
        try (final ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())) {
            final List<String> primaryKeyColumnNames = new ArrayList<>();

            while (primaryKeys.next()) {
                final String columnName = primaryKeys.getString("COLUMN_NAME");
                primaryKeyColumnNames.add(columnName);
            }

            return primaryKeyColumnNames;
        }
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
     * @param tableMetaData       Meta-data for the current table
     * @param connectionProvider  the {@link ConnectionProvider} used to obtain a database connection.
     * @return a {@link PreparedStatement} that is ready to be executed based on the provided SQL and bind values.
     * @throws SQLException if a database access error occurs or the preparation of the SQL statement fails.
     */
    protected PreparedStatement prepareStatement(final PreparedSql preparedSql,
                                                 final boolean returnGeneratedKeys,
                                                 final TableMetaData tableMetaData,
                                                 final ConnectionProvider connectionProvider) throws SQLException {
        if (getLogger().isTraceEnabled() && !CollectionUtils.isEmpty(preparedSql.bindValues())) {
            getLogger().trace("Generated SQL: {} with bind parameters: {}", preparedSql.sql(), preparedSql.bindValues().stream()
                    .filter(Objects::nonNull)
                    .map(bindValue -> bindValue.value() != null ? bindValue.value() : "<null>")
                    .toList());
        } else {
            getLogger().debug("Generated SQL: {}", preparedSql.sql());
        }

        final ManagedConnection connection = connectionProvider.connection();
        final PreparedStatement preparedStatement = createPreparedStatementUsingConnection(preparedSql, returnGeneratedKeys, tableMetaData, connection);

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
                    default -> preparedStatement.setObject(ordinal[0]++, bindValue.value(), bindValue.sqlDataType());
                }
            }
        }

        return preparedStatement;
    }

    protected PreparedStatement createPreparedStatementUsingConnection(final PreparedSql preparedSql,
                                                                       final boolean returnGeneratedKeys,
                                                                       final TableMetaData tableMetaData,
                                                                       final ManagedConnection connection) throws SQLException {
        if (returnGeneratedKeys) {
            final String[] generatedKeyNames = getGeneratedPrimaryKeyColumns(tableMetaData).stream()
                    .map(ColumnMetaData::name)
                    .toArray(String[]::new);

            return connection.prepareStatement(preparedSql.sql(), generatedKeyNames);
        } else {
            return connection.prepareStatement(preparedSql.sql());
        }
    }

    /**
     * Return the logger instance for this database provider.
     *
     * @return the logger instance
     */
    protected Logger getLogger() {
        return LOGGER;
    }

    protected TableMetaData ensureTableMetaData(final Table table, final ConnectionProvider connectionProvider) {
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
     * @param table the table for which metadata is being fetched, containing schema, catalog, and table name details
     * @return a {@code TableMetaData} object containing details about the table's structure, primary keys, and lhs metadata
     * @throws SQLException if an error occurs while fetching database metadata
     */
    protected TableMetaData fetchTableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
        final List<String> primaryKeys;
        final List<ColumnMetaData> columns;

        try (final Connection connection = connectionProvider.connection()) {
            final DatabaseMetaData databaseMetaData = connection.getMetaData();

            // Verify basic details
            verifySchemaAndTableExists(table, databaseMetaData);

            // Load table metadata
            primaryKeys = getPrimaryKeyColumnNames(table, databaseMetaData);
            columns = getColumnMetaData(table, databaseMetaData);
        }

        return new TableMetaData(table, primaryKeys, columns);
    }

    /**
     * Create a {@link SqlFunctionRegistryFactory} instance for the generation of a SQL function registry.
     * <p>
     * This method may be overridden to provide a custom {@link SqlFunctionRegistryFactory} instance, allowing
     * overriding/disabling of specific SQL functions.
     *
     * @return a {@link SqlFunctionRegistryFactory} implementation instance
     */
    protected SqlFunctionRegistryFactory createSqlFunctionRegistryFactory() {
        return new SqlFunctionRegistryFactory(columnIdentifierGenerator.orThrow(), selectSqlGenerator.orThrow());
    }

    protected AliasTransformer createAliasTransformer() {
        return new UppercaseAliasTransformer();
    }

    protected ColumnIdentifierGenerator createColumnIdentifierGenerator() {
        return new ColumnIdentifierGenerator();
    }

    protected SelectSqlGenerator createSelectSqlGenerator() {
        return new SelectSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }

    protected InsertSqlGenerator createInsertSqlGenerator() {
        return new InsertSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }

    protected UpdateSqlGenerator createUpdateSqlGenerator() {
        return new UpdateSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }

    protected DeleteSqlGenerator createDeleteSqlGenerator() {
        return new DeleteSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }
}
