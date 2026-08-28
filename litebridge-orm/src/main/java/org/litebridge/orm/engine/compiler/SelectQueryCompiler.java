package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.ConditionJoinUsingNode;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.ConditionWithIdNode;
import org.litebridge.orm.api.select.ast.GroupByNode;
import org.litebridge.orm.api.select.ast.HavingNode;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.LimitNode;
import org.litebridge.orm.api.select.ast.OrderByNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;

public final class SelectQueryCompiler extends ConditionBasedQueryCompiler<SelectCompilationContext> {

    public SelectQueryCompiler(final LitebridgeContext litebridgeContext) {
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
            case WhereNode whereNode -> flattenAndApplyNodes(whereNode.condition(),
                    compilationContext,
                    conditionNode -> applyConditionNode(conditionNode, compilationContext, ConditionClauseType.WHERE));
            case GroupByNode groupByNode -> compilationContext.addGroupBy(groupByNode);
            case HavingNode havingNode -> flattenAndApplyNodes(havingNode.condition(),
                    compilationContext,
                    conditionNode -> applyConditionNode(conditionNode, compilationContext, ConditionClauseType.HAVING));
            case OrderByNode orderByNode -> compilationContext.addOrderBy(orderByNode);
            case LimitNode limitNode -> compilationContext.setLimit(limitNode);
            case SelectNode selectNode -> { /* Ignore */ }
            default -> throw new IllegalArgumentException("Unsupported node type: " + node);
        }
    }

    private void applyJoinNode(final JoinNode joinNode, final SelectCompilationContext compilationContext) {
        compilationContext.addJoin(joinNode);
        flattenAndApplyNodes(joinNode.condition(),
                compilationContext,
                conditionNode -> applyConditionNode(conditionNode, compilationContext, ConditionClauseType.JOIN));
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
                applyConditionNode(conditionNode, compilationContext, conditionClauseType);
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
                flattenAndApplyNodes(conditionGroupNode.lastChild(),
                        compilationContext,
                        conditionNode -> applyConditionNode(conditionNode, compilationContext, conditionClauseType));
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
