package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;

final class DeleteQueryCompiler extends AbstractQueryCompiler<DeleteCompilationContext> {

    DeleteQueryCompiler(final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
    }

    @Override
    DeleteCompilationContext createCompilationContext(final QueryNode rootNode) {
        if (!(rootNode instanceof DeleteNode deleteNode)) {
            throw new IllegalArgumentException("Expected DeleteNode, but got " + rootNode);
        }

        return new DeleteCompilationContext(deleteNode, litebridgeContext);
    }

    @Override
    protected void applyNode(final QueryNode node, final DeleteCompilationContext compilationContext) {
        switch (node) {
            case WhereNode whereNode -> flattenAndApplyNodes(whereNode.condition(), compilationContext);
            case DeleteNode deleteNode -> { /* Ignore */ }
            case ConditionNode conditionNode -> compilationContext.addWhereCondition(conditionNode);
            case ConditionGroupNode conditionGroupNode -> {
                final ConditionGroupSpecStack conditionGroupSpecStack = compilationContext.ensureWhereConditionGroupStack();
                conditionGroupSpecStack.push(conditionGroupNode.logicOperator());
                flattenAndApplyNodes(conditionGroupNode.lastChild(), compilationContext);
                conditionGroupSpecStack.pop();
            }
            default -> throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
        }
    }
}
