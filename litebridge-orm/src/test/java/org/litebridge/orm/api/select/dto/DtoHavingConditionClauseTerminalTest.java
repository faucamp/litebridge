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
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Fn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class DtoHavingConditionClauseTerminalTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    private GroupByNode groupByNode;

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        final SelectNode selectNode = new SelectNode(null, SelectTestDto.class, null, null, null, null);
        groupByNode = new GroupByNode(selectNode, new String[]{"name"}, null);
    }

    @Test
    void and_withField_whenNodeIsNotHavingNode() {
        // Given
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(groupByNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoHavingConditionClause<SelectTestDto> clause = terminal.and("age");
        final DtoHavingConditionClauseTerminal<SelectTestDto> nextTerminal = clause.gt(18);

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(HavingNode.class, node);
        final HavingNode havingNode = (HavingNode) node;
        assertSame(groupByNode, havingNode.previous());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, havingNode.condition());
        assertEquals("age", conditionNode.lhsColumn());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.GT, conditionNode.operator());
        assertEquals(18, conditionNode.rhs());
    }

    @Test
    void and_withField_whenNodeIsHavingNode() {
        // Given
        final ConditionNode existingCondition = new ConditionNode(null, LogicOperator.NOOP, null, Fn.count(), Operator.GT, 5);
        final HavingNode existingHavingNode = new HavingNode(groupByNode, existingCondition);
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(existingHavingNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoHavingConditionClause<SelectTestDto> clause = terminal.and("age");
        final DtoHavingConditionClauseTerminal<SelectTestDto> nextTerminal = clause.lt(65);

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(HavingNode.class, node);
        final HavingNode havingNode = (HavingNode) node;
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, havingNode.condition());
        assertSame(existingCondition, conditionNode.previous());
        assertEquals("age", conditionNode.lhsColumn());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.LT, conditionNode.operator());
        assertEquals(65, conditionNode.rhs());
    }

    @Test
    void and_withExpression_whenNodeIsNotHavingNode() {
        // Given
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(groupByNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec countExpr = Fn.count();

        // When
        final DtoHavingConditionClause<SelectTestDto> clause = terminal.and(countExpr);
        final DtoHavingConditionClauseTerminal<SelectTestDto> nextTerminal = clause.gte(2);

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(HavingNode.class, node);
        final HavingNode havingNode = (HavingNode) node;
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, havingNode.condition());
        assertEquals(countExpr, conditionNode.lhsExpression());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.GTE, conditionNode.operator());
        assertEquals(2, conditionNode.rhs());
    }

    @Test
    void and_withQueryConditionBuilder_whenNodeIsNotHavingNode() {
        // Given
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(groupByNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").gt(21);

        // When
        final DtoHavingConditionClauseTerminal<SelectTestDto> nextTerminal = terminal.and(builder);

        // Then
        assertSame(terminal, nextTerminal);
        final QueryNode node = SelectTerminalInspector.getNode(terminal);
        assertInstanceOf(HavingNode.class, node);
        final HavingNode havingNode = (HavingNode) node;
        assertSame(groupByNode, havingNode.previous());
        final ConditionGroupNode groupNode = assertInstanceOf(ConditionGroupNode.class, havingNode.condition());
        assertNull(groupNode.previous());
        assertEquals(LogicOperator.AND, groupNode.logicOperator());
        final ConditionNode childCondition = assertInstanceOf(ConditionNode.class, groupNode.lastChild());
        assertEquals("age", childCondition.lhsColumn());
        assertEquals(Operator.GT, childCondition.operator());
        assertEquals(21, childCondition.rhs());
    }

    @Test
    void and_withQueryConditionBuilder_whenNodeIsHavingNode() {
        // Given
        final ConditionNode existingCondition = new ConditionNode(null, LogicOperator.NOOP, null, Fn.count(), Operator.GT, 1);
        final HavingNode existingHavingNode = new HavingNode(groupByNode, existingCondition);
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(existingHavingNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("name").isNotNull();

        // When
        final DtoHavingConditionClauseTerminal<SelectTestDto> nextTerminal = terminal.and(builder);

        // Then
        assertSame(terminal, nextTerminal);
        final ConditionGroupNode groupNode = assertInstanceOf(ConditionGroupNode.class, existingHavingNode.condition());
        assertSame(existingCondition, groupNode.previous());
        assertEquals(LogicOperator.AND, groupNode.logicOperator());
        final ConditionNode childCondition = assertInstanceOf(ConditionNode.class, groupNode.lastChild());
        assertEquals("name", childCondition.lhsColumn());
        assertEquals(Operator.IS_NOT_NULL, childCondition.operator());
    }

    @Test
    void or_withField() {
        // Given
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(groupByNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoHavingConditionClause<SelectTestDto> clause = terminal.or("name");
        final DtoHavingConditionClauseTerminal<SelectTestDto> nextTerminal = clause.eq("Alice");

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(HavingNode.class, node);
        final HavingNode havingNode = (HavingNode) node;
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, havingNode.condition());
        assertEquals("name", conditionNode.lhsColumn());
        assertEquals(LogicOperator.OR, conditionNode.logicOperator());
        assertEquals(Operator.EQ, conditionNode.operator());
    }

    @Test
    void or_withExpression() {
        // Given
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(groupByNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec countExpr = Fn.count();

        // When
        final DtoHavingConditionClause<SelectTestDto> clause = terminal.or(countExpr);
        final DtoHavingConditionClauseTerminal<SelectTestDto> nextTerminal = clause.lte(10);

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(HavingNode.class, node);
        final HavingNode havingNode = (HavingNode) node;
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, havingNode.condition());
        assertEquals(countExpr, conditionNode.lhsExpression());
        assertEquals(LogicOperator.OR, conditionNode.logicOperator());
        assertEquals(Operator.LTE, conditionNode.operator());
    }

    @Test
    void or_withQueryConditionBuilder_whenNodeIsNotHavingNode() {
        // Given
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(groupByNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").lt(10);

        // When
        final DtoHavingConditionClauseTerminal<SelectTestDto> nextTerminal = terminal.or(builder);

        // Then
        assertSame(terminal, nextTerminal);
        final HavingNode havingNode = assertInstanceOf(HavingNode.class, SelectTerminalInspector.getNode(terminal));
        final ConditionGroupNode groupNode = assertInstanceOf(ConditionGroupNode.class, havingNode.condition());
        assertNull(groupNode.previous());
        assertEquals(LogicOperator.OR, groupNode.logicOperator());
    }

    @Test
    void or_withQueryConditionBuilder_whenNodeIsHavingNode() {
        // Given
        final ConditionNode existingCondition = new ConditionNode(null, LogicOperator.NOOP, null, Fn.count(), Operator.GT, 1);
        final HavingNode existingHavingNode = new HavingNode(groupByNode, existingCondition);
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(existingHavingNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").lt(10);

        // When
        final DtoHavingConditionClauseTerminal<SelectTestDto> nextTerminal = terminal.or(builder);

        // Then
        assertSame(terminal, nextTerminal);
        final ConditionGroupNode groupNode = assertInstanceOf(ConditionGroupNode.class, existingHavingNode.condition());
        assertSame(existingCondition, groupNode.previous());
        assertEquals(LogicOperator.OR, groupNode.logicOperator());
    }

    @Test
    void orderBy_withFields() {
        // Given
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(groupByNode, selectEngineTerminal, litebridgeContext);

        // When
        final DtoOrderByClause<SelectTestDto> orderByClause = terminal.orderBy("age");

        // Then
        assertNotNull(orderByClause);
        final DtoOrderByClauseChain<SelectTestDto> chain = orderByClause.asc();
        final OrderByNode orderByNode = assertInstanceOf(OrderByNode.class, SelectTerminalInspector.getNode(chain));
        assertEquals("age", orderByNode.column());
        assertEquals(true, orderByNode.ascending());
    }

    @Test
    void orderBy_withExpressions() {
        // Given
        final DtoHavingConditionClauseTerminal<SelectTestDto> terminal = new DtoHavingConditionClauseTerminal<>(groupByNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("age");

        // When
        final DtoOrderByClause<SelectTestDto> orderByClause = terminal.orderBy(expr);

        // Then
        assertNotNull(orderByClause);
        final DtoOrderByClauseChain<SelectTestDto> chain = orderByClause.desc();
        final OrderByNode orderByNode = assertInstanceOf(OrderByNode.class, SelectTerminalInspector.getNode(chain));
        assertEquals(expr, orderByNode.expression());
        assertEquals(false, orderByNode.ascending());
    }
}
