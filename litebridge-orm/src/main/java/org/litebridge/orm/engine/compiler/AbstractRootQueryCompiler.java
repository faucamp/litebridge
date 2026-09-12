package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base compiler class for flattening and processing query node ASTs.
 */
abstract sealed class AbstractRootQueryCompiler
        permits AbstractQueryCompiler, QueryCompiler {

    /**
     * The Litebridge context.
     */
    protected final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code AbstractRootQueryCompiler} instance.
     *
     * @param litebridgeContext the Litebridge context
     */
    protected AbstractRootQueryCompiler(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Flattens the linked query node chain into a sequential list in root-to-leaf order.
     *
     * @param node the terminal query node
     * @return the ordered list of query nodes
     */
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
