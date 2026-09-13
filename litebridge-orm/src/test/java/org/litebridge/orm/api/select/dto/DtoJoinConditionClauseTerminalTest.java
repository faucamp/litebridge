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
import org.litebridge.orm.engine.ast.JoinNode;
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
import static org.mockito.Mockito.mock;

class DtoJoinConditionClauseTerminalTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    private JoinNode joinNode;
    private DtoJoinConditionClauseTerminal<SelectTestDto> terminal;

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        final SelectNode selectNode = new SelectNode(null, SelectTestDto.class, null, null, null, null);
        joinNode = new JoinNode(selectNode, "INNER", SelectTestDto.class, null);
        terminal = new DtoJoinConditionClauseTerminal<>(joinNode, selectEngineTerminal, litebridgeContext);
    }

    @Test
    void and_withField() {
        // When
        final DtoJoinConditionClause<SelectTestDto> clause = terminal.and("name");
        final DtoJoinConditionClauseTerminal<SelectTestDto> nextTerminal = clause.eq("Alice");

        // Then
        assertSame(terminal, nextTerminal);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, joinNode.condition());
        assertEquals("name", conditionNode.lhsColumn());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals("Alice", conditionNode.rhs());
    }

    @Test
    void and_withExpression() {
        // Given
        final ExpressionSpec expr = Fn.column("age");

        // When
        final DtoJoinConditionClause<SelectTestDto> clause = terminal.and(expr);
        final DtoJoinConditionClauseTerminal<SelectTestDto> nextTerminal = clause.gt(18);

        // Then
        assertSame(terminal, nextTerminal);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, joinNode.condition());
        assertEquals(expr, conditionNode.lhsExpression());
        assertEquals(LogicOperator.AND, conditionNode.logicOperator());
        assertEquals(Operator.GT, conditionNode.operator());
        assertEquals(18, conditionNode.rhs());
    }

    @Test
    void and_withQueryConditionBuilder() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "status", null, Operator.EQ, "ACTIVE");
        joinNode.setCondition(initialCondition);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").gte(21);

        // When
        final DtoJoinConditionClauseTerminal<SelectTestDto> nextTerminal = terminal.and(builder);

        // Then
        assertSame(terminal, nextTerminal);
        final ConditionGroupNode groupNode = assertInstanceOf(ConditionGroupNode.class, joinNode.condition());
        assertSame(initialCondition, groupNode.previous());
        assertEquals(LogicOperator.AND, groupNode.logicOperator());
        final ConditionNode childCondition = assertInstanceOf(ConditionNode.class, groupNode.lastChild());
        assertEquals("age", childCondition.lhsColumn());
        assertEquals(Operator.GTE, childCondition.operator());
        assertEquals(21, childCondition.rhs());
    }

    @Test
    void or_withField() {
        // When
        final DtoJoinConditionClause<SelectTestDto> clause = terminal.or("age");
        final DtoJoinConditionClauseTerminal<SelectTestDto> nextTerminal = clause.lt(30);

        // Then
        assertSame(terminal, nextTerminal);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, joinNode.condition());
        assertEquals("age", conditionNode.lhsColumn());
        assertEquals(LogicOperator.OR, conditionNode.logicOperator());
        assertEquals(Operator.LT, conditionNode.operator());
        assertEquals(30, conditionNode.rhs());
    }

    @Test
    void or_withExpression() {
        // Given
        final ExpressionSpec expr = Fn.column("age");

        // When
        final DtoJoinConditionClause<SelectTestDto> clause = terminal.or(expr);
        final DtoJoinConditionClauseTerminal<SelectTestDto> nextTerminal = clause.lte(60);

        // Then
        assertSame(terminal, nextTerminal);
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, joinNode.condition());
        assertEquals(expr, conditionNode.lhsExpression());
        assertEquals(LogicOperator.OR, conditionNode.logicOperator());
        assertEquals(Operator.LTE, conditionNode.operator());
        assertEquals(60, conditionNode.rhs());
    }

    @Test
    void or_withQueryConditionBuilder() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "status", null, Operator.EQ, "INACTIVE");
        joinNode.setCondition(initialCondition);
        final QueryConditionBuilder<SelectTestDto> builder = q -> q.where("age").isNull();

        // When
        final DtoJoinConditionClauseTerminal<SelectTestDto> nextTerminal = terminal.or(builder);

        // Then
        assertSame(terminal, nextTerminal);
        final ConditionGroupNode groupNode = assertInstanceOf(ConditionGroupNode.class, joinNode.condition());
        assertSame(initialCondition, groupNode.previous());
        assertEquals(LogicOperator.OR, groupNode.logicOperator());
        final ConditionNode childCondition = assertInstanceOf(ConditionNode.class, groupNode.lastChild());
        assertEquals("age", childCondition.lhsColumn());
        assertEquals(Operator.IS_NULL, childCondition.operator());
    }

    @Test
    void where_withField() {
        // When
        final DtoWhereConditionClause<SelectTestDto> whereClause = terminal.where("name");
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = whereClause.eq("Bob");

        // Then
        assertNotNull(nextTerminal);
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        final WhereNode whereNode = assertInstanceOf(WhereNode.class, node);
        assertSame(joinNode, whereNode.previous());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals("name", conditionNode.lhsColumn());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals("Bob", conditionNode.rhs());
    }

    @Test
    void where_withExpression() {
        // Given
        final ExpressionSpec expr = Fn.column("age");

        // When
        final DtoWhereConditionClause<SelectTestDto> whereClause = terminal.where(expr);
        final DtoWhereConditionClauseTerminal<SelectTestDto> nextTerminal = whereClause.gt(25);

        // Then
        assertNotNull(nextTerminal);
        final WhereNode whereNode = assertInstanceOf(WhereNode.class, SelectTerminalInspector.getNode(nextTerminal));
        assertSame(joinNode, whereNode.previous());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(expr, conditionNode.lhsExpression());
        assertEquals(Operator.GT, conditionNode.operator());
        assertEquals(25, conditionNode.rhs());
    }

    @Test
    void join() {
        // When
        final DtoJoinClause<SelectTestDto> newJoinClause = terminal.join(SelectTestDto.class);
        final DtoJoinConditionClauseTerminal<SelectTestDto> nextTerminal = newJoinClause.on("id");

        // Then
        assertNotNull(nextTerminal);
        final JoinNode secondJoinNode = assertInstanceOf(JoinNode.class, SelectTerminalInspector.getNode(nextTerminal));
        assertSame(joinNode, secondJoinNode.previous());
        assertEquals("INNER", secondJoinNode.type());
        assertEquals(SelectTestDto.class, secondJoinNode.dtoClass());
    }

    @Test
    void groupBy_withFields() {
        // When
        final DtoGroupByClauseTerminal<SelectTestDto> groupByTerminal = terminal.groupBy("name");

        // Then
        assertNotNull(groupByTerminal);
        final GroupByNode node = assertInstanceOf(GroupByNode.class, SelectTerminalInspector.getNode(groupByTerminal));
        assertSame(joinNode, node.previous());
        assertEquals("name", node.columns()[0]);
    }

    @Test
    void groupBy_withExpressions() {
        // Given
        final ExpressionSpec expr = Fn.column("name");

        // When
        final DtoGroupByClauseTerminal<SelectTestDto> groupByTerminal = terminal.groupBy(expr);

        // Then
        assertNotNull(groupByTerminal);
        final GroupByNode node = assertInstanceOf(GroupByNode.class, SelectTerminalInspector.getNode(groupByTerminal));
        assertSame(joinNode, node.previous());
        assertEquals(expr, node.expressions()[0]);
    }

    @Test
    void orderBy_withFields() {
        // When
        final DtoOrderByClause<SelectTestDto> orderByClause = terminal.orderBy("age");
        final DtoOrderByClauseChain<SelectTestDto> chain = orderByClause.asc();

        // Then
        assertNotNull(chain);
        final OrderByNode node = assertInstanceOf(OrderByNode.class, SelectTerminalInspector.getNode(chain));
        assertSame(joinNode, node.previous());
        assertEquals("age", node.column());
        assertEquals(true, node.ascending());
    }

    @Test
    void orderBy_withExpressions() {
        // Given
        final ExpressionSpec expr = Fn.column("age");

        // When
        final DtoOrderByClause<SelectTestDto> orderByClause = terminal.orderBy(expr);
        final DtoOrderByClauseChain<SelectTestDto> chain = orderByClause.desc();

        // Then
        assertNotNull(chain);
        final OrderByNode node = assertInstanceOf(OrderByNode.class, SelectTerminalInspector.getNode(chain));
        assertSame(joinNode, node.previous());
        assertEquals(expr, node.expression());
        assertEquals(false, node.ascending());
    }
}
