package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.select.ast.QueryNode;

public final class MergeTerminalInspector {

    private MergeTerminalInspector() {
    }

    public static QueryNode getNode(final MergeTerminal mergeTerminal) {
        return mergeTerminal.node();
    }
}
