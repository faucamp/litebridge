package org.litebridge.orm.api.select.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class DtoWhereConditionClauseTerminalTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    private SelectNode selectNode;

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        selectNode = new SelectNode(null, SelectTestDto.class, null, null, null, null);
    }

    @Test
    void and_withField_whenNodeIsWhereNode() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "name", null, Operator.EQ, "Alice");
        final WhereNode whereNode = new WhereNode(selectNode, initialCondition);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoWhereConditionClause<SelectTestDto> clause = terminal.and("age");
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = clause.gt(18);

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertSame(whereNode, node);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals("age", conditionNode.lhsColumn());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.GT, conditionNode.operator());
        assertEquals(18, conditionNode.rhs());
    }

    @Test
    void and_withField_whenNodeIsNotWhereNode() {
        // Given
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoWhereConditionClause<SelectTestDto> clause = terminal.and("name");
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = clause.eq("Bob");

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        final WhereNode whereNode = assertInstanceOf(WhereNode.class, node);
        assertSame(selectNode, whereNode.previous());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals("name", conditionNode.lhsColumn());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals("Bob", conditionNode.rhs());
    }

    @Test
    void and_withExpression() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "name", null, Operator.EQ, "Alice");
        final WhereNode whereNode = new WhereNode(selectNode, initialCondition);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("age");

        // When
        final DtoWhereConditionClause<SelectTestDto> clause = terminal.and(expr);
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = clause.lte(60);

        // Then
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(expr, conditionNode.lhsExpression());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.LTE, conditionNode.operator());
        assertEquals(60, conditionNode.rhs());
    }

    @Test
    void and_withQueryConditionBuilder_success() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "name", null, Operator.EQ, "Alice");
        final WhereNode whereNode = new WhereNode(selectNode, initialCondition);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").gt(21);

        // When
        final DtoWhereConditionClauseTerminal<SelectTestDto> result = terminal.and(builder);

        // Then
        assertSame(terminal, result);
        final ConditionGroupNode groupNode = assertInstanceOf(ConditionGroupNode.class, whereNode.condition());
        assertSame(initialCondition, groupNode.previous());
        assertEquals(LogicOperator.AND, groupNode.logicOperator());
        final ConditionNode childCondition = assertInstanceOf(ConditionNode.class, groupNode.lastChild());
        assertEquals("age", childCondition.lhsColumn());
        assertEquals(Operator.GT, childCondition.operator());
        assertEquals(21, childCondition.rhs());
    }

    @Test
    void and_withQueryConditionBuilder_throwsExceptionWhenNodeNotWhereNode() {
        // Given
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").gt(21);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> terminal.and(builder));
    }

    @Test
    void or_withField() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "name", null, Operator.EQ, "Alice");
        final WhereNode whereNode = new WhereNode(selectNode, initialCondition);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoWhereConditionClause<SelectTestDto> clause = terminal.or("age");
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = clause.lt(30);

        // Then
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals("age", conditionNode.lhsColumn());
        assertEquals(LogicOperator.OR, conditionNode.logicOperator());
        assertEquals(Operator.LT, conditionNode.operator());
        assertEquals(30, conditionNode.rhs());
    }

    @Test
    void or_withExpression() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "name", null, Operator.EQ, "Alice");
        final WhereNode whereNode = new WhereNode(selectNode, initialCondition);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("age");

        // When
        final DtoWhereConditionClause<SelectTestDto> clause = terminal.or(expr);
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = clause.gte(18);

        // Then
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(expr, conditionNode.lhsExpression());
        assertEquals(LogicOperator.OR, conditionNode.logicOperator());
        assertEquals(Operator.GTE, conditionNode.operator());
        assertEquals(18, conditionNode.rhs());
    }

    @Test
    void or_withQueryConditionBuilder_success() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "name", null, Operator.EQ, "Alice");
        final WhereNode whereNode = new WhereNode(selectNode, initialCondition);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").lt(10);

        // When
        final DtoWhereConditionClauseTerminal<SelectTestDto> result = terminal.or(builder);

        // Then
        assertSame(terminal, result);
        final ConditionGroupNode groupNode = assertInstanceOf(ConditionGroupNode.class, whereNode.condition());
        assertSame(initialCondition, groupNode.previous());
        assertEquals(LogicOperator.OR, groupNode.logicOperator());
        final ConditionNode childCondition = assertInstanceOf(ConditionNode.class, groupNode.lastChild());
        assertEquals("age", childCondition.lhsColumn());
        assertEquals(Operator.LT, childCondition.operator());
        assertEquals(10, childCondition.rhs());
    }

    @Test
    void or_withQueryConditionBuilder_throwsExceptionWhenNodeNotWhereNode() {
        // Given
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(selectNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").lt(10);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> terminal.or(builder));
    }

    @Test
    void groupBy_withFields() {
        // Given
        final WhereNode whereNode = new WhereNode(selectNode, null);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoGroupByClauseTerminal<SelectTestDto> groupByTerminal = terminal.groupBy("name");

        // Then
        assertNotNull(groupByTerminal);
        final GroupByNode node = assertInstanceOf(GroupByNode.class, SelectTerminalInspector.getNode(groupByTerminal));
        assertSame(whereNode, node.previous());
        assertEquals("name", node.columns()[0]);
    }

    @Test
    void groupBy_withExpressions() {
        // Given
        final WhereNode whereNode = new WhereNode(selectNode, null);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("name");

        // When
        final DtoGroupByClauseTerminal<SelectTestDto> groupByTerminal = terminal.groupBy(expr);

        // Then
        assertNotNull(groupByTerminal);
        final GroupByNode node = assertInstanceOf(GroupByNode.class, SelectTerminalInspector.getNode(groupByTerminal));
        assertSame(whereNode, node.previous());
        assertEquals(expr, node.expressions()[0]);
    }

    @Test
    void orderBy_withFields() {
        // Given
        final WhereNode whereNode = new WhereNode(selectNode, null);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoOrderByClause<SelectTestDto> orderByClause = terminal.orderBy("age");
        final DtoOrderByClauseChain<SelectTestDto> chain = orderByClause.asc();

        // Then
        assertNotNull(chain);
        final OrderByNode node = assertInstanceOf(OrderByNode.class, SelectTerminalInspector.getNode(chain));
        assertSame(whereNode, node.previous());
        assertEquals("age", node.column());
        assertEquals(true, node.ascending());
    }

    @Test
    void orderBy_withExpressions() {
        // Given
        final WhereNode whereNode = new WhereNode(selectNode, null);
        final DtoWhereConditionClauseTerminal<SelectTestDto> terminal = new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("age");

        // When
        final DtoOrderByClause<SelectTestDto> orderByClause = terminal.orderBy(expr);
        final DtoOrderByClauseChain<SelectTestDto> chain = orderByClause.desc();

        // Then
        assertNotNull(chain);
        final OrderByNode node = assertInstanceOf(OrderByNode.class, SelectTerminalInspector.getNode(chain));
        assertSame(whereNode, node.previous());
        assertEquals(expr, node.expression());
        assertEquals(false, node.ascending());
    }
}
