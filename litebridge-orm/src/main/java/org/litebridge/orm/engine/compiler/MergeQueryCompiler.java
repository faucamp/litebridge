package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.engine.ast.WhenMatchedNode;
import org.litebridge.orm.engine.ast.WhenNotMatchedNode;

final class MergeQueryCompiler extends AbstractQueryCompiler<MergeCompilationContext> {

    MergeQueryCompiler(final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
    }

    @Override
    MergeCompilationContext createCompilationContext(final QueryNode rootNode) {
        if (!(rootNode instanceof MergeNode mergeNode)) {
            throw new IllegalArgumentException("Expected MergeNode, but got: " + rootNode);
        }

        return new MergeCompilationContext(mergeNode, litebridgeContext);
    }

    @Override
    protected void applyNode(final QueryNode node, final MergeCompilationContext compilationContext) {
        switch (node) {
            case UsingNode usingNode -> {
                compilationContext.setUsingNode(usingNode);
                flattenAndApplyConditionNode(usingNode.on(), compilationContext, ConditionClauseType.ON);
            }
            case WhenMatchedNode whenMatchedNode -> {
                final MergeCompilationContext.WhenMatchedSpec whenMatchedSpec = compilationContext.addWhenMatchedSpec(true);

                // "AND" condition
                if (whenMatchedNode.and() != null) {
                    flattenAndApplyConditionNode(whenMatchedNode.and(), compilationContext, ConditionClauseType.WHEN_MATCHED);
                }

                if (whenMatchedNode.update() != null) {
                    flattenAndApplyNodes(whenMatchedNode.update(), compilationContext);
                } else if (whenMatchedNode.delete()) {
                    whenMatchedSpec.setDelete(true);
                }
            }
            case WhenNotMatchedNode whenNotMatchedNode -> {
                final MergeCompilationContext.WhenMatchedSpec whenMatchedSpec = compilationContext.addWhenMatchedSpec(false);

                // "AND" condition
                if (whenNotMatchedNode.and() != null) {
                    flattenAndApplyConditionNode(whenNotMatchedNode.and(), compilationContext, ConditionClauseType.WHEN_NOT_MATCHED);
                }

                flattenAndApplyNodes(whenNotMatchedNode.insert(), compilationContext);
            }
            case InsertNode insertNode -> compilationContext.whenNotMatchedInsert(insertNode);
            case InsertValuesNode insertValuesNode -> compilationContext.addInsertValues(insertValuesNode);
            case MergeNode mergeNode -> { /* Ignore */ }
            case UpdateNode updateNode -> { /* Ignore */ }
            case SetNode setNode -> compilationContext.whenMatchedUpdateSet(setNode);
            default -> throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
        }
    }

    private void flattenAndApplyConditionNode(final QueryNode node, final MergeCompilationContext compilationContext, ConditionClauseType conditionClauseType) {
        flattenAndApplyNodes(node, conditionNode -> applyConditionNode(conditionNode, compilationContext, conditionClauseType));
    }

    private void applyConditionNode(final QueryNode node,
                                    final MergeCompilationContext compilationContext,
                                    final ConditionClauseType conditionClauseType) {
        switch (node) {
            case ConditionNode conditionNode -> {
                switch (conditionClauseType) {
                    case ON -> compilationContext.addOnCondition(conditionNode);
                    case WHEN_MATCHED, WHEN_NOT_MATCHED -> compilationContext.addMatchAndCondition(conditionNode);
                }
            }
            case ConditionGroupNode conditionGroupNode -> {
                final ConditionGroupSpecStack conditionGroupSpecStack = switch (conditionClauseType) {
                    case ON -> compilationContext.onConditionGroupStack();
                    case WHEN_MATCHED, WHEN_NOT_MATCHED -> compilationContext.matchAndConditionGroupStack();
                };

                conditionGroupSpecStack.push(conditionGroupNode.logicOperator());
                flattenAndApplyConditionNode(conditionGroupNode.lastChild(), compilationContext, conditionClauseType);
                conditionGroupSpecStack.pop();
            }
            default -> throw new IllegalArgumentException("Unsupported condition node type: " + node);
        }
    }

    private enum ConditionClauseType {
        ON,
        WHEN_MATCHED,
        WHEN_NOT_MATCHED
    }
}
