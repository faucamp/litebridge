package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Terminal step for a {@code MERGE} statement.
 */
public sealed class MergeTerminal permits InsertValuesStep, MergeWhenNotMatchedStep {

    /**
     * The current query node.
     */
    protected QueryNode node;

    /**
     * Creates a new {@code MergeTerminal} instance.
     *
     * @param node the current query node
     */
    public MergeTerminal(final QueryNode node) {
        this.node = node;
    }

    QueryNode node() {
        return node;
    }
}
