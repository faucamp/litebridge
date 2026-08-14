package org.litebridge.db.spi.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.ForeignKeyConstraint;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridge.db.spi.impl.function.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.DeleteSqlGenerator;
import org.litebridge.db.spi.impl.sql.InsertSqlGenerator;
import org.litebridge.db.spi.impl.sql.MergeSqlGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.impl.sql.UpdateSqlGenerator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.InsertV2;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
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
import java.util.Collections;
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

    /**
     * The type converter used for mapping between database and Java types.
     */
    protected final TypeConverter typeConverter;

    /**
     * Map of qualified table name -> table metadata.
     */
    private final Map<String, TableMetaData> tableMetaDataCache = new ConcurrentHashMap<>();

    /**
     * Lazy-loaded SQL function registry.
     */
    private final ConcurrentLazy<SqlFunctionRegistry> sqlFunctionRegistry = new ConcurrentLazy<>(() -> createSqlFunctionRegistryFactory().create());

    /**
     * Lazy-loaded alias transformer.
     */
    private final ConcurrentLazy<AliasTransformer> aliasTransformer = new ConcurrentLazy<>(this::createAliasTransformer);

    /**
     * Lazy-loaded column identifier generator.
     */
    protected final ConcurrentLazy<ColumnIdentifierGenerator> columnIdentifierGenerator = new ConcurrentLazy<>(this::createColumnIdentifierGenerator);

    /**
     * Lazy-loaded SELECT SQL generator.
     */
    protected final ConcurrentLazy<SelectSqlGenerator> selectSqlGenerator = new ConcurrentLazy<>(this::createSelectSqlGenerator);

    /**
     * Lazy-loaded INSERT SQL generator.
     */
    protected final ConcurrentLazy<InsertSqlGenerator> insertSqlGenerator = new ConcurrentLazy<>(this::createInsertSqlGenerator);

    /**
     * Lazy-loaded UPDATE SQL generator.
     */
    protected final ConcurrentLazy<UpdateSqlGenerator> updateSqlGenerator = new ConcurrentLazy<>(this::createUpdateSqlGenerator);

    /**
     * Lazy-loaded DELETE SQL generator.
     */
    protected final ConcurrentLazy<DeleteSqlGenerator> deleteSqlGenerator = new ConcurrentLazy<>(this::createDeleteSqlGenerator);

    /**
     * Lazy-loaded MERGE SQL generator.
     */
    protected final ConcurrentLazy<MergeSqlGenerator> mergeSqlGenerator = new ConcurrentLazy<>(this::createMergeSqlGenerator);

    /**
     * Constructs a new {@code AbstractDatabaseProvider}.
     *
     * @param typeConverter The type converter to use.
     */
    public AbstractDatabaseProvider(final TypeConverter typeConverter) {
        this.typeConverter = Objects.requireNonNull(typeConverter, "No TypeConverter provided");
    }

    @Override
    public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
        return ensureTableMetaData(table, connectionProvider);
    }

    @Override
    public InsertResult insert(final PreparedSql insert, final ConnectionProvider connectionProvider) throws SQLException {
        return executeSqlInsert(insert, connectionProvider);
    }

    @Override
    public UpdateResult update(final PreparedSql update, final ConnectionProvider connectionProvider) throws SQLException {
        return executeSqlUpdate(update, connectionProvider);
    }

    @Override
    public UpdateResult delete(final PreparedSql delete, final ConnectionProvider connectionProvider) throws SQLException {
        return executeSqlUpdate(delete, connectionProvider);
    }

    @Override
    public UpdateResult merge(final PreparedSql merge, final ConnectionProvider connectionProvider) throws SQLException {
        return executeSqlUpdate(merge, connectionProvider);
    }

    @Override
    public List<Row> select(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        final Map<String, ColumnMetaData> columnLabelsToColumnMetaData;
        final Class<?>[] typeOverrides;

        if (preparedSql.typeConversionMetaData() != null) {
            columnLabelsToColumnMetaData = preparedSql.typeConversionMetaData().columnLabelsToColumnMetaData();
            typeOverrides = preparedSql.typeConversionMetaData().typeOverrides();
        } else {
            columnLabelsToColumnMetaData = Collections.emptyMap();
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

                for (int i = 1; i <= columnCount; i++) {
                    final String alias = Objects.requireNonNull(aliasTransformer.orThrow().transformAlias(resultSet.getMetaData().getColumnLabel(i)));
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
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    @Override
    public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return new DefaultSequenceColumnValueGenerator(sequence);
    }

    @Override
    public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
        return switch (operation) {
            case Select select -> selectSqlGenerator.orThrow().prepareSql(select, connectionProvider);
            case Insert insert -> insertSqlGenerator.orThrow().prepareSql(insert, connectionProvider);
            case InsertV2 insert -> insertSqlGenerator.orThrow().prepareSql(insert, connectionProvider);
            case Update update -> updateSqlGenerator.orThrow().prepareSql(update, connectionProvider);
            case Delete delete -> deleteSqlGenerator.orThrow().prepareSql(delete, connectionProvider);
            case Merge merge -> mergeSqlGenerator.orThrow().prepareSql(merge, connectionProvider);
        };
    }

    @Override
    public List<Row> nativeSqlQuery(final String sql, final List<@Nullable Object> bindParameters, ConnectionProvider connectionProvider) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareNativeStatement(sql, bindParameters, false, connectionProvider)) {
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
                    final String columnName = Objects.requireNonNull(aliasTransformer.orThrow().transformAlias(resultSet.getMetaData().getColumnName(i)));
                    final String columnAlias = Objects.requireNonNull(aliasTransformer.orThrow().transformAlias(resultSet.getMetaData().getColumnLabel(i)));
                    final int columnSqlType = resultSet.getMetaData().getColumnType(i);

                    final Table table = new Table(null, schemaName, tableName);
                    final Column column = new Column(table, columnName, columnAlias);

                    final Object value = typeConverter.convert(resultSet.getObject(i), columnSqlType);
                    row.withColumn(column, value);
                }

                rows.add(row);
            }

            return rows;
        }
    }

    @Override
    public UpdateResult nativeSqlUpdate(final String sql, final List<@Nullable Object> bindParameters, final ConnectionProvider connectionProvider) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareNativeStatement(sql, bindParameters, false, connectionProvider)) {
            final int updateCount = preparedStatement.executeUpdate();
            return new UpdateResult(updateCount);
        }
    }

    @Override
    public SqlFunctionRegistry getSqlFunctionRegistry() {
        return sqlFunctionRegistry.orThrow();
    }

    @Override
    public AliasTransformer getAliasTransformer() {
        return aliasTransformer.orThrow();
    }

    /**
     * Execute a SQL INSERT operation using the provided prepared SQL statement and table metadata.
     * <p>
     * This method executes the prepared statement, retrieves any generated primary key values,
     * and wraps the results in an {@link InsertResult} object.
     *
     * @param preparedSql        the {@link PreparedSql} object containing the SQL query string and bind values to be executed
     * @param connectionProvider the {@link ConnectionProvider} used to obtain a database connection.
     * @return an {@link InsertResult} object encapsulating the number of affected rows and a list of generated keys (if any)
     * @throws SQLException if an error occurs while executing the SQL insert or retrieving the generated keys
     */
    protected InsertResult executeSqlInsert(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
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
    protected UpdateResult executeSqlUpdate(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        try (final PreparedStatement preparedStatement = prepareStatement(preparedSql, connectionProvider)) {
            final int affectedRows = preparedStatement.executeUpdate();
            return new UpdateResult(affectedRows);
        }
    }


    /**
     * Retrieve column metadata for the specified table.
     *
     * @param table            the {@link Table} object representing the table
     * @param databaseMetaData the {@link DatabaseMetaData} object used to retrieve column information
     * @return a list of {@link ColumnMetaData} objects representing the table's columns
     * @throws SQLException if an error occurs while retrieving column metadata
     */
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
                String defaultValue = dbColumns.getString("COLUMN_DEF");

                // Remove the enclosing ' character from string defaults
                if (defaultValue != null
                        && defaultValue.length() >= 2
                        && (dataType == Types.VARCHAR || dataType == Types.CHAR || dataType == Types.LONGVARCHAR)
                        && defaultValue.charAt(0) == '\''
                        && defaultValue.charAt(defaultValue.length() - 1) == '\'') {
                    defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                }

                columns.add(new ColumnMetaData(table, name, nullable, dataType, size, decimalDigits, isAutoincrement, defaultValue, null));
            }

            return columns;
        }
    }

    /**
     * Retrieve the names of the primary key columns for the specified table.
     *
     * @param table            the {@link Table} object representing the table
     * @param databaseMetaData the {@link DatabaseMetaData} object used to retrieve primary key information
     * @return a list of primary key column names
     * @throws SQLException if an error occurs while retrieving primary key information
     */
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

    /**
     * Verify that the specified schema and table exist in the database.
     *
     * @param table            the {@link Table} object representing the table to verify
     * @param databaseMetaData the {@link DatabaseMetaData} object used to perform the verification
     * @throws SQLException             if an error occurs while performing the verification
     * @throws IllegalArgumentException if the schema or table is not found
     */
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

        final ManagedConnection connection = connectionProvider.connection();
        final PreparedStatement preparedStatement = createPreparedStatementUsingConnection(preparedSql, connection);

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

    /**
     * Prepares a native SQL statement with the provided bind parameters.
     *
     * @param sql                 the SQL query to prepare
     * @param bindParameters      the list of parameters to bind to the query
     * @param returnGeneratedKeys whether to return generated keys
     * @param connectionProvider  the provider to use for obtaining a connection
     * @return the prepared statement
     * @throws SQLException if a database access error occurs
     */
    protected PreparedStatement prepareNativeStatement(final String sql,
                                                       final List<@Nullable Object> bindParameters,
                                                       final boolean returnGeneratedKeys,
                                                       final ConnectionProvider connectionProvider) throws SQLException {
        if (getLogger().isTraceEnabled() && !CollectionUtils.isEmpty(bindParameters)) {
            getLogger().trace("Executing native SQL: {} with bind parameters: {}", sql, bindParameters.stream()
                    .map(bindParam -> bindParam != null ? bindParam : "<null>")
                    .toList());
        } else {
            getLogger().debug("Executing native SQL: {}", sql);
        }

        final PreparedStatement preparedStatement;
        try (ManagedConnection connection = connectionProvider.connection()) {
            preparedStatement = connection.prepareStatement(sql);

            final int[] ordinal = {1};

            if (!CollectionUtils.isEmpty(bindParameters)) {
                for (Object bindParameter : bindParameters) {
                    if (bindParameter == null) {
                        preparedStatement.setString(ordinal[0]++, null);
                        continue;
                    }

                    switch (bindParameter) {
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
                        default -> preparedStatement.setObject(ordinal[0]++, bindParameter);
                    }
                }
            }
        }

        return preparedStatement;
    }

    /**
     * Creates a {@link PreparedStatement} using the provided connection and prepared SQL.
     *
     * @param preparedSql the SQL and bind values to use
     * @param connection  the connection to use for preparing the statement
     * @return the created prepared statement
     * @throws SQLException if a database access error occurs
     */
    protected PreparedStatement createPreparedStatementUsingConnection(final PreparedSql preparedSql,
                                                                       final ManagedConnection connection) throws SQLException {
        final UpdateMetaData updateMetaData = preparedSql.updateMetaData();

        if (updateMetaData == null) {
            return connection.prepareStatement(preparedSql.sql());
        }

        if (updateMetaData.returnGeneratedKeys()) {
            final String[] generatedKeyNames = updateMetaData.generatedKeys().stream()
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

    /**
     * Ensure that table metadata is available for the specified table, fetching it if not already cached.
     *
     * @param table              the {@link Table} object representing the table
     * @param connectionProvider the {@link ConnectionProvider} used to fetch metadata if needed
     * @return the {@link TableMetaData} for the table
     */
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
     * @param table              the table for which metadata is being fetched, containing schema, catalog, and table name details
     * @param connectionProvider the {@link ConnectionProvider} used to obtain a database connection.
     * @return a {@code TableMetaData} object containing details about the table's structure, primary keys, and column metadata
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

            try (ResultSet rs = databaseMetaData.getImportedKeys(table.catalog(), table.schema(), table.name())) {
                while (rs.next()) {
                    final String fkName = rs.getString("FK_NAME");

                    // Remote table
                    final String pkTable = rs.getString("PKTABLE_NAME");
                    final String pkColumn = rs.getString("PKCOLUMN_NAME");

                    // Local table
                    final String fkTable = rs.getString("FKTABLE_NAME");
                    final String fkColumn = rs.getString("FKCOLUMN_NAME");

                    if (table.name().equals(fkTable)) {
                        columns.stream()
                                .filter(column -> column.name().equals(fkColumn))
                                .forEach(column -> {
                                    final ForeignKeyConstraint constraint = new ForeignKeyConstraint(fkName, new Column(new Table(table.catalog(), table.schema(), pkTable), pkColumn));
                                    column.addForeignKeyConstraint(constraint);
                                });
                    }
                }
            }

            try (ResultSet rs = databaseMetaData.getExportedKeys(table.catalog(), table.schema(), table.name())) {
                while (rs.next()) {
                    final String fkName = rs.getString("FK_NAME");

                    // Local table
                    final String pkTable = rs.getString("PKTABLE_NAME");
                    final String pkColumn = rs.getString("PKCOLUMN_NAME");

                    // Remote table
                    final String fkTable = rs.getString("FKTABLE_NAME");
                    final String fkColumn = rs.getString("FKCOLUMN_NAME");

                    if (table.name().equals(pkTable)) {
                        columns.stream()
                                .filter(column -> column.name().equals(pkColumn))
                                .forEach(column -> {
                                    final ForeignKeyConstraint constraint = new ForeignKeyConstraint(fkName, new Column(new Table(table.catalog(), table.schema(), fkTable), fkColumn));
                                    column.addForeignReference(constraint);
                                });
                    }
                }
            }
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

    /**
     * Create an {@link AliasTransformer} instance for the database provider.
     *
     * @return an {@link AliasTransformer} instance
     */
    protected AliasTransformer createAliasTransformer() {
        return new UppercaseAliasTransformer();
    }

    /**
     * Create a {@link ColumnIdentifierGenerator} instance for the database provider.
     *
     * @return a {@link ColumnIdentifierGenerator} instance
     */
    protected ColumnIdentifierGenerator createColumnIdentifierGenerator() {
        return new ColumnIdentifierGenerator();
    }

    /**
     * Create a {@link SelectSqlGenerator} instance for the database provider.
     *
     * @return a {@link SelectSqlGenerator} instance
     */
    protected SelectSqlGenerator createSelectSqlGenerator() {
        return new SelectSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }

    /**
     * Create an {@link InsertSqlGenerator} instance for the database provider.
     *
     * @return an {@link InsertSqlGenerator} instance
     */
    protected InsertSqlGenerator createInsertSqlGenerator() {
        return new InsertSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }

    /**
     * Create an {@link UpdateSqlGenerator} instance for the database provider.
     *
     * @return an {@link UpdateSqlGenerator} instance
     */
    protected UpdateSqlGenerator createUpdateSqlGenerator() {
        return new UpdateSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }

    /**
     * Create a {@link DeleteSqlGenerator} instance for the database provider.
     *
     * @return a {@link DeleteSqlGenerator} instance
     */
    protected DeleteSqlGenerator createDeleteSqlGenerator() {
        return new DeleteSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }

    /**
     * Create a {@link MergeSqlGenerator} instance for the database provider.
     *
     * @return a {@link MergeSqlGenerator} instance
     */
    protected MergeSqlGenerator createMergeSqlGenerator() {
        return new MergeSqlGenerator(typeConverter,
                columnIdentifierGenerator.orThrow(),
                this::ensureTableMetaData,
                insertSqlGenerator.orThrow(),
                updateSqlGenerator.orThrow(),
                deleteSqlGenerator.orThrow());
    }
}
