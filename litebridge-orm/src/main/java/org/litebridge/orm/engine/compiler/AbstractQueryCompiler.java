package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.List;
import java.util.function.Consumer;

abstract sealed class AbstractQueryCompiler<CC extends CompilationContext>
        extends AbstractRootQueryCompiler
        permits ConditionBasedQueryCompiler, DeleteQueryCompiler, InsertQueryCompiler, UpdateQueryCompiler {

    protected AbstractQueryCompiler(final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
    }

    @SuppressWarnings("unchecked")
    final void applyNodes(final List<QueryNode> nodes, final CompilationContext compilationContext) {
        final CC castedCompilationContext = (CC) compilationContext;

        for (final QueryNode node : nodes) {
            applyNode(node, castedCompilationContext);
        }
    }

    abstract CC createCompilationContext(QueryNode rootNode);

    protected abstract void applyNode(final QueryNode node, final CC compilationContext);

    protected final void flattenAndApplyNodes(final QueryNode terminalNode, final CC compilationContext) {
        final List<QueryNode> nodes = flatten(terminalNode);
        applyNodes(nodes, compilationContext);
    }

    protected final void flattenAndApplyNodes(final QueryNode terminalNode, final CC compilationContext, final Consumer<QueryNode> nodeHandler) {
        final List<QueryNode> nodes = flatten(terminalNode);

        for (final QueryNode node : nodes) {
            nodeHandler.accept(node);
        }
    }
}
