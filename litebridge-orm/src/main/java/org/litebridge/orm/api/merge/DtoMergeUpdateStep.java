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

/**
 * DTO-mode step for specifying the action to take in a {@code WHEN MATCHED} clause.
 *
 * @param <DTO> the mapped DTO/entity type
 */
public final class DtoMergeUpdateStep<DTO> extends MergeUpdateStep {

    private final Class<DTO> dtoClass;

    /**
     * Creates a new {@code DtoMergeUpdateStep} instance.
     *
     * @param dtoClass          the mapped DTO/entity class
     * @param node              the current query node
     * @param litebridgeContext the Litebridge context
     */
    public DtoMergeUpdateStep(final Class<DTO> dtoClass, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
        this.dtoClass = dtoClass;
    }

    /**
     * Specifies the update action to take when rows match.
     *
     * @param update function specifying the update operation
     * @return the {@code WHEN MATCHED} clause terminal
     */
    public MergeTerminal update(final Function<DtoUpdateStart<DTO>, UpdateQuery> update) {
        final QueryNode terminalNode = UpdateEngine.createUpdateNodeChain(dtoClass, update, litebridgeContext);
        return new MergeTerminal(terminalNode);
    }

    /**
     * Specifies the delete action to take when rows match.
     *
     * @param delete function specifying the delete operation
     * @return the {@code WHEN MATCHED} clause terminal
     */
    public MergeTerminal delete(final Function<DtoDeleteStart<DTO>, DeleteTerminal> delete) {
        final QueryNode terminalNode = DeleteEngine.createDeleteNodeChain(dtoClass, delete, litebridgeContext);
        return new MergeTerminal(terminalNode);
    }

    /**
     * Specifies that the matched row should be deleted.
     *
     * @return the {@code WHEN MATCHED} clause terminal
     */
    @Override
    public MergeTerminal delete() {
        final DeleteNode deleteNode = new DeleteNode(null, null, dtoClass);
        return new MergeTerminal(deleteNode);
    }
}
