package org.litebridge.orm.engine;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.delete.DeleteTerminalInspector;
import org.litebridge.orm.api.delete.DtoDeleteStart;
import org.litebridge.orm.api.delete.SqlDeleteStart;
import org.litebridge.orm.engine.ast.QueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Engine for processing {@code DELETE} statements.
 */
public final class DeleteEngine extends AbstractUpdateEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteEngine.class);

    /**
     * Executes a SQL {@code DELETE} statement targeting a mapped DTO/entity class.
     *
     * @param dtoClass          the mapped DTO/entity type to delete
     * @param delete            delete logic; a function that takes a {@link DtoDeleteStart} and returns a {@link DeleteTerminal}
     * @param litebridgeContext Litebridge context
     * @return the result of the delete operation
     */
    public <DTO> UpdateResult delete(final Class<DTO> dtoClass,
                                     final Function<DtoDeleteStart<DTO>, DeleteTerminal> delete,
                                     final LitebridgeContext litebridgeContext) {
        final QueryNode node = createDeleteNodeChain(dtoClass, delete, litebridgeContext);
        return execute(node, litebridgeContext);
    }

    /**
     * Executes a SQL {@code DELETE} statement targeting a specific table.
     *
     * @param tableName         the name of the table to delete
     * @param delete            delete logic; a function that takes a {@link SqlDeleteStart} and returns a {@link DeleteTerminal}
     * @param litebridgeContext Litebridge context
     * @return the result of the update operation
     */
    public UpdateResult delete(final String tableName,
                               final Function<SqlDeleteStart, DeleteTerminal> delete,
                               final LitebridgeContext litebridgeContext) {
        final QueryNode node = createDeleteNodeChain(tableName, delete, litebridgeContext);
        return execute(node, litebridgeContext);
    }

    /**
     * Creates a chain of update nodes for the specified mapped DTO/entity type and delete logic.
     *
     * @param dtoClass          the mapped DTO/entity type to update
     * @param delete            delete logic; a function defining the update operation, which consumes a {@link DtoDeleteStart}
     *                          instance and returns an {@link DeleteTerminal}
     * @param litebridgeContext Litebridge context
     * @return the terminal {@link QueryNode} of the constructed delete query chain
     */
    public static <DTO> QueryNode createDeleteNodeChain(final Class<DTO> dtoClass,
                                                        final Function<DtoDeleteStart<DTO>, DeleteTerminal> delete,
                                                        final LitebridgeContext litebridgeContext) {
        final DtoDeleteStart<DTO> dtoDeleteStart = new DtoDeleteStart<>(dtoClass, litebridgeContext);
        final DeleteTerminal deleteTerminal = delete.apply(dtoDeleteStart);
        return DeleteTerminalInspector.getNode(deleteTerminal);
    }

    /**
     * Creates a chain of update nodes for the specified table name and delete logic.
     *
     * @param tableName         the name of the database table to update
     * @param delete            delete logic; a function defining the update operation, which consumes a {@link SqlDeleteStart}
     *                          instance and returns an {@link DeleteTerminal}
     * @param litebridgeContext Litebridge context
     * @return the terminal {@link QueryNode} of the constructed update query chain
     */
    public static QueryNode createDeleteNodeChain(final String tableName, final Function<SqlDeleteStart, DeleteTerminal> delete, final LitebridgeContext litebridgeContext) {
        final SqlDeleteStart sqlDeleteStart = new SqlDeleteStart(tableName, litebridgeContext);
        final DeleteTerminal deleteTerminal = delete.apply(sqlDeleteStart);
        return DeleteTerminalInspector.getNode(deleteTerminal);
    }

    @Override
    protected String operationTypeName() {
        return "DELETE";
    }

    @Override
    protected Logger logger() {
        return LOGGER;
    }
}
