package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Utility inspector for extracting the AST {@link QueryNode} from a {@link MergeTerminal}.
 */
public final class MergeTerminalInspector {

    private MergeTerminalInspector() {
    }

    /**
     * Extracts the AST {@link QueryNode} from the specified {@link MergeTerminal}.
     *
     * @param mergeTerminal the merge terminal step
     * @return the underlying query node
     */
    public static QueryNode getNode(final MergeTerminal mergeTerminal) {
        return mergeTerminal.node();
    }
}
