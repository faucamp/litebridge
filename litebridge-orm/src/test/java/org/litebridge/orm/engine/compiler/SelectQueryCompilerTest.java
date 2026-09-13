package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.DefaultAliasTransformer;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
import org.mockito.Mockito;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SelectQueryCompilerTest {

    private LitebridgeContext createMockContext() {
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final TableMetaDataCache metadataCache = mock(TableMetaDataCache.class);
        final SelectExpressionMapper expressionMapper = mock(SelectExpressionMapper.class);
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class, Mockito.RETURNS_DEEP_STUBS);

        when(context.tableRegistry()).thenReturn(tableRegistry);
        when(context.tableMetaDataCache()).thenReturn(metadataCache);
        when(context.selectExpressionMapper()).thenReturn(expressionMapper);
        when(context.sqlFunctionRegistry()).thenReturn(sqlFunctionRegistry);
        when(context.aliasGenerator()).thenReturn(new DefaultAliasGenerator(new DefaultAliasTransformer()));
        return context;
    }

    @Test
    void createCompilationContextThrowsWhenNotSelectNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final DeleteNode deleteNode = new DeleteNode(null, "items", null);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> compiler.createCompilationContext(deleteNode));
        assertEquals("Expected SelectNode, but got " + deleteNode, ex.getMessage());
    }

    @Test
    void createCompilationContextSucceedsForSelectNode() {
        // Given
        final LitebridgeContext context = createMockContext();
        final Table table = new Table("items");
        final ColumnMetaData idCol = new ColumnMetaData(table, "id", true, Types.INTEGER, 0);
        final TableMetaData metaData = new TableMetaData(table, List.of("id"), List.of(idCol));

        when(context.tableRegistry().getOrCreateSpiTable("items")).thenReturn(table);
        when(context.tableMetaDataCache().ensureTableMetaData(any())).thenReturn(metaData);

        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final SelectNode selectNode = new SelectNode("items", null, null, null, new ExpressionSpec[0], null);

        // When
        final SelectCompilationContext compilationContext = compiler.createCompilationContext(selectNode);

        // Then
        assertNotNull(compilationContext);
    }

    @Test
    void applyNodeSelectNodeIsIgnored() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final SelectCompilationContext compilationContext = mock(SelectCompilationContext.class);

        // When
        compiler.applyNode(new SelectNode("items", null, null, null, new ExpressionSpec[0], null), compilationContext);

        // Then: no exception or interactions
    }

    @Test
    void applyNodeGroupByOrderByLimit() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final SelectCompilationContext compilationContext = mock(SelectCompilationContext.class);

        final GroupByNode groupBy = new GroupByNode(null, new String[]{"category"}, null);
        final OrderByNode orderBy = new OrderByNode(null, "id", null, true);
        final LimitNode limit = new LimitNode(null, 10, 5);

        // When
        compiler.applyNode(groupBy, compilationContext);
        compiler.applyNode(orderBy, compilationContext);
        compiler.applyNode(limit, compilationContext);

        // Then
        verify(compilationContext).addGroupBy(groupBy);
        verify(compilationContext).addOrderBy(orderBy);
        verify(compilationContext).setLimit(limit);
    }

    @Test
    void applyNodeWhereAndHavingConditions() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final SelectCompilationContext compilationContext = mock(SelectCompilationContext.class);

        final ConditionNode whereCond = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 1);
        final WhereNode whereNode = new WhereNode(null, whereCond);

        final ConditionNode havingCond = new ConditionNode(null, LogicOperator.AND, "cnt", null, Operator.GT, 5);
        final HavingNode havingNode = new HavingNode(null, havingCond);

        // When
        compiler.applyNode(whereNode, compilationContext);
        compiler.applyNode(havingNode, compilationContext);

        // Then
        verify(compilationContext).addWhereCondition(whereCond);
        verify(compilationContext).addHavingCondition(havingCond);
    }

    @Test
    void applyNodeJoinNodeWithConditionJoinUsingAndConditionNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final SelectCompilationContext compilationContext = mock(SelectCompilationContext.class);

        final ConditionNode joinCond = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 10);
        final JoinNode joinNode = new JoinNode(null, "INNER", null, "users");
        joinNode.setCondition(joinCond);

        // When
        compiler.applyNode(joinNode, compilationContext);

        // Then
        verify(compilationContext).addJoin(joinNode);
        verify(compilationContext).addJoinCondition(joinCond);

        // When condition is ConditionJoinUsingNode
        final ConditionJoinUsingNode usingNode = new ConditionJoinUsingNode(null, LogicOperator.AND, "user_id", null);
        final JoinNode joinUsing = new JoinNode(null, "LEFT", null, "orders");
        joinUsing.setCondition(usingNode);

        compiler.applyNode(joinUsing, compilationContext);
        verify(compilationContext).addJoin(joinUsing);
        verify(compilationContext).addJoinCondition(usingNode);
    }

    @Test
    void applyNodeConditionWithIdNode() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final SelectCompilationContext compilationContext = mock(SelectCompilationContext.class);

        final ConditionWithIdNode withIdNode = new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 123);
        final ConditionNode translatedNode = new ConditionNode(null, LogicOperator.AND, "id", null, Operator.EQ, 123);
        when(compilationContext.toConditionNode(withIdNode)).thenReturn(translatedNode);

        final WhereNode whereNode = new WhereNode(null, withIdNode);

        // When
        compiler.applyNode(whereNode, compilationContext);

        // Then
        verify(compilationContext).toConditionNode(withIdNode);
        verify(compilationContext).addWhereCondition(translatedNode);
    }

    @Test
    void applyConditionGroupNodeInWhereHavingAndJoin() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final SelectCompilationContext compilationContext = mock(SelectCompilationContext.class);

        final ConditionGroupSpecStack whereStack = mock(ConditionGroupSpecStack.class);
        final ConditionGroupSpecStack havingStack = mock(ConditionGroupSpecStack.class);
        final ConditionGroupSpecStack joinStack = mock(ConditionGroupSpecStack.class);

        when(compilationContext.ensureWhereConditionGroupStack()).thenReturn(whereStack);
        when(compilationContext.ensureHavingConditionGroupStack()).thenReturn(havingStack);
        when(compilationContext.joinConditionGroupStack()).thenReturn(joinStack);

        final ConditionNode child = new ConditionNode(null, LogicOperator.AND, "x", null, Operator.EQ, 1);
        final ConditionGroupNode group = new ConditionGroupNode(null, LogicOperator.OR, child);

        // When WHERE group
        compiler.applyNode(new WhereNode(null, group), compilationContext);
        verify(whereStack).push(LogicOperator.OR);
        verify(compilationContext).addWhereCondition(child);
        verify(whereStack).pop();

        // When HAVING group
        compiler.applyNode(new HavingNode(null, group), compilationContext);
        verify(havingStack).push(LogicOperator.OR);
        verify(compilationContext).addHavingCondition(child);
        verify(havingStack).pop();

        // When JOIN group
        final JoinNode joinWithGroup = new JoinNode(null, "INNER", null, "table");
        joinWithGroup.setCondition(group);
        compiler.applyNode(joinWithGroup, compilationContext);
        verify(joinStack).push(LogicOperator.OR);
        verify(compilationContext).addJoinCondition(child);
        verify(joinStack).pop();
    }

    @Test
    void applyNodeUnsupportedNodeThrowsException() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final SelectCompilationContext compilationContext = mock(SelectCompilationContext.class);
        final DeleteNode unsupported = new DeleteNode(null, "items", null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> compiler.applyNode(unsupported, compilationContext));
    }

    @Test
    void applyConditionNodeUnsupportedConditionThrowsException() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final SelectQueryCompiler compiler = new SelectQueryCompiler(context);
        final SelectCompilationContext compilationContext = mock(SelectCompilationContext.class);
        final DeleteNode nonConditionNode = new DeleteNode(null, "items", null);
        final WhereNode whereNode = new WhereNode(null, nonConditionNode);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> compiler.applyNode(whereNode, compilationContext));
    }
}
