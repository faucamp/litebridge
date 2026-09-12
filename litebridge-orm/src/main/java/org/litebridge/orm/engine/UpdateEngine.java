package org.litebridge.orm.engine;

import org.litebridge.db.spi.update.UpdateOpResult;
import org.litebridge.orm.api.update.DtoUpdateStart;
import org.litebridge.orm.api.update.SqlUpdateStart;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateQueryInspector;
import org.litebridge.orm.engine.ast.QueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Engine for processing {@code UPDATE} statements.
 */
public final class UpdateEngine extends AbstractUpdateEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateEngine.class);

    /**
     * Executes a SQL {@code UPDATE} statement targeting a mapped DTO/entity class.
     *
     * @param dtoClass          the mapped DTO/entity type to update
     * @param update            update logic; a function that takes a {@link DtoUpdateStart} and returns a {@link UpdateQuery}
     * @param litebridgeContext Litebridge context
     * @return the result of the update operation
     */
    public <DTO> UpdateOpResult update(final Class<DTO> dtoClass,
                                       final Function<DtoUpdateStart<DTO>, UpdateQuery> update,
                                       final LitebridgeContext litebridgeContext) {
        final QueryNode node = createUpdateNodeChain(dtoClass, update, litebridgeContext);
        return execute(node, litebridgeContext);
    }

    /**
     * Executes a SQL {@code UPDATE} statement targeting a specific table.
     *
     * @param tableName         the name of the table to update
     * @param update            update logic; a function that takes a {@link SqlUpdateStart} and returns a {@link UpdateQuery}
     * @param litebridgeContext Litebridge context
     * @return the result of the update operation
     */
    public UpdateOpResult update(final String tableName,
                                 final Function<SqlUpdateStart, UpdateQuery> update,
                                 final LitebridgeContext litebridgeContext) {
        final QueryNode node = createUpdateNodeChain(tableName, update, litebridgeContext);
        return execute(node, litebridgeContext);
    }

    /**
     * Creates a chain of update nodes for the specified mapped DTO/entity type and update logic.
     *
     * @param dtoClass          the mapped DTO/entity type to update
     * @param update            update logic; a function defining the update operation, which consumes a {@link SqlUpdateStart}
     *                          instance and returns an {@link UpdateQuery}
     * @param litebridgeContext Litebridge context
     * @return the terminal {@link QueryNode} of the constructed update query chain
     */
    public static <DTO> QueryNode createUpdateNodeChain(final Class<DTO> dtoClass, final Function<DtoUpdateStart<DTO>, UpdateQuery> update, final LitebridgeContext litebridgeContext) {
        final DtoUpdateStart<DTO> dtoDtoUpdateStart = new DtoUpdateStart<>(dtoClass, litebridgeContext);
        final UpdateQuery updateQuery = update.apply(dtoDtoUpdateStart);
        return UpdateQueryInspector.getNode(updateQuery);
    }

    /**
     * Creates a chain of update nodes for the specified table name and update logic.
     *
     * @param tableName         the name of the database table to update
     * @param update            update logic; a function defining the update operation, which consumes a {@link SqlUpdateStart}
     *                          instance and returns an {@link UpdateQuery}
     * @param litebridgeContext Litebridge context
     * @return the terminal {@link QueryNode} of the constructed update query chain
     */
    public static QueryNode createUpdateNodeChain(final String tableName, final Function<SqlUpdateStart, UpdateQuery> update, final LitebridgeContext litebridgeContext) {
        final SqlUpdateStart sqlUpdateStart = new SqlUpdateStart(tableName, litebridgeContext);
        final UpdateQuery updateQuery = update.apply(sqlUpdateStart);
        return UpdateQueryInspector.getNode(updateQuery);
    }

    @Override
    protected String operationTypeName() {
        return "UPDATE";
    }

    @Override
    protected Logger logger() {
        return LOGGER;
    }
}
