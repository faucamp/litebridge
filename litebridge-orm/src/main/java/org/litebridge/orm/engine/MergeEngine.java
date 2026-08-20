package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.merge.DtoMergeUsingStep;
import org.litebridge.orm.api.merge.MergeTerminal;
import org.litebridge.orm.api.merge.MergeTerminalInspector;
import org.litebridge.orm.api.merge.SqlMergeUsingStep;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public class MergeEngine extends AbstractUpdateEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeEngine.class);
    private final LitebridgeContext litebridgeContext;

    public MergeEngine(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }

    public <DTO> UpdateResult mergeInto(final Class<DTO> dtoClass, final Function<DtoMergeUsingStep<DTO>, MergeTerminal> merge) {
        final Table table = litebridgeContext.tableRegistry().getTableOrThrow(dtoClass).getMetaData().toTable();
        final DtoMergeUsingStep<DTO> mergeUsingStep = new DtoMergeUsingStep<>(dtoClass, litebridgeContext);
        final MergeTerminal mergeTerminal = merge.apply(mergeUsingStep);
        return execute(mergeTerminal, table);
    }

    public UpdateResult mergeInto(final String tableName, final Function<SqlMergeUsingStep, MergeTerminal> merge) {
        final Table table = litebridgeContext.tableRegistry().getOrCreateSpiTable(tableName);
        final SqlMergeUsingStep mergeUsingStep = new SqlMergeUsingStep(new Table(tableName), litebridgeContext);
        final MergeTerminal mergeTerminal = merge.apply(mergeUsingStep);
        return execute(mergeTerminal, table);
    }

    private UpdateResult execute(final MergeTerminal mergeTerminal, final Table table) {
        final QueryNode node = MergeTerminalInspector.getNode(mergeTerminal);
        return execute(table, node, litebridgeContext);
    }

    private UpdateResult execute(final Table table, final QueryNode node, final LitebridgeContext litebridgeContext) {
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> bindValues = QueryBindValueExtractor.extractBindValues(node);
            return execute(cachedOperation.preparedSql(bindValues), litebridgeContext);
        } else {
            return compileAndExecute(nodeHash, table, node, litebridgeContext);
        }
    }

    private UpdateResult compileAndExecute(final int astCacheKey, final Table table, final QueryNode node, final LitebridgeContext litebridgeContext) {
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            return execute(cachedOperation.preparedSql(QueryBindValueExtractor.extractBindValues(node)), litebridgeContext);
        } else {
            // Compile/prepare SQL query
            final TableMetaData tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
            final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(node);
            final Merge merge = (Merge) preparedOperation.operation();
            // Generate SQL and create type conversion metadata
            final String sql = litebridgeContext.databaseProvider().toSql(merge, litebridgeContext.transactionManager());
            final UpdateMetaData updateMetaData = createUpdateMetaData(tableMetaData);
            // Cache compiled SQL for this AST
            final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                    .map(BindValue::sqlDataType)
                    .toList();
            litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, updateMetaData, null));
            // Execute SQL query
            final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, updateMetaData);
            return execute(executionSql, litebridgeContext);
        }
    }

    private UpdateResult execute(final PreparedSql preparedSql, final LitebridgeContext litebridgeContext) {
        final UpdateResult updateResult;

        try {
            updateResult = litebridgeContext.databaseProvider().merge(preparedSql, litebridgeContext.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute insert", ex);
        }

        LOGGER.debug("Merge result: {}", updateResult);
        return updateResult;
    }

}
