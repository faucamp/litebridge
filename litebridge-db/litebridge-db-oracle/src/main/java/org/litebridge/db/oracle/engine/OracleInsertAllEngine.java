package org.litebridge.db.oracle.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.oracle.api.insert.InsertAllStep;
import org.litebridge.db.oracle.api.insert.InsertAllStepInspector;
import org.litebridge.db.oracle.sql.OracleInsertSqlGenerator;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.orm.engine.AbstractInsertEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryBindValueExtractor;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * Oracle-specific engine for processing {@code INSERT ALL} statements.
 */
public final class OracleInsertAllEngine extends AbstractInsertEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleInsertAllEngine.class);
    private final OracleInsertSqlGenerator oracleInsertSqlGenerator;

    public OracleInsertAllEngine(final OracleInsertSqlGenerator oracleInsertSqlGenerator) {
        this.oracleInsertSqlGenerator = oracleInsertSqlGenerator;
    }

    public InsertResult insertAll(final Function<InsertAllStep, InsertAllStep> insert,
                                  final Function<LitebridgeContext.Mode, LitebridgeContext> litebridgeContextCreator) {
        final LitebridgeContext litebridgeContext = litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL);
        final InsertAllStep insertAllStep = new InsertAllStep(litebridgeContext);
        final InsertAllStep terminal = insert.apply(insertAllStep);
        final List<InsertValuesNode> insertValuesNodes = InsertAllStepInspector.insertValuesNodes(terminal);

        return execute(insertValuesNodes, litebridgeContext);
    }

    private InsertResult execute(final List<InsertValuesNode> insertValuesNodes,
                                 final LitebridgeContext litebridgeContext) {
        final int nodesHash = insertValuesNodes.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodesHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> bindValues = insertValuesNodes.stream()
                    .flatMap(node -> QueryBindValueExtractor.extractBindValues(node, litebridgeContext).stream())
                    .toList();
            return execute(cachedOperation.preparedSql(bindValues), InsertResult.class, litebridgeContext);
        } else {
            return compileAndExecute(nodesHash, insertValuesNodes, litebridgeContext);
        }
    }

    private InsertResult compileAndExecute(final int astCacheKey,
                                           final List<InsertValuesNode> insertValuesNodes,
                                           final LitebridgeContext litebridgeContext) {
        // Compile/prepare SQL query
        final QueryCompiler queryCompiler = litebridgeContext.createQueryCompiler();

        final List<PreparedOperation> preparedInserts = insertValuesNodes.stream()
                .map(queryCompiler::compile)
                .toList();

        final List<Insert> inserts = preparedInserts.stream()
                .map(preparedOperation -> (Insert) preparedOperation.operation())
                .toList();

        // Generate SQL and create type conversion metadata
        final String sql = oracleInsertSqlGenerator.createInsertAllClause(inserts);

        // Collect bind values and cache compiled SQL for this AST
        final List<BindValue> bindValues = preparedInserts.stream()
                .map(PreparedOperation::bindValues)
                .flatMap(List::stream)
                .toList();
        final List<Integer> bindValueSqlTypes = bindValues.stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, EMPTY_UPDATE_META_DATA));

        // Execute SQL statement
        final PreparedSql preparedSql = new PreparedSql(sql, bindValues, null, EMPTY_UPDATE_META_DATA);
        return execute(preparedSql, InsertResult.class, litebridgeContext);
    }

    @Override
    protected String operationTypeName() {
        return "INSERT ALL";
    }

    @Override
    protected Logger logger() {
        return LOGGER;
    }
}
