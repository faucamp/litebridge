package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.select.ast.QueryNode;

public class MergeTerminal {

    protected final QueryNode node;

    MergeTerminal(final QueryNode node) {
        this.node = node;
    }

    QueryNode node() {
        return node;
    }
}
