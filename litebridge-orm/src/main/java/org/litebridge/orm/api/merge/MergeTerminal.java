package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.ast.QueryNode;

public sealed class MergeTerminal permits MergeWhenNotMatchedStep {

    protected final QueryNode node;

    MergeTerminal(final QueryNode node) {
        this.node = node;
    }

    QueryNode node() {
        return node;
    }
}
