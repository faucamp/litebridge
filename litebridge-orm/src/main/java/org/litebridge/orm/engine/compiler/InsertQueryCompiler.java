package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.SelectTarget;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.QueryNode;

import java.util.Arrays;
import java.util.List;

/**
 * Specialised query node compiler for INSERT statements.
 */
final class InsertQueryCompiler extends AbstractQueryCompiler<InsertCompilationContext> {

    InsertQueryCompiler(final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
    }

    @Override
    InsertCompilationContext createCompilationContext(final QueryNode rootNode, final @Nullable List<SelectTarget> contextSelectTargets) {
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
