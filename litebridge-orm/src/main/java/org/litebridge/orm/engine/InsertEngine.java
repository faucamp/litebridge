package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.orm.api.insert.DtoInsertIntoStep;
import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.api.insert.InsertValuesStepInspector;
import org.litebridge.orm.api.insert.SqlInsertIntoStep;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.persistence.TableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class InsertEngine extends AbstractInsertEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertEngine.class);
    private final TableRegistry tableRegistry;

    public InsertEngine(final TableRegistry tableRegistry) {
        this.tableRegistry = tableRegistry;
    }

    public InsertResult insert(final Class<?> dtoClass,
                               final Function<DtoInsertIntoStep, InsertValuesStep> insert,
                               final LitebridgeContext litebridgeContext) {
        final DtoInsertIntoStep insertIntoStep = new DtoInsertIntoStep(dtoClass, litebridgeContext);
        final InsertValuesStep insertValuesStep = insert.apply(insertIntoStep);
        final QueryNode node = InsertValuesStepInspector.getNode(insertValuesStep);
        return execute(node, litebridgeContext, () -> tableRegistry.getOrmTableOrThrow(dtoClass).getMetaData().toTable());
    }

    public InsertResult insert(final String tableName,
                               final Function<SqlInsertIntoStep, InsertValuesStep> insert,
                               final LitebridgeContext litebridgeContext) {
        final SqlInsertIntoStep insertIntoStep = new SqlInsertIntoStep(tableName, litebridgeContext);
        final InsertValuesStep insertValuesStep = insert.apply(insertIntoStep);
        final QueryNode node = InsertValuesStepInspector.getNode(insertValuesStep);
        return execute(node, litebridgeContext, () -> tableRegistry.getOrCreateSpiTable(tableName));
    }

    private InsertResult execute(final QueryNode node, final LitebridgeContext litebridgeContext, final Supplier<Table> tableSupplier) {
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> bindValues = QueryBindValueExtractor.extractBindValues(node);
            return execute(cachedOperation.preparedSql(bindValues), litebridgeContext);
        } else {
            return compileAndExecute(nodeHash, tableSupplier, node, litebridgeContext);
        }
    }

    private InsertResult compileAndExecute(final int astCacheKey, final Supplier<Table> tableSupplier, final QueryNode node, final LitebridgeContext litebridgeContext) {
        // Compile/prepare SQL query
        final Table table = tableSupplier.get();
        final TableMetaData tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
        final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(node);
        final Insert insert = (Insert) preparedOperation.operation();
        // Generate SQL and create type conversion metadata
        final String sql = litebridgeContext.databaseProvider().toSql(insert, litebridgeContext.transactionManager());
        final UpdateMetaData updateMetaData = createUpdateMetaData(tableMetaData);
        // Cache compiled SQL for this AST
        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, updateMetaData));
        // Execute SQL query
        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, updateMetaData);
        return execute(executionSql, litebridgeContext);
    }

    private InsertResult execute(final PreparedSql preparedSql, final LitebridgeContext litebridgeContext) {
        final InsertResult insertResult;

        try {
            insertResult = litebridgeContext.databaseProvider().insert(preparedSql, litebridgeContext.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute insert: " + preparedSql.sql(), ex);
        }

        LOGGER.debug("Insert result: {}", insertResult);
        return insertResult;
    }
}
