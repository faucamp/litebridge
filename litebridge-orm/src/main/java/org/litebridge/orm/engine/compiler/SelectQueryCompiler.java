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
            case WhereNode whereNode ->
                    flattenAndApplyConditionNode(whereNode.condition(), compilationContext, ConditionClauseType.WHERE);
            case GroupByNode groupByNode -> compilationContext.addGroupBy(groupByNode);
            case HavingNode havingNode ->
                    flattenAndApplyConditionNode(havingNode.condition(), compilationContext, ConditionClauseType.HAVING);
            case OrderByNode orderByNode -> compilationContext.addOrderBy(orderByNode);
            case LimitNode limitNode -> compilationContext.setLimit(limitNode);
            case SelectNode selectNode -> { /* Ignore */ }
            default -> throw new IllegalArgumentException("Unsupported node type: " + node);
        }
    }

    private void applyJoinNode(final JoinNode joinNode, final SelectCompilationContext compilationContext) {
        compilationContext.addJoin(joinNode);
        flattenAndApplyConditionNode(joinNode.condition(), compilationContext, ConditionClauseType.JOIN);
    }

    private void flattenAndApplyConditionNode(final QueryNode node, final SelectCompilationContext compilationContext, ConditionClauseType conditionClauseType) {
        flattenAndApplyNodes(node, conditionNode -> applyConditionNode(conditionNode, compilationContext, conditionClauseType));
    }

    private void applyConditionNode(final QueryNode node,
                                    final SelectCompilationContext compilationContext,
                                    final ConditionClauseType conditionClauseType) {
        switch (node) {
            case ConditionNode conditionNode -> {
                switch (conditionClauseType) {
                    case JOIN -> compilationContext.addJoinCondition(conditionNode);
                    case WHERE -> compilationContext.addWhereCondition(conditionNode);
                    case HAVING -> compilationContext.addHavingCondition(conditionNode);
                }
            }
            case ConditionWithIdNode conditionWithIdNode -> {
                final ConditionNode conditionNode = compilationContext.toConditionNode(conditionWithIdNode);
                flattenAndApplyConditionNode(conditionNode, compilationContext, conditionClauseType);
            }
            case ConditionJoinUsingNode conditionJoinUsingNode ->
                    compilationContext.addJoinCondition(conditionJoinUsingNode);
            case ConditionGroupNode conditionGroupNode -> {
                final ConditionGroupSpecStack conditionGroupSpecStack = switch (conditionClauseType) {
                    case JOIN -> compilationContext.joinConditionGroupStack();
                    case WHERE -> compilationContext.ensureWhereConditionGroupStack();
                    case HAVING -> compilationContext.ensureHavingConditionGroupStack();
                };

                conditionGroupSpecStack.push(conditionGroupNode.logicOperator());
                flattenAndApplyConditionNode(conditionGroupNode.lastChild(), compilationContext, conditionClauseType);
                conditionGroupSpecStack.pop();
            }
            default -> throw new IllegalArgumentException("Unsupported condition node type: " + node);
        }
    }

    private enum ConditionClauseType {
        JOIN,
        WHERE,
        HAVING
    }
}
