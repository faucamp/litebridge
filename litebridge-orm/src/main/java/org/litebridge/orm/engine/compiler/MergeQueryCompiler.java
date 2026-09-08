package org.litebridge.orm.engine.compiler;

import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.InsertDtoValuesNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.engine.ast.WhenMatchedNode;
import org.litebridge.orm.engine.ast.WhenNotMatchedNode;
import org.litebridge.orm.engine.ast.WhereNode;

/**
 * Specialised query node compiler for MERGE INTO statements.
 */
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
            case MergeNode mergeNode -> { /* Ignore */ }
            case UsingNode usingNode -> {
                compilationContext.setUsingNode(usingNode);
                flattenAndApplyConditionNode(usingNode.on(), compilationContext);
            }
            case WhenMatchedNode whenMatchedNode -> {
                compilationContext.addWhenMatchedSpec(true);
                flattenAndApplyNodes(whenMatchedNode.update(), compilationContext);
            }
            case UpdateNode updateNode -> { /* Ignore */ }
            case SetNode setNode -> compilationContext.whenMatchedUpdateSet(setNode);
            case DeleteNode deleteNode -> compilationContext.getWhenMatchedSpec().setDelete(true);
            case WhereNode whereNode -> flattenAndApplyConditionNode(whereNode.condition(), compilationContext);
            case WhenNotMatchedNode whenNotMatchedNode -> {
                compilationContext.addWhenMatchedSpec(false);
                flattenAndApplyNodes(whenNotMatchedNode.insert(), compilationContext);
            }
            case InsertNode insertNode -> compilationContext.whenNotMatchedInsert(insertNode);
            case InsertValuesNode insertValuesNode -> compilationContext.addInsertValues(insertValuesNode);
            case InsertDtoValuesNode insertDtoValuesNode -> compilationContext.addInsertDtoValues(insertDtoValuesNode);
            default -> throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
        }
    }

    private void flattenAndApplyConditionNode(final QueryNode node, final MergeCompilationContext compilationContext) {
        flattenAndApplyNodes(node, conditionNode -> applyConditionNode(conditionNode, compilationContext));
    }

    private void applyConditionNode(final QueryNode node,
                                    final MergeCompilationContext compilationContext) {
        final MergeCompilationContext.ConditionContext conditionContext = compilationContext.conditionContext();

        switch (node) {
            case ConditionNode conditionNode -> {
                switch (conditionContext) {
                    case ON -> compilationContext.addOnCondition(conditionNode);
                    case WHEN_MATCHED, WHEN_NOT_MATCHED -> compilationContext.addMatchAndCondition(conditionNode);
                }
            }
            case ConditionGroupNode conditionGroupNode -> {
                final ConditionGroupSpecStack conditionGroupSpecStack = switch (conditionContext) {
                    case ON -> compilationContext.onConditionGroupStack();
                    case WHEN_MATCHED, WHEN_NOT_MATCHED -> compilationContext.matchAndConditionGroupStack();
                };

                conditionGroupSpecStack.push(conditionGroupNode.logicOperator());
                flattenAndApplyConditionNode(conditionGroupNode.lastChild(), compilationContext);
                conditionGroupSpecStack.pop();
            }
            default -> throw new IllegalArgumentException("Unsupported condition node type: " + node);
        }
    }
}
