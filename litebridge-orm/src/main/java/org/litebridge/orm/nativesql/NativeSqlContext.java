package org.litebridge.orm.nativesql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.BatchUpdateResult;
import org.litebridge.db.spi.update.UpdateOpResult;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Provides a context for executing native SQL queries and updates in a transactional database environment.
 * <p>
 * This class facilitates the execution of raw SQL operations using the capabilities provided
 * by a {@code TransactionalDatabaseProvider}.
 * <p>
 * It supports parameterised SQL queries and updates with flexible parameter binding and ensures that all
 * database interactions are performed in a transactional context.
 */
public final class NativeSqlContext {

    private final NativeSqlCache nativeSqlCache = new NativeSqlCache();
    private final TransactionalDatabaseProvider databaseProvider;

    /**
     * Constructs a new {@code NativeSqlContext} instance.
     *
     * @param databaseProvider the database provider to use for executing SQL operations
     */
    public NativeSqlContext(final TransactionalDatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    /**
     * Executes a SQL query with the given SQL string and a variable-length list of positional bind parameters.
     * <p>
     * This method takes a variable number of parameters and delegates to the overloaded
     * query method that accepts a list of bind parameters.
     *
     * @param sql            the SQL query string to be executed; must not be {@code null}
     * @param bindParameters the variable-length list of parameters to bind to the query
     * @return a list of {@code Row} objects representing the result set of the query
     * @throws IllegalStateException if an error occurs during query execution
     * @see NativeSqlContext#query(String, List) which this method delegates to
     */
    public List<Row> query(final String sql, final Object... bindParameters) {
        final ParsedSql parsedSql = nativeSqlCache.getCachedSql(sql);
        final PreparedSql preparedSql = parsedSql.prepareSql(bindParameters);
        return query(preparedSql);
    }

    /**
     * Executes a SQL query with the given SQL string and a list of positional bind parameters.
     *
     * @param sql            the SQL query string to be executed; must not be {@code null}
     * @param bindParameters the list of parameters to bind to the query
     * @return a list of {@code Row} objects representing the result set of the query
     * @throws IllegalStateException if an error occurs during query execution
     * @see NativeSqlContext#query(String, Map) for a named bind parameter-based alternative
     */
    public List<Row> query(final String sql, final List<@Nullable Object> bindParameters) {
        final ParsedSql parsedSql = nativeSqlCache.getCachedSql(sql);
        final PreparedSql preparedSql = parsedSql.prepareSql(bindParameters);
        return query(preparedSql);
    }

    /**
     * Executes a SQL query with the given SQL string and a map of named bind parameters.
     *
     * @param sql            the SQL query string to be executed; must not be {@code null}
     * @param bindParameters the map of named parameters to bind to the query
     * @return a list of {@code Row} objects representing the result set of the query
     * @throws IllegalStateException if an error occurs during query execution
     * @see NativeSqlContext#query(String, List) for a positional bind parameter-based alternative
     */
    public List<Row> query(final String sql, final Map<String, @Nullable Object> bindParameters) {
        final ParsedSql parsedSql = nativeSqlCache.getCachedSql(sql);
        final PreparedSql preparedSql = parsedSql.prepareSql(bindParameters);
        return query(preparedSql);
    }

    /**
     * Executes a SQL update statement with the given SQL string and a variable-length list of positional bind parameters.
     * <p>
     * This method delegates to the overloaded execute method that accepts a list of bind parameters.
     *
     * @param sql            the SQL update statement to execute; must not be {@code null}
     * @param bindParameters the variable-length list of positional parameters to bind to the statement
     * @return an {@code UpdateResult} object that encapsulates the outcome of the update operation
     * @throws IllegalStateException if an error occurs while executing the update statement
     * @see NativeSqlContext#execute(String, List) which this method delegates to
     */
    public UpdateResult execute(final String sql, final Object... bindParameters) {
        final ParsedSql parsedSql = nativeSqlCache.getCachedSql(sql);
        final PreparedSql preparedSql = parsedSql.prepareSql(bindParameters);
        return execute(preparedSql);
    }

    /**
     * Executes a SQL update statement with the given SQL string and a list of positional bind parameters.
     *
     * @param sql            the SQL update statement to execute; must not be {@code null}
     * @param bindParameters the variable-length list of positional parameters to bind to the statement
     * @return an {@code UpdateResult} object that encapsulates the outcome of the update operation
     * @throws IllegalStateException if an error occurs while executing the update statement
     * @see NativeSqlContext#execute(String, Map) for a named bind parameter-based alternative
     */
    public UpdateResult execute(final String sql, final List<@Nullable Object> bindParameters) {
        final ParsedSql parsedSql = nativeSqlCache.getCachedSql(sql);
        final PreparedSql preparedSql = parsedSql.prepareSql(bindParameters);
        return execute(preparedSql);
    }

    /**
     * Executes a SQL update statement with the given SQL string and a map of named bind parameters.
     *
     * @param sql            the SQL update statement to execute; must not be {@code null}
     * @param bindParameters the map of named parameters to bind to the statement
     * @return an {@code UpdateResult} object that encapsulates the outcome of the update operation
     * @throws IllegalStateException if an error occurs while executing the update statement
     * @see NativeSqlContext#execute(String, List) for a positional bind parameter-based alternative
     */
    public UpdateResult execute(final String sql, final Map<String, @Nullable Object> bindParameters) {
        final ParsedSql parsedSql = nativeSqlCache.getCachedSql(sql);
        final PreparedSql preparedSql = parsedSql.prepareSql(bindParameters);
        return execute(preparedSql);
    }

    /**
     * Executes a batch SQL update statement with the given SQL string and a per-row list containing per-row positional bind parameters.
     *
     * @param sql               the SQL update statement to execute; must not be {@code null}
     * @param rowBindParameters the list of positional parameters to bind to the statement, per row
     * @return the outcome of the batch update operation
     * @throws IllegalStateException if an error occurs while executing the update statement
     * @see NativeSqlContext#executeNamedBatch(String, List) for a named bind parameter-based alternative
     */
    public BatchUpdateResult executeBatch(final String sql, final List<List<Object>> rowBindParameters) {
        final ParsedSql parsedSql = nativeSqlCache.getCachedSql(sql);
        final List<PreparedSql> operations = rowBindParameters.stream()
                .map(parsedSql::prepareSql)
                .toList();
        return executeBatch(operations);
    }

    /**
     * Executes a batch SQL update statement with the given SQL string and a per-row list containing named bind parameters.
     *
     * @param sql               the SQL update statement to execute; must not be {@code null}
     * @param rowBindParameters the list of named parameters to bind to the statement, per row
     * @return the outcome of the batch update operation
     * @throws IllegalStateException if an error occurs while executing the update statement
     * @see NativeSqlContext#executeBatch(String, List) for a positional bind parameter alternative
     */
    public BatchUpdateResult executeNamedBatch(final String sql, final List<Map<String, @Nullable Object>> rowBindParameters) {
        final ParsedSql parsedSql = nativeSqlCache.getCachedSql(sql);
        final List<PreparedSql> operations = rowBindParameters.stream()
                .map(parsedSql::prepareSql)
                .toList();
        return executeBatch(operations);
    }

    private UpdateResult execute(final PreparedSql preparedSql) {
        try {
            return databaseProvider.executeUpdate(preparedSql, UpdateResult.class, databaseProvider.transactionManager());
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to execute raw SQL: " + preparedSql.sql(), ex);
        }
    }

    private List<Row> query(final PreparedSql preparedSql) {
        try {
            return databaseProvider.executeQuery(preparedSql, databaseProvider.transactionManager());
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to execute raw SQL: " + preparedSql.sql(), ex);
        }
    }

    private BatchUpdateResult executeBatch(final List<PreparedSql> operations) {
        try {
            return databaseProvider.executeBatch(operations, databaseProvider.transactionManager());
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to execute batched raw SQL: " + operations.getFirst().sql(), ex);
        }
    }
}
