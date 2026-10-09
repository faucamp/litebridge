package org.litebridge.orm.api.condition;

import org.litebridge.orm.engine.ast.QueryNode;

public final class CbConditionClauseTerminalInspector {

    private CbConditionClauseTerminalInspector() {
    }

    /**
     * Returns the current query node.
     *
     * @return the query node
     */
    public static QueryNode getNode(final AbstractCbConditionClauseTerminal<?, ?, ?, ?> terminal) {
        return terminal.node();
    }
}
