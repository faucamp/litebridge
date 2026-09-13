package org.litebridge.orm.api.select.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Row;
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

class SqlWhereConditionClauseTerminalTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    private SelectNode selectNode;

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        selectNode = new SelectNode(null, null, null, null, null, null);
    }

    @Test
    void and_withColumn_whenNodeIsWhereNode() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "name", null, Operator.EQ, "Alice");
        final WhereNode whereNode = new WhereNode(selectNode, initialCondition);
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);

        // When
        final SqlWhereConditionClause clause = terminal.and("age");
        final SqlWhereConditionClauseTerminal nextTerminal = clause.gt(18);

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
    void and_withColumn_whenNodeIsNotWhereNode() {
        // Given
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", selectNode, selectEngineTerminal, litebridgeContext);

        // When
        final SqlWhereConditionClause clause = terminal.and("name");
        final SqlWhereConditionClauseTerminal nextTerminal = clause.eq("Bob");

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
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("age");

        // When
        final SqlWhereConditionClause clause = terminal.and(expr);
        final SqlWhereConditionClauseTerminal nextTerminal = clause.lte(60);

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
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<Row> builder = q -> q.where("age").gt(21);

        // When
        final SqlWhereConditionClauseTerminal result = terminal.and(builder);

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
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", selectNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<Row> builder = q -> q.where("age").gt(21);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> terminal.and(builder));
        assertEquals("AST error: Expected a WhereNode but got " + selectNode, ex.getMessage());
    }

    @Test
    void or_withColumn() {
        // Given
        final ConditionNode initialCondition = new ConditionNode(null, LogicOperator.NOOP, "name", null, Operator.EQ, "Alice");
        final WhereNode whereNode = new WhereNode(selectNode, initialCondition);
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);

        // When
        final SqlWhereConditionClause clause = terminal.or("age");
        final SqlWhereConditionClauseTerminal nextTerminal = clause.lt(30);

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
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("age");

        // When
        final SqlWhereConditionClause clause = terminal.or(expr);
        final SqlWhereConditionClauseTerminal nextTerminal = clause.gte(18);

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
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<Row> builder = q -> q.where("age").lt(10);

        // When
        final SqlWhereConditionClauseTerminal result = terminal.or(builder);

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
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", selectNode, selectEngineTerminal, litebridgeContext);
        final QueryConditionBuilder<Row> builder = q -> q.where("age").lt(10);

        // When & Then
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> terminal.or(builder));
        assertEquals("AST error: Expected a WhereNode but got " + selectNode, ex.getMessage());
    }

    @Test
    void groupBy_withColumns() {
        // Given
        final WhereNode whereNode = new WhereNode(selectNode, null);
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);

        // When
        final SqlGroupByClauseTerminal groupByTerminal = terminal.groupBy("name");

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
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("name");

        // When
        final SqlGroupByClauseTerminal groupByTerminal = terminal.groupBy(expr);

        // Then
        assertNotNull(groupByTerminal);
        final GroupByNode node = assertInstanceOf(GroupByNode.class, SelectTerminalInspector.getNode(groupByTerminal));
        assertSame(whereNode, node.previous());
        assertEquals(expr, node.expressions()[0]);
    }

    @Test
    void orderBy_withColumns() {
        // Given
        final WhereNode whereNode = new WhereNode(selectNode, null);
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);

        // When
        final SqlOrderByClause orderByClause = terminal.orderBy("age");
        final SqlOrderByClauseChain chain = orderByClause.asc();

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
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal("users", whereNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("age");

        // When
        final SqlOrderByClause orderByClause = terminal.orderBy(expr);
        final SqlOrderByClauseChain chain = orderByClause.desc();

        // Then
        assertNotNull(chain);
        final OrderByNode node = assertInstanceOf(OrderByNode.class, SelectTerminalInspector.getNode(chain));
        assertSame(whereNode, node.previous());
        assertEquals(expr, node.expression());
        assertEquals(false, node.ascending());
    }
}
