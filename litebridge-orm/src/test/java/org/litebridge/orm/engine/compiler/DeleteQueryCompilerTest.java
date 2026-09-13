package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteQueryCompilerTest {

    @Test
    void createCompilationContextThrowsWhenNotDeleteNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);
        final SelectNode selectNode = new SelectNode("items", null, null, null, new ExpressionSpec[0], null);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compiler.createCompilationContext(selectNode));
        assertEquals("Expected DeleteNode, but got " + selectNode, ex.getMessage());
    }

    @Test
    void createCompilationContextSucceedsForDeleteNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableRegistry registry = mock(TableRegistry.class);
        when(context.tableRegistry()).thenReturn(registry);
        when(registry.getOrCreateSpiTable("items")).thenReturn(new Table("items"));

        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);
        final DeleteNode deleteNode = new DeleteNode(null, "items", null);

        // When
        final DeleteCompilationContext compilationContext = compiler.createCompilationContext(deleteNode);

        // Then
        assertNotNull(compilationContext);
    }

    @Test
    void applyNodeDeleteNodeIsIgnored() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);
        final DeleteCompilationContext compilationContext = mock(DeleteCompilationContext.class);
        final DeleteNode deleteNode = new DeleteNode(null, "items", null);

        // When
        compiler.applyNode(deleteNode, compilationContext);

        // Then: no exception, nothing called on compilationContext
    }

    @Test
    void applyNodeConditionNodeAddsWhereCondition() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);
        final DeleteCompilationContext compilationContext = mock(DeleteCompilationContext.class);
        final ConditionNode conditionNode = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 10);

        // When
        compiler.applyNode(conditionNode, compilationContext);

        // Then
        verify(compilationContext).addWhereCondition(conditionNode);
    }

    @Test
    void applyNodeWhereNodeFlattensAndAppliesChildCondition() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);
        final DeleteCompilationContext compilationContext = mock(DeleteCompilationContext.class);

        final ConditionNode conditionNode = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 10);
        final WhereNode whereNode = new WhereNode(null, conditionNode);

        // When
        compiler.applyNode(whereNode, compilationContext);

        // Then
        verify(compilationContext).addWhereCondition(conditionNode);
    }

    @Test
    void applyNodeConditionGroupNodePushesAndPopsStack() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);
        final DeleteCompilationContext compilationContext = mock(DeleteCompilationContext.class);
        final ConditionGroupSpecStack stack = mock(ConditionGroupSpecStack.class);
        when(compilationContext.ensureWhereConditionGroupStack()).thenReturn(stack);

        final ConditionNode childCondition = new ConditionNode(null, LogicOperator.AND, "age", null, Operator.GT, 18);
        final ConditionGroupNode groupNode = new ConditionGroupNode(null, LogicOperator.OR, childCondition);

        // When
        compiler.applyNode(groupNode, compilationContext);

        // Then
        verify(stack).push(LogicOperator.OR);
        verify(compilationContext).addWhereCondition(childCondition);
        verify(stack).pop();
    }

    @Test
    void applyNodeUnsupportedNodeThrowsException() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final DeleteQueryCompiler compiler = new DeleteQueryCompiler(context);
        final DeleteCompilationContext compilationContext = mock(DeleteCompilationContext.class);
        final SelectNode unsupportedNode = new SelectNode("items", null, null, null, new ExpressionSpec[0], null);

        // When & Then
        final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> compiler.applyNode(unsupportedNode, compilationContext));
        assertInstanceOf(UnsupportedOperationException.class, ex);
    }
}
