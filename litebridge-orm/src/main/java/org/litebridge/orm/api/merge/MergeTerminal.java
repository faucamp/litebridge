package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.engine.ast.QueryNode;

public sealed class MergeTerminal permits InsertValuesStep, MergeWhenNotMatchedStep {

    protected final QueryNode node;

    public MergeTerminal(final QueryNode node) {
        this.node = node;
    }

    QueryNode node() {
        return node;
    }
}
