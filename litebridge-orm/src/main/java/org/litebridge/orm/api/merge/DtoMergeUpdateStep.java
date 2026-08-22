package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.dto.update.DtoUpdateStart;
import org.litebridge.orm.api.dto.update.DtoUpdater;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateQueryInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.persistence.OrmTable;

import java.util.function.Function;

public final class DtoMergeUpdateStep<DTO> extends MergeUpdateStep<DTO> {

    private final Class<DTO> dtoClass;

    public DtoMergeUpdateStep(final Class<DTO> dtoClass, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
        this.dtoClass = dtoClass;
    }

    public MergeTerminal update(final Function<DtoUpdateStart<DTO>, UpdateQuery> update) {
        final OrmTable ormTable = litebridgeContext.tableRegistry().getTableOrThrow(dtoClass);
        final DtoUpdater<DTO> dtoUpdater = new DtoUpdater<>(dtoClass, ormTable, litebridgeContext);
        final UpdateQuery terminal = update.apply(dtoUpdater);
        final QueryNode terminalNode = UpdateQueryInspector.getNode(terminal);
        return new MergeTerminal(terminalNode);
    }

    public MergeTerminal delete() {
        final DeleteNode deleteNode = new DeleteNode(node, null, dtoClass);
        return new MergeTerminal(deleteNode);
    }
}
