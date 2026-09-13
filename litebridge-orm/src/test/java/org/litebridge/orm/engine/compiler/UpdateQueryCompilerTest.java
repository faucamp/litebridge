package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateQueryCompilerTest {

    @Test
    void createCompilationContextThrowsWhenNotUpdateNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final UpdateQueryCompiler compiler = new UpdateQueryCompiler(context);
        final DeleteNode deleteNode = new DeleteNode(null, "items", null);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compiler.createCompilationContext(deleteNode));
        assertEquals("Expected UpdateNode, but got " + deleteNode, ex.getMessage());
    }

    @Test
    void createCompilationContextSucceedsForUpdateNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        final Table table = new Table("items");
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of());

        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(metadataCache);
        when(tableRegistry.getOrCreateSpiTable("items")).thenReturn(table);
        when(metadataCache.ensureTableMetaData(table)).thenReturn(metaData);

        final UpdateQueryCompiler compiler = new UpdateQueryCompiler(context);
        final UpdateNode updateNode = new UpdateNode(null, "items", null);

        // When
        final UpdateCompilationContext compilationContext = compiler.createCompilationContext(updateNode);

        // Then
        assertNotNull(compilationContext);
    }

    @Test
    void applyNodeUpdateNodeIsIgnored() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final UpdateQueryCompiler compiler = new UpdateQueryCompiler(context);
        final UpdateCompilationContext compilationContext = mock(UpdateCompilationContext.class);
        final UpdateNode updateNode = new UpdateNode(null, "items", null);

        // When
        compiler.applyNode(updateNode, compilationContext);

        // Then: no exception, nothing called on compilationContext
    }

    @Test
    void applyNodeSetNodeAddsSetNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final UpdateQueryCompiler compiler = new UpdateQueryCompiler(context);
        final UpdateCompilationContext compilationContext = mock(UpdateCompilationContext.class);
        final SetNode setNode = new SetNode(null, "name", "Alice");

        // When
        compiler.applyNode(setNode, compilationContext);

        // Then
        verify(compilationContext).addSetNode(setNode);
    }

    @Test
    void applyNodeConditionNodeAddsCondition() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final UpdateQueryCompiler compiler = new UpdateQueryCompiler(context);
        final UpdateCompilationContext compilationContext = mock(UpdateCompilationContext.class);
        final ConditionNode conditionNode = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);

        // When
        compiler.applyNode(conditionNode, compilationContext);

        // Then
        verify(compilationContext).addCondition(conditionNode);
    }

    @Test
    void applyNodeWhereNodeFlattensAndAppliesCondition() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final UpdateQueryCompiler compiler = new UpdateQueryCompiler(context);
        final UpdateCompilationContext compilationContext = mock(UpdateCompilationContext.class);

        final ConditionNode conditionNode = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);
        final WhereNode whereNode = new WhereNode(null, conditionNode);

        // When
        compiler.applyNode(whereNode, compilationContext);

        // Then
        verify(compilationContext).addCondition(conditionNode);
    }

    @Test
    void applyNodeConditionGroupNodePushesAndPopsStack() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final UpdateQueryCompiler compiler = new UpdateQueryCompiler(context);
        final UpdateCompilationContext compilationContext = mock(UpdateCompilationContext.class);
        final ConditionGroupSpecStack stack = mock(ConditionGroupSpecStack.class);
        when(compilationContext.ensureWhereConditionGroupStack()).thenReturn(stack);

        final ConditionNode childCondition = new ConditionNode(null, LogicOperator.AND, "age", null, Operator.GT, 18);
        final ConditionGroupNode groupNode = new ConditionGroupNode(null, LogicOperator.OR, childCondition);

        // When
        compiler.applyNode(groupNode, compilationContext);

        // Then
        verify(stack).push(LogicOperator.OR);
        verify(compilationContext).addCondition(childCondition);
        verify(stack).pop();
    }

    @Test
    void applyNodeUnsupportedNodeThrowsException() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final UpdateQueryCompiler compiler = new UpdateQueryCompiler(context);
        final UpdateCompilationContext compilationContext = mock(UpdateCompilationContext.class);
        final DeleteNode unsupportedNode = new DeleteNode(null, "items", null);

        // When & Then
        final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> compiler.applyNode(unsupportedNode, compilationContext));
        assertInstanceOf(UnsupportedOperationException.class, ex);
    }
}
