package org.litebridge.db.oracle.engine;

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
        final InsertAllStep insertAllStep = new InsertAllStep(litebridgeContextCreator);
        final InsertAllStep terminal = insert.apply(insertAllStep);
        final List<InsertValuesNode> insertValuesNodes = InsertAllStepInspector.insertValuesNodes(terminal);

        final LitebridgeContext litebridgeContext = litebridgeContextCreator.apply(LitebridgeContext.Mode.SQL);
        final QueryCompiler queryCompiler = litebridgeContext.createQueryCompiler();

        final List<PreparedOperation> preparedInserts = insertValuesNodes.stream()
                .map(queryCompiler::compile)
                .toList();

        final List<Insert> inserts = preparedInserts.stream()
                .map(preparedOperation -> (Insert) preparedOperation.operation())
                .toList();

        final String sql = oracleInsertSqlGenerator.createInsertAllClause(inserts);
        final List<BindValue> bindValues = preparedInserts.stream()
                .map(PreparedOperation::bindValues)
                .flatMap(List::stream)
                .toList();

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
