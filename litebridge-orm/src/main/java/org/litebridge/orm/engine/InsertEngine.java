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

/**
 * Engine for processing {@code INSERT} statements.
 */
public final class InsertEngine extends AbstractInsertEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertEngine.class);
    private final TableRegistry tableRegistry;

    /**
     * Creates a new {@code InsertEngine} instance.
     *
     * @param tableRegistry Litebridge table registry
     */
    public InsertEngine(final TableRegistry tableRegistry) {
        this.tableRegistry = tableRegistry;
    }

    /**
     * Executes a SQL {@code INSERT} statement targeting a mapped DTO/entity class.
     *
     * @param dtoClass          the mapped DTO/entity type to insert into
     * @param insert            insert logic; a function that takes a {@link DtoInsertIntoStep} and returns a {@link InsertValuesStep}
     * @param litebridgeContext Litebridge context
     * @return the result of the insert operation
     */
    public InsertResult insert(final Class<?> dtoClass,
                               final Function<DtoInsertIntoStep, InsertValuesStep> insert,
                               final LitebridgeContext litebridgeContext) {
        final DtoInsertIntoStep insertIntoStep = new DtoInsertIntoStep(dtoClass, litebridgeContext);
        final InsertValuesStep insertValuesStep = insert.apply(insertIntoStep);
        final QueryNode node = InsertValuesStepInspector.getNode(insertValuesStep);
        return execute(node, litebridgeContext, () -> tableRegistry.getOrmTableOrThrow(dtoClass).getMetaData().toTable());
    }

    /**
     * Executes a SQL {@code INSERT} statement targeting a specific table.
     *
     * @param tableName         the name of the table to insert into
     * @param insert            insert logic; a function that takes a {@link SqlInsertIntoStep} and returns a {@link InsertValuesStep}
     * @param litebridgeContext Litebridge context
     * @return the result of the insert operation
     */
    public InsertResult insert(final String tableName,
                               final Function<SqlInsertIntoStep, InsertValuesStep> insert,
                               final LitebridgeContext litebridgeContext) {
        final SqlInsertIntoStep insertIntoStep = new SqlInsertIntoStep(tableName, litebridgeContext);
        final InsertValuesStep insertValuesStep = insert.apply(insertIntoStep);
        final QueryNode node = InsertValuesStepInspector.getNode(insertValuesStep);
        return execute(node, litebridgeContext, () -> tableRegistry.getOrCreateSpiTable(tableName));
    }

    private InsertResult execute(final QueryNode node, final LitebridgeContext litebridgeContext, final Supplier<Table> tableSupplier) {
        return execute(node,
                preparedOperation -> createUpdateMetaData(preparedOperation, tableSupplier, litebridgeContext),
                InsertResult.class,
                litebridgeContext);
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
