package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.update.DtoUpdateStart;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateQueryInspector;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.function.Function;

public final class DtoMergeUpdateStep<DTO> extends MergeUpdateStep<DTO> {

    private final Class<DTO> dtoClass;

    public DtoMergeUpdateStep(final Class<DTO> dtoClass, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
        this.dtoClass = dtoClass;
    }

    public MergeTerminal update(final Function<DtoUpdateStart<DTO>, UpdateQuery> update) {
        final DtoUpdateStart<DTO> dtoDtoUpdateStart = new DtoUpdateStart<>(dtoClass, litebridgeContext);
        final UpdateQuery terminal = update.apply(dtoDtoUpdateStart);
        final QueryNode terminalNode = UpdateQueryInspector.getNode(terminal);
        return new MergeTerminal(terminalNode);
    }

    public MergeTerminal delete() {
        final DeleteNode deleteNode = new DeleteNode(node, null, dtoClass);
        return new MergeTerminal(deleteNode);
    }
}
