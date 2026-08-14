package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.InsertV2;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.insert.InsertIntoStep;
import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.api.insert.InsertValuesStepInspector;
import org.litebridge.orm.api.insert.model.InsertSpec;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.persistence.TableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class InsertEngine extends AbstractUpdateEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertEngine.class);
    private final TableRegistry tableRegistry;
    private final Supplier<LitebridgeContext> litebridgeContextSupplier;

    public InsertEngine(final TableRegistry tableRegistry, final Supplier<LitebridgeContext> litebridgeContextSupplier) {
        this.tableRegistry = tableRegistry;
        this.litebridgeContextSupplier = litebridgeContextSupplier;
    }

    public InsertResult insert(final String tableName, final Function<InsertIntoStep, InsertValuesStep> insert) {
        final Table table = tableRegistry.getOrCreateSpiTable(tableName);
        final LitebridgeContext litebridgeContext = litebridgeContextSupplier.get();
        final InsertIntoStep insertIntoStep = new InsertIntoStep(table, null, litebridgeContext);
        final InsertValuesStep insertValuesStep = insert.apply(insertIntoStep);
        final QueryNode node = InsertValuesStepInspector.getNode(insertValuesStep);
        return execute(table, node, litebridgeContext);
    }

    private InsertResult execute(final Table table, final QueryNode node, final LitebridgeContext litebridgeContext) {
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> bindValues = QueryBindValueExtractor.extractBindValues(node);
            return execute(cachedOperation.preparedSql(bindValues), litebridgeContext);
        } else {
            return compileAndExecute(nodeHash, table, node, litebridgeContext);
        }
    }

    private InsertResult compileAndExecute(final int astCacheKey, final Table table, final QueryNode node, final LitebridgeContext litebridgeContext) {
        // Compile/prepare SQL query
        final TableMetaData tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
        final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(node);
        final InsertV2 insert = (InsertV2) preparedOperation.operation();
        // Generate SQL and create type conversion metadata
        final String sql = litebridgeContext.databaseProvider().toSql(insert, litebridgeContext.transactionManager());
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

    private InsertResult execute(final PreparedSql preparedSql, final LitebridgeContext litebridgeContext) {
        final InsertResult insertResult;

        try {
            insertResult = litebridgeContext.databaseProvider().insert(preparedSql, litebridgeContext.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute insert", ex);
        }

        LOGGER.debug("Insert result: {}", insertResult);
        return insertResult;
    }
}
