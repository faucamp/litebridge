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
import org.litebridge.orm.engine.ast.InsertDtoValuesNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.engine.ast.WhenMatchedNode;
import org.litebridge.orm.engine.ast.WhenNotMatchedNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
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

class MergeQueryCompilerTest {

    @Test
    void createCompilationContextThrowsWhenNotMergeNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final SelectNode selectNode = new SelectNode("items", null, null, null, new ExpressionSpec[0], null);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compiler.createCompilationContext(selectNode));
        assertEquals("Expected MergeNode, but got: " + selectNode, ex.getMessage());
    }

    @Test
    void createCompilationContextSucceedsForMergeNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        final Table table = new Table("items");
        final TableMetaData metaData = new TableMetaData(table, List.of(), List.of());

        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(metadataCache);
        when(context.aliasGenerator()).thenReturn(new org.litebridge.orm.persistence.alias.DefaultAliasGenerator(new org.litebridge.db.spi.alias.DefaultAliasTransformer()));
        when(tableRegistry.getOrmTable("items")).thenReturn(null);
        when(tableRegistry.getOrCreateSpiTable("items")).thenReturn(table);
        when(metadataCache.ensureTableMetaData(table)).thenReturn(metaData);

        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeNode mergeNode = new MergeNode("items", null);

        // When
        final MergeCompilationContext compilationContext = compiler.createCompilationContext(mergeNode);

        // Then
        assertNotNull(compilationContext);
    }

    @Test
    void applyNodeMergeNodeAndUpdateNodeAreIgnored() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeCompilationContext compilationContext = mock(MergeCompilationContext.class);

        // When
        compiler.applyNode(new MergeNode("items", null), compilationContext);
        compiler.applyNode(new UpdateNode(null, "items", null), compilationContext);

        // Then: no interaction on compilationContext
    }

    @Test
    void applyNodeUsingNodeAppliesOnCondition() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeCompilationContext compilationContext = mock(MergeCompilationContext.class);
        when(compilationContext.conditionContext()).thenReturn(MergeCompilationContext.ConditionContext.ON);

        final ConditionNode onCondition = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);
        final UsingNode usingNode = new UsingNode(null, "incoming", null, onCondition);

        // When
        compiler.applyNode(usingNode, compilationContext);

        // Then
        verify(compilationContext).setUsingNode(usingNode);
        verify(compilationContext).addOnCondition(onCondition);
    }

    @Test
    void applyNodeWhenMatchedNodeWithSetNodeAndDeleteNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeCompilationContext compilationContext = mock(MergeCompilationContext.class);
        final MergeCompilationContext.WhenMatchedSpec whenMatchedSpec = mock(MergeCompilationContext.WhenMatchedSpec.class);
        when(compilationContext.getWhenMatchedSpec()).thenReturn(whenMatchedSpec);

        final SetNode setNode = new SetNode(null, "name", "Alice");
        final WhenMatchedNode whenMatchedNode = new WhenMatchedNode(null, setNode);

        // When
        compiler.applyNode(whenMatchedNode, compilationContext);

        // Then
        verify(compilationContext).addWhenMatchedSpec(true);
        verify(compilationContext).whenMatchedUpdateSet(setNode);

        // When DeleteNode applied
        compiler.applyNode(new DeleteNode(null, "items", null), compilationContext);
        verify(whenMatchedSpec).setDelete(true);
    }

    @Test
    void applyNodeWhereNodeInWhenMatchedContext() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeCompilationContext compilationContext = mock(MergeCompilationContext.class);
        when(compilationContext.conditionContext()).thenReturn(MergeCompilationContext.ConditionContext.WHEN_MATCHED);

        final ConditionNode condition = new ConditionNode(null, LogicOperator.AND, "status", null, Operator.IS_NOT_NULL, null);
        final WhereNode whereNode = new WhereNode(null, condition);

        // When
        compiler.applyNode(whereNode, compilationContext);

        // Then
        verify(compilationContext).addMatchAndCondition(condition);
    }

    @Test
    void applyNodeWhenNotMatchedNodeWithAndConditionAndInsertNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeCompilationContext compilationContext = mock(MergeCompilationContext.class);
        when(compilationContext.conditionContext()).thenReturn(MergeCompilationContext.ConditionContext.WHEN_NOT_MATCHED);

        final ConditionNode andCond = new ConditionNode(null, LogicOperator.AND, "active", null, Operator.EQ, true);
        final InsertNode insertNode = new InsertNode("items", null, new String[]{"id"});
        final WhenNotMatchedNode whenNotMatchedNode = new WhenNotMatchedNode(null, andCond, insertNode);

        // When
        compiler.applyNode(whenNotMatchedNode, compilationContext);

        // Then
        verify(compilationContext).addWhenMatchedSpec(false);
        verify(compilationContext).addMatchAndCondition(andCond);
        verify(compilationContext).whenNotMatchedInsert(insertNode);
    }

    @Test
    void applyNodeInsertValuesAndDtoValues() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeCompilationContext compilationContext = mock(MergeCompilationContext.class);

        final InsertValuesNode valuesNode = new InsertValuesNode(null, new Object[]{1, "Bob"});
        final InsertDtoValuesNode dtoValuesNode = new InsertDtoValuesNode(null, new Object());

        // When
        compiler.applyNode(valuesNode, compilationContext);
        compiler.applyNode(dtoValuesNode, compilationContext);

        // Then
        verify(compilationContext).addInsertValues(valuesNode);
        verify(compilationContext).addInsertDtoValues(dtoValuesNode);
    }

    @Test
    void applyConditionGroupNodeInOnAndMatchedContexts() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeCompilationContext compilationContext = mock(MergeCompilationContext.class);

        final ConditionGroupSpecStack onStack = mock(ConditionGroupSpecStack.class);
        when(compilationContext.onConditionGroupStack()).thenReturn(onStack);

        final ConditionNode childCond = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);
        final ConditionGroupNode groupNode = new ConditionGroupNode(null, LogicOperator.OR, childCond);

        // When in ON context
        when(compilationContext.conditionContext()).thenReturn(MergeCompilationContext.ConditionContext.ON);
        final UsingNode usingNode = new UsingNode(null, "incoming", null, groupNode);
        compiler.applyNode(usingNode, compilationContext);

        // Then
        verify(onStack).push(LogicOperator.OR);
        verify(compilationContext).addOnCondition(childCond);
        verify(onStack).pop();

        // When in WHEN_NOT_MATCHED context
        final ConditionGroupSpecStack matchStack = mock(ConditionGroupSpecStack.class);
        when(compilationContext.matchAndConditionGroupStack()).thenReturn(matchStack);
        when(compilationContext.conditionContext()).thenReturn(MergeCompilationContext.ConditionContext.WHEN_NOT_MATCHED);

        final WhenNotMatchedNode whenNotMatchedNode = new WhenNotMatchedNode(null, groupNode, null);
        compiler.applyNode(whenNotMatchedNode, compilationContext);

        // Then
        verify(matchStack).push(LogicOperator.OR);
        verify(compilationContext).addMatchAndCondition(childCond);
        verify(matchStack).pop();
    }

    @Test
    void applyNodeUnsupportedNodeThrowsException() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeCompilationContext compilationContext = mock(MergeCompilationContext.class);
        final SelectNode selectNode = new SelectNode("items", null, null, null, new ExpressionSpec[0], null);

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> compiler.applyNode(selectNode, compilationContext));
    }

    @Test
    void applyConditionNodeUnsupportedConditionNodeThrowsException() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final MergeQueryCompiler compiler = new MergeQueryCompiler(context);
        final MergeCompilationContext compilationContext = mock(MergeCompilationContext.class);
        when(compilationContext.conditionContext()).thenReturn(MergeCompilationContext.ConditionContext.ON);

        final SelectNode nonConditionNode = new SelectNode("items", null, null, null, new ExpressionSpec[0], null);
        final UsingNode usingWithInvalidCond = new UsingNode(null, "incoming", null, nonConditionNode);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> compiler.applyNode(usingWithInvalidCond, compilationContext));
    }
}
