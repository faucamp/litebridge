package org.litebridge.orm.api.select.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class SqlGroupByClauseTerminalTest {

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
    void constructor_withColumns() {
        // Given
        final String[] columns = new String[]{"name", "age"};

        // When
        final SqlGroupByClauseTerminal terminal = new SqlGroupByClauseTerminal("users", columns, selectNode, selectEngineTerminal, litebridgeContext);

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(terminal);
        assertInstanceOf(GroupByNode.class, node);
        final GroupByNode groupByNode = (GroupByNode) node;
        assertSame(selectNode, groupByNode.previous());
        assertEquals(columns, groupByNode.columns());
    }

    @Test
    void constructor_withExpressions() {
        // Given
        final ExpressionSpec[] expressions = new ExpressionSpec[]{Fn.column("name")};

        // When
        final SqlGroupByClauseTerminal terminal = new SqlGroupByClauseTerminal("users", expressions, selectNode, selectEngineTerminal, litebridgeContext);

        // Then
        final QueryNode node = SelectTerminalInspector.getNode(terminal);
        assertInstanceOf(GroupByNode.class, node);
        final GroupByNode groupByNode = (GroupByNode) node;
        assertSame(selectNode, groupByNode.previous());
        assertEquals(expressions, groupByNode.expressions());
    }

    @Test
    void having() {
        // Given
        final String[] columns = new String[]{"name"};
        final SqlGroupByClauseTerminal terminal = new SqlGroupByClauseTerminal("users", columns, selectNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec countExpr = Fn.count();

        // When
        final SqlHavingConditionClause havingClause = terminal.having(countExpr);

        // Then
        assertNotNull(havingClause);
        final SqlHavingConditionClauseTerminal havingTerminal = havingClause.gt(5);
        final QueryNode node = SelectTerminalInspector.getNode(havingTerminal);
        assertInstanceOf(HavingNode.class, node);
        final HavingNode havingNode = (HavingNode) node;
        assertInstanceOf(GroupByNode.class, havingNode.previous());
        final ConditionNode conditionNode = assertInstanceOf(ConditionNode.class, havingNode.condition());
        assertEquals(countExpr, conditionNode.lhsExpression());
        assertEquals(Operator.GT, conditionNode.operator());
        assertEquals(5, conditionNode.rhs());
    }

    @Test
    void orderBy_withColumns() {
        // Given
        final String[] columns = new String[]{"name"};
        final SqlGroupByClauseTerminal terminal = new SqlGroupByClauseTerminal("users", columns, selectNode, selectEngineTerminal, litebridgeContext);

        // When
        final SqlOrderByClause orderByClause = terminal.orderBy("age");

        // Then
        assertNotNull(orderByClause);
        final SqlOrderByClauseChain chain = orderByClause.asc();
        final QueryNode node = SelectTerminalInspector.getNode(chain);
        assertInstanceOf(OrderByNode.class, node);
        final OrderByNode orderByNode = (OrderByNode) node;
        assertInstanceOf(GroupByNode.class, orderByNode.previous());
        assertEquals("age", orderByNode.column());
        assertEquals(true, orderByNode.ascending());
    }

    @Test
    void orderBy_withExpressions() {
        // Given
        final String[] columns = new String[]{"name"};
        final SqlGroupByClauseTerminal terminal = new SqlGroupByClauseTerminal("users", columns, selectNode, selectEngineTerminal, litebridgeContext);
        final ExpressionSpec expr = Fn.column("age");

        // When
        final SqlOrderByClause orderByClause = terminal.orderBy(expr);

        // Then
        assertNotNull(orderByClause);
        final SqlOrderByClauseChain chain = orderByClause.desc();
        final QueryNode node = SelectTerminalInspector.getNode(chain);
        assertInstanceOf(OrderByNode.class, node);
        final OrderByNode orderByNode = (OrderByNode) node;
        assertInstanceOf(GroupByNode.class, orderByNode.previous());
        assertEquals(expr, orderByNode.expression());
        assertEquals(false, orderByNode.ascending());
    }
}
