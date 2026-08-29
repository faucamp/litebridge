package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract sealed class AbstractRootQueryCompiler
        permits AbstractQueryCompiler, QueryCompiler {

    protected final LitebridgeContext litebridgeContext;

    public AbstractRootQueryCompiler(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }

    protected static List<QueryNode> flatten(final QueryNode node) {
        final List<QueryNode> nodes = new ArrayList<>();
        QueryNode current = node;

        while (current != null) {
            nodes.add(current);
            current = current.previous();
        }

        Collections.reverse(nodes);
        return nodes;
    }
}
