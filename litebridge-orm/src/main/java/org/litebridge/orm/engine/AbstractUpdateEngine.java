package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.engine.ast.QueryNode;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Provides common functionality for compiling, caching and executing SQL statements.
 */
abstract sealed class AbstractUpdateEngine permits AbstractInsertEngine, DeleteEngine, UpdateEngine {

    protected static final UpdateMetaData EMPTY_UPDATE_META_DATA = new UpdateMetaData(false, Collections.emptyList(), new String[0]);

    protected abstract String operationTypeName();

    protected abstract Logger logger();

    /**
     * Executes a given query node within the provided Litebridge context and returns
     * the result of the operation.
     *
     * @param node              The query node representing a step in the query chain
     *                          to be executed.
     * @param litebridgeContext Context for the statement execution.
     * @return The result of executing the query node, represented as an instance of
     * {@link UpdateResult}, which includes information such as the number of
     * rows affected by the operation.
     * @throws IllegalStateException If an error occurs during query execution,
     *                               such as issues with the database or query compilation.
     */
    protected final UpdateResult execute(final QueryNode node,
                                         final LitebridgeContext litebridgeContext) {
        return execute(node, preparedOperation -> EMPTY_UPDATE_META_DATA, UpdateResult.class, litebridgeContext);
    }

    /**
     * Executes a given query node, either by using a cached prepared SQL statement
     * or by compiling and executing it if no cache exists.
     * <p>
     * The result is returned as an instance of the specified result type.
     *
     * @param <T>                   The type of the result extending {@link UpdateResult}.
     * @param node                  The query node to be executed.
     * @param updateMetaDataCreator A function to create update metadata for the operation.
     * @param resultType            The class type of the result to return; must extend {@link UpdateResult}.
     * @param litebridgeContext     Context for the statement execution.
     * @return The result of executing the query node, represented as an instance of the specified result type.
     * This result contains information about the outcome of the operation, such as the number of rows affected.
     * @throws IllegalStateException If an error occurs during SQL execution, including database or query issues.
     */
    protected final <T extends UpdateResult> T execute(final QueryNode node,
                                                       final Function<PreparedOperation, UpdateMetaData> updateMetaDataCreator,
                                                       final Class<T> resultType,
                                                       final LitebridgeContext litebridgeContext) {
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> bindValues = QueryBindValueExtractor.extractBindValues(node);
            return execute(cachedOperation.preparedSql(bindValues), resultType, litebridgeContext);
        } else {
            return compileAndExecute(nodeHash, node, updateMetaDataCreator, resultType, litebridgeContext);
        }
    }

    /**
     * Compiles and executes the provided query node against the database.
     *
     * @param astCacheKey           The cache key for the AST.
     * @param node                  The query node to be compiled and executed.
     * @param updateMetaDataCreator A function to create update metadata.
     * @param resultType            The result type to return.
     * @param litebridgeContext     The context for the statement execution.
     * @param <T>                   The type of the result.
     * @return The result of the database operation, which is an instance of the specified result type.
     * @throws IllegalStateException If the execution of the SQL statement fails, including database or query issues.
     */
    protected final <T extends UpdateResult> T compileAndExecute(final int astCacheKey,
                                                                 final QueryNode node,
                                                                 final Function<PreparedOperation, UpdateMetaData> updateMetaDataCreator,
                                                                 final Class<T> resultType,
                                                                 final LitebridgeContext litebridgeContext) {
        // Compile/prepare SQL query
        final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(node);

        // Generate SQL and create type conversion metadata
        final String sql = litebridgeContext.databaseProvider().toSql(preparedOperation.operation(), litebridgeContext.transactionManager());
        final UpdateMetaData updateMetaData = updateMetaDataCreator.apply(preparedOperation);

        // Cache compiled SQL for this AST
        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, updateMetaData));

        // Execute SQL query
        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, updateMetaData);
        return execute(executionSql, resultType, litebridgeContext);
    }

    /**
     * Executes the provided prepared SQL statement against the database and returns the result of the operation.
     *
     * @param <T>               The type of the result.
     * @param preparedSql       The prepared SQL statement containing the SQL query and its bind parameters.
     * @param resultType        The result type to return.
     * @param litebridgeContext Context for the statement execution.
     * @return The result of the database operation, which is an instance of the specified result type.
     * @throws IllegalStateException If the execution of the SQL statement fails, including database or query issues.
     */
    protected final <T extends UpdateResult> T execute(final PreparedSql preparedSql, final Class<T> resultType, final LitebridgeContext litebridgeContext) {
        final T updateResult;

        try {
            updateResult = litebridgeContext.databaseProvider().executeUpdate(preparedSql, resultType, litebridgeContext.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute %s: %s".formatted(operationTypeName(), preparedSql.sql()), ex);
        }

        logger().debug("{} result: {}", operationTypeName(), updateResult);
        return updateResult;
    }
}
