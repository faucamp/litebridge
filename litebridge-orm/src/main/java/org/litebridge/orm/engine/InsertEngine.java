package org.litebridge.orm.engine;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.orm.api.insert.DtoInsertIntoStep;
import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.api.insert.InsertValuesStepInspector;
import org.litebridge.orm.api.insert.SqlInsertIntoStep;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.persistence.TableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return execute(node, () -> createUpdateMetaData(tableSupplier, litebridgeContext), InsertResult.class, litebridgeContext);
    }

    @Override
    protected String operationTypeName() {
        return "INSERT";
    }

    @Override
    protected Logger logger() {
        return LOGGER;
    }
}
