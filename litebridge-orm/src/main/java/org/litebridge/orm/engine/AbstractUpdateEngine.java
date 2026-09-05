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

abstract sealed class AbstractUpdateEngine permits DeleteEngine, UpdateEngine {

    protected static final UpdateMetaData UPDATE_META_DATA = new UpdateMetaData(false, Collections.emptyList(), new String[0]);

    protected abstract String operationTypeName();

    protected abstract Logger logger();

    protected UpdateResult execute(final QueryNode node, final LitebridgeContext litebridgeContext) {
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> bindValues = QueryBindValueExtractor.extractBindValues(node);
            return execute(cachedOperation.preparedSql(bindValues), litebridgeContext);
        } else {
            return compileAndExecute(nodeHash, node, litebridgeContext);
        }
    }

    protected UpdateResult compileAndExecute(final int astCacheKey, final QueryNode node, final LitebridgeContext litebridgeContext) {
        // Compile/prepare SQL query
        final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(node);
        // Generate SQL and create type conversion metadata
        final String sql = litebridgeContext.databaseProvider().toSql(preparedOperation.operation(), litebridgeContext.transactionManager());
        // Cache compiled SQL for this AST
        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, UPDATE_META_DATA));
        // Execute SQL query
        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, UPDATE_META_DATA);
        return execute(executionSql, litebridgeContext);
    }

    protected UpdateResult execute(final PreparedSql preparedSql, final LitebridgeContext litebridgeContext) {
        final UpdateResult updateResult;

        try {
            updateResult = litebridgeContext.databaseProvider().update(preparedSql, litebridgeContext.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute %s: %s".formatted(operationTypeName(), preparedSql.sql()), ex);
        }

        logger().debug("{} result: {}", operationTypeName(), updateResult);
        return updateResult;
    }
}
