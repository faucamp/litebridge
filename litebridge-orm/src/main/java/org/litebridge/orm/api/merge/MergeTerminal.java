package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.select.ast.QueryNode;

public class MergeTerminal {

    private final QueryNode node;

    public MergeTerminal(final QueryNode node) {
        this.node = node;
    }

    QueryNode node() {
        return node;
    }
}
