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

public final class DeleteEngine extends AbstractUpdateEngine {

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

    @Override
    protected String operationTypeName() {
        return "DELETE";
    }

    @Override
    protected Logger logger() {
        return LOGGER;
    }
}
