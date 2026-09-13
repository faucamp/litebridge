package org.litebridge.orm.api.select.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
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

class SqlFromClauseTerminalTest {

    private SelectEngineTerminal selectEngineTerminal;
    private LitebridgeContext litebridgeContext;
    private SelectNode selectNode;
    private SqlFromClauseTerminal terminal;

    @BeforeEach
    void setUp() {
        selectEngineTerminal = mock(SelectEngineTerminal.class);
        litebridgeContext = mock(LitebridgeContext.class);
        selectNode = new SelectNode("users", null, null, null, null, null);
        terminal = new SqlFromClauseTerminal(selectNode, selectEngineTerminal, litebridgeContext);
    }

    @Test
    void where_withColumn() {
        // Given
        final String column = "name";

        // When
        final SqlWhereConditionClause clause = terminal.where(column);

        // Then
        assertNotNull(clause);
        final SqlWhereConditionClauseTerminal nextTerminal = clause.eq("Alice");
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(WhereNode.class, node);
        final WhereNode whereNode = (WhereNode) node;
        assertSame(selectNode, whereNode.previous());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals("name", conditionNode.lhsColumn());
        assertEquals(Operator.EQ, conditionNode.operator());
        assertEquals("Alice", conditionNode.rhs());
    }

    @Test
    void where_withExpression() {
        // Given
        final ExpressionSpec expression = Fn.column("age");

        // When
        final SqlWhereConditionClause clause = terminal.where(expression);

        // Then
        assertNotNull(clause);
        final SqlWhereConditionClauseTerminal nextTerminal = clause.gt(18);
        final QueryNode node = SelectTerminalInspector.getNode(nextTerminal);
        assertInstanceOf(WhereNode.class, node);
        final WhereNode whereNode = (WhereNode) node;
        assertSame(selectNode, whereNode.previous());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, whereNode.condition());
        assertEquals(expression, conditionNode.lhsExpression());
        assertEquals(Operator.GT, conditionNode.operator());
        assertEquals(18, conditionNode.rhs());
    }

    @Test
    void join() {
        // Given
        final String joinTable = "orders";

        // When
        final SqlJoinClause joinClause = terminal.join(joinTable);

        // Then
        assertNotNull(joinClause);
        final SqlJoinConditionClauseTerminal joinTerminal = joinClause.using("user_id");
        final QueryNode node = SelectTerminalInspector.getNode(joinTerminal);
        assertInstanceOf(JoinNode.class, node);
        final JoinNode joinNode = (JoinNode) node;
        assertSame(selectNode, joinNode.previous());
        assertEquals("INNER", joinNode.type());
        assertEquals("orders", joinNode.rightTable());
    }

    @Test
    void groupBy_withColumns() {
        // Given
        final String[] columns = new String[]{"name", "age"};

        // When
        final SqlGroupByClauseTerminal groupByTerminal = terminal.groupBy(columns);

        // Then
        assertNotNull(groupByTerminal);
        final QueryNode node = SelectTerminalInspector.getNode(groupByTerminal);
        assertInstanceOf(GroupByNode.class, node);
        final GroupByNode groupByNode = (GroupByNode) node;
        assertSame(selectNode, groupByNode.previous());
        assertEquals(columns, groupByNode.columns());
    }

    @Test
    void groupBy_withExpressions() {
        // Given
        final ExpressionSpec[] expressions = new ExpressionSpec[]{Fn.column("name")};

        // When
        final SqlGroupByClauseTerminal groupByTerminal = terminal.groupBy(expressions);

        // Then
        assertNotNull(groupByTerminal);
        final QueryNode node = SelectTerminalInspector.getNode(groupByTerminal);
        assertInstanceOf(GroupByNode.class, node);
        final GroupByNode groupByNode = (GroupByNode) node;
        assertSame(selectNode, groupByNode.previous());
        assertEquals(expressions, groupByNode.expressions());
    }

    @Test
    void orderBy_withColumns() {
        // Given
        final String[] columns = new String[]{"name"};

        // When
        final SqlOrderByClause orderByClause = terminal.orderBy(columns);

        // Then
        assertNotNull(orderByClause);
        final SqlOrderByClauseChain chain = orderByClause.asc();
        final QueryNode node = SelectTerminalInspector.getNode(chain);
        assertInstanceOf(OrderByNode.class, node);
        final OrderByNode orderByNode = (OrderByNode) node;
        assertSame(selectNode, orderByNode.previous());
        assertEquals("name", orderByNode.column());
        assertEquals(true, orderByNode.ascending());
    }

    @Test
    void orderBy_withExpressions() {
        // Given
        final ExpressionSpec[] expressions = new ExpressionSpec[]{Fn.column("age")};

        // When
        final SqlOrderByClause orderByClause = terminal.orderBy(expressions);

        // Then
        assertNotNull(orderByClause);
        final SqlOrderByClauseChain chain = orderByClause.desc();
        final QueryNode node = SelectTerminalInspector.getNode(chain);
        assertInstanceOf(OrderByNode.class, node);
        final OrderByNode orderByNode = (OrderByNode) node;
        assertSame(selectNode, orderByNode.previous());
        assertEquals(expressions[0], orderByNode.expression());
        assertEquals(false, orderByNode.ascending());
    }
}
