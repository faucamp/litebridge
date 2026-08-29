package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.update.DtoUpdateStart;
import org.litebridge.orm.api.update.SqlUpdateStart;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateQueryInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public class UpdateEngine extends AbstractUpdateEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateEngine.class);

    public <DTO> UpdateResult update(final Class<DTO> dtoClass,
                                     final Function<DtoUpdateStart<DTO>, UpdateQuery> update,
                                     final LitebridgeContext litebridgeContext) {
        final DtoUpdateStart<DTO> dtoDtoUpdateStart = new DtoUpdateStart<>(dtoClass, litebridgeContext);
        final UpdateQuery updateQuery = update.apply(dtoDtoUpdateStart);
        final QueryNode node = UpdateQueryInspector.getNode(updateQuery);
        return execute(node, litebridgeContext);
    }

    public UpdateResult update(final String tableName,
                               final Function<SqlUpdateStart, UpdateQuery> update,
                               final LitebridgeContext litebridgeContext) {
        final SqlUpdateStart sqlUpdateStart = new SqlUpdateStart(tableName, litebridgeContext);
        final UpdateQuery updateQuery = update.apply(sqlUpdateStart);
        final QueryNode node = UpdateQueryInspector.getNode(updateQuery);
        return execute(node, litebridgeContext);
    }

    private UpdateResult execute(final QueryNode node, final LitebridgeContext litebridgeContext) {
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> bindValues = QueryBindValueExtractor.extractBindValues(node);
            return execute(cachedOperation.preparedSql(bindValues), litebridgeContext);
        } else {
            return compileAndExecute(nodeHash, node, litebridgeContext);
        }
    }

    private UpdateResult compileAndExecute(final int astCacheKey, final QueryNode node, final LitebridgeContext litebridgeContext) {
        // Compile/prepare SQL query
        final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(node);
        final Update update = (Update) preparedOperation.operation();
        // Generate SQL and create type conversion metadata
        final String sql = litebridgeContext.databaseProvider().toSql(update, litebridgeContext.transactionManager());
        // Cache compiled SQL for this AST
        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, UPDATE_META_DATA));
        // Execute SQL query
        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, UPDATE_META_DATA);
        return execute(executionSql, litebridgeContext);
    }

    private UpdateResult execute(final PreparedSql preparedSql, final LitebridgeContext litebridgeContext) {
        final UpdateResult updateResult;

        try {
            updateResult = litebridgeContext.databaseProvider().insert(preparedSql, litebridgeContext.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute update: " + preparedSql.sql(), ex);
        }

        LOGGER.debug("Update result: {}", updateResult);
        return updateResult;
    }
}
