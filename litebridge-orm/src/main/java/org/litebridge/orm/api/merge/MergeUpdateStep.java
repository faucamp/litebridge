package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Abstract base for steps specifying the action to take in a {@code WHEN MATCHED} clause.
 */
public abstract sealed class MergeUpdateStep permits DtoMergeUpdateStep, SqlMergeUpdateStep {

    /**
     * The current query node in the AST chain.
     */
    protected final QueryNode node;

    /**
     * The Litebridge context.
     */
    protected final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code MergeUpdateStep} instance.
     *
     * @param node              current query node
     * @param litebridgeContext Litebridge context
     */
    protected MergeUpdateStep(final QueryNode node, final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Specifies that the matched row should be deleted.
     *
     * @return the {@code WHEN MATCHED} clause terminal
     */
    public abstract MergeTerminal delete();
}
