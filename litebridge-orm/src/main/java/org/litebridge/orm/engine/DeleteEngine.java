package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.delete.DeleteTerminalInspector;
import org.litebridge.orm.api.delete.DtoDeleteStart;
import org.litebridge.orm.api.delete.SqlDeleteStart;
import org.litebridge.orm.engine.ast.QueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public class DeleteEngine extends AbstractUpdateEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteEngine.class);

    public <DTO> UpdateResult delete(final Class<DTO> dtoClass,
                                     final Function<DtoDeleteStart<DTO>, DeleteTerminal> delete,
                                     final LitebridgeContext litebridgeContext) {
        final DtoDeleteStart<DTO> dtoDeleteStart = new DtoDeleteStart<>(dtoClass, litebridgeContext);
        final DeleteTerminal deleteTerminal = delete.apply(dtoDeleteStart);
        final QueryNode node = DeleteTerminalInspector.getNode(deleteTerminal);
        return execute(node, litebridgeContext);
    }

    public UpdateResult delete(final String tableName,
                               final Function<SqlDeleteStart, DeleteTerminal> delete,
                               final LitebridgeContext litebridgeContext) {
        final SqlDeleteStart sqlDeleteStart = new SqlDeleteStart(tableName, litebridgeContext);
        final DeleteTerminal deleteTerminal = delete.apply(sqlDeleteStart);
        final QueryNode node = DeleteTerminalInspector.getNode(deleteTerminal);
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
        final Delete delete = (Delete) preparedOperation.operation();
        // Generate SQL and create type conversion metadata
        final String sql = litebridgeContext.databaseProvider().toSql(delete, litebridgeContext.transactionManager());
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
            throw new IllegalStateException("Failed to execute delete: " + preparedSql.sql(), ex);
        }

        LOGGER.debug("Delete result: {}", updateResult);
        return updateResult;
    }
}
