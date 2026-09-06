package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.engine.ast.WhereNode;

final class UpdateQueryCompiler extends AbstractQueryCompiler<UpdateCompilationContext> {

    UpdateQueryCompiler(final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
    }

    @Override
    UpdateCompilationContext createCompilationContext(final QueryNode rootNode) {
        if (!(rootNode instanceof UpdateNode updateNode)) {
            throw new IllegalArgumentException("Expected UpdateNode, but got " + rootNode);
        }

        return new UpdateCompilationContext(updateNode, litebridgeContext);
    }

    @Override
    protected void applyNode(final QueryNode node, final UpdateCompilationContext compilationContext) {
        switch (node) {
            case SetNode setNode -> compilationContext.addSetNode(setNode);
            case WhereNode whereNode -> flattenAndApplyNodes(whereNode.condition(), compilationContext);
            case UpdateNode updateNode -> { /* Ignore */ }
            case ConditionNode conditionNode -> compilationContext.addCondition(conditionNode);
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
