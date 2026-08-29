package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.api.select.ast.InsertValuesNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.Arrays;

final class InsertQueryCompiler extends AbstractQueryCompiler<InsertCompilationContext> {

    InsertQueryCompiler(final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
    }

    @Override
    InsertCompilationContext createCompilationContext(final QueryNode rootNode) {
        if (!(rootNode instanceof InsertNode insertNode)) {
            throw new IllegalArgumentException("Expected InsertNode, but got " + rootNode);
        }

        return new InsertCompilationContext(insertNode, litebridgeContext);
    }

    @Override
    protected void applyNode(final QueryNode node, final InsertCompilationContext compilationContext) {
        switch (node) {
            case InsertValuesNode insertValuesNode ->
                    compilationContext.addRowBindValues(Arrays.asList(insertValuesNode.values()));
            case InsertNode insertNode -> { /* Ignore */ }
            default -> throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
        }
    }
}
