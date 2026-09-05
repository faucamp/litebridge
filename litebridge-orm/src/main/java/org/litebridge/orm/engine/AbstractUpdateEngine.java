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
import java.util.function.Supplier;

abstract sealed class AbstractUpdateEngine permits AbstractInsertEngine, DeleteEngine, UpdateEngine {

    protected static final UpdateMetaData EMPTY_UPDATE_META_DATA = new UpdateMetaData(false, Collections.emptyList(), new String[0]);

    protected abstract String operationTypeName();

    protected abstract Logger logger();

    protected final UpdateResult execute(final QueryNode node,
                                         final LitebridgeContext litebridgeContext) {
        return execute(node, () -> EMPTY_UPDATE_META_DATA, UpdateResult.class, litebridgeContext);
    }

    protected final <T extends UpdateResult> T execute(final QueryNode node,
                                                       final Supplier<UpdateMetaData> updateMetaDataSupplier,
                                                       final Class<T> resultType,
                                                       final LitebridgeContext litebridgeContext) {
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> bindValues = QueryBindValueExtractor.extractBindValues(node);
            return execute(cachedOperation.preparedSql(bindValues), resultType, litebridgeContext);
        } else {
            return compileAndExecute(nodeHash, node, updateMetaDataSupplier, resultType, litebridgeContext);
        }
    }

    protected final <T extends UpdateResult> T compileAndExecute(final int astCacheKey,
                                                                 final QueryNode node,
                                                                 final Supplier<UpdateMetaData> updateMetaDataSupplier,
                                                                 final Class<T> resultType,
                                                                 final LitebridgeContext litebridgeContext) {
        // Compile/prepare SQL query
        final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(node);
        // Generate SQL and create type conversion metadata
        final String sql = litebridgeContext.databaseProvider().toSql(preparedOperation.operation(), litebridgeContext.transactionManager());
        // Cache compiled SQL for this AST
        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, EMPTY_UPDATE_META_DATA));
        // Execute SQL query
        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, updateMetaDataSupplier.get());
        return execute(executionSql, resultType, litebridgeContext);
    }

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
