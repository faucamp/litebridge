package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;

/**
 * Specialised query node compiler for SELECT statements.
 */
final class SelectQueryCompiler extends AbstractQueryCompiler<SelectCompilationContext> {

    SelectQueryCompiler(final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
    }

    @Override
    SelectCompilationContext createCompilationContext(final QueryNode rootNode) {
        if (!(rootNode instanceof SelectNode selectNode)) {
            throw new IllegalArgumentException("Expected SelectNode, but got " + rootNode);
        }

        return new SelectCompilationContext(selectNode, litebridgeContext);
    }

    @Override
    protected void applyNode(final QueryNode node, final SelectCompilationContext compilationContext) {
        switch (node) {
            case JoinNode joinNode -> applyJoinNode(joinNode, compilationContext);
            case WhereNode whereNode -> applyWhereNode(whereNode, compilationContext);
            case GroupByNode groupByNode -> compilationContext.addGroupByNode(groupByNode);
            case HavingNode havingNode -> applyHavingNode(havingNode, compilationContext);
            case SelectNode selectNode -> { /* Ignore */ }
            case OrderByNode orderByNode -> compilationContext.addOrderByNode(orderByNode);
            case LimitNode limitNode -> compilationContext.setLimitNode(limitNode);
            default -> throw new IllegalArgumentException("Unsupported node type: " + node);
        }
    }

    private void applyJoinNode(final JoinNode joinNode, final SelectCompilationContext compilationContext) {
        final ConditionGroupSpecStack conditionGroupSpecStack = compilationContext.addJoin(joinNode);
        flattenAndApplyConditionNode(joinNode.condition(), conditionGroupSpecStack, compilationContext);
    }

    private void applyWhereNode(final WhereNode whereNode, final SelectCompilationContext compilationContext) {
        final ConditionGroupSpecStack conditionGroupSpecStack = compilationContext.setWhereNode(whereNode);
        flattenAndApplyConditionNode(whereNode.condition(), conditionGroupSpecStack, compilationContext);
    }

    private void applyHavingNode(final HavingNode havingNode, final SelectCompilationContext compilationContext) {
        final ConditionGroupSpecStack conditionGroupSpecStack = compilationContext.setHavingNode(havingNode);
        flattenAndApplyConditionNode(havingNode.condition(), conditionGroupSpecStack, compilationContext);
    }

    private void flattenAndApplyConditionNode(final QueryNode node,
                                              final ConditionGroupSpecStack conditionGroupSpecStack,
                                              final SelectCompilationContext compilationContext) {
        flattenAndApplyNodes(node, conditionNode -> applyConditionNode(conditionNode, conditionGroupSpecStack, compilationContext));
    }

    private void applyConditionNode(final QueryNode node,
                                    final ConditionGroupSpecStack conditionGroupSpecStack,
                                    final SelectCompilationContext compilationContext) {
        switch (node) {
            case ConditionNode conditionNode -> conditionGroupSpecStack.current()
                    .newCondition(conditionNode.logicOperator(),
                            conditionNode.lhsColumn(),
                            conditionNode.lhsExpression(),
                            conditionNode.operator(),
                            conditionNode.rhs());
            case ConditionWithIdNode conditionWithIdNode ->
                    compilationContext.setWhereConditionWithIdNode(conditionWithIdNode);
            case ConditionJoinUsingNode conditionJoinUsingNode ->
                    compilationContext.addJoinUsingCondition(conditionJoinUsingNode);
            case ConditionGroupNode conditionGroupNode -> {
                conditionGroupSpecStack.push(conditionGroupNode.logicOperator());
                flattenAndApplyConditionNode(conditionGroupNode.lastChild(), conditionGroupSpecStack, compilationContext);
                conditionGroupSpecStack.pop();
            }
            default -> throw new IllegalArgumentException("Unsupported condition node type: " + node);
        }
    }
}
