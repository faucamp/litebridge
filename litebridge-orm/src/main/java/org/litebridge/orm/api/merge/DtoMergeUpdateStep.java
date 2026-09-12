package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.delete.DtoDeleteStart;
import org.litebridge.orm.api.update.DtoUpdateStart;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.engine.DeleteEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.UpdateEngine;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.QueryNode;

import java.util.function.Function;

public final class DtoMergeUpdateStep<DTO> extends MergeUpdateStep {

    private final Class<DTO> dtoClass;

    public DtoMergeUpdateStep(final Class<DTO> dtoClass, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
        this.dtoClass = dtoClass;
    }

    public MergeTerminal update(final Function<DtoUpdateStart<DTO>, UpdateQuery> update) {
        final QueryNode terminalNode = UpdateEngine.createUpdateNodeChain(dtoClass, update, litebridgeContext);
        return new MergeTerminal(terminalNode);
    }

    public MergeTerminal delete(final Function<DtoDeleteStart<DTO>, DeleteTerminal> delete) {
        final QueryNode terminalNode = DeleteEngine.createDeleteNodeChain(dtoClass, delete, litebridgeContext);
        return new MergeTerminal(terminalNode);
    }

    public MergeTerminal delete() {
        final DeleteNode deleteNode = new DeleteNode(null, null, dtoClass);
        return new MergeTerminal(deleteNode);
    }
}
