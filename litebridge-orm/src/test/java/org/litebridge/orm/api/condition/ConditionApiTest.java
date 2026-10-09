package org.litebridge.orm.api.condition;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ConditionApiTest {

    @Test
    @SuppressWarnings("unchecked")
    void testBasicOperators() {
        final ExpressionSpec lhs = new SelectColumnSpec(new Column(new Table("TEST"), "COL"));
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);

        final CbDtoConditionClause<Object> clause = createClause(litebridgeContext, lhs);

        CbDtoConditionClauseTerminal<Object> terminal = clause.eq("val");
        assertEquals(Operator.EQ, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());
        assertEquals("val", ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).rhs());

        terminal = clause.neq("val");
        assertEquals(Operator.NEQ, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        terminal = clause.lt(10);
        assertEquals(Operator.LT, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        terminal = clause.lte(10);
        assertEquals(Operator.LTE, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        terminal = clause.gt(10);
        assertEquals(Operator.GT, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        terminal = clause.gte(10);
        assertEquals(Operator.GTE, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        terminal = clause.like("%val%");
        assertEquals(Operator.LIKE, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        terminal = clause.notLike("%val%");
        assertEquals(Operator.NOT_LIKE, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        terminal = clause.isNull();
        assertEquals(Operator.IS_NULL, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        terminal = clause.isNotNull();
        assertEquals(Operator.IS_NOT_NULL, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());
    }

    private static CbDtoConditionClause<Object> createClause(final LitebridgeContext litebridgeContext, final ExpressionSpec lhs) {
        return new CbDtoConditionClause<>(
                litebridgeContext,
                LogicOperator.NOOP,
                null,
                lhs,
                null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInOperators() {
        final ExpressionSpec lhs = new SelectColumnSpec(new Column(new Table("TEST"), "COL"));
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);

        final CbDtoConditionClause<Object> clause = createClause(litebridgeContext, lhs);

        CbDtoConditionClauseTerminal<Object> terminal = clause.in(1, 2, 3);
        assertEquals(Operator.IN, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());
        assertEquals(List.of(1, 2, 3), ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).rhs());

        terminal = clause.in(List.of(4, 5));
        assertEquals(List.of(4, 5), ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).rhs());

        terminal = clause.notIn(1, 2);
        assertEquals(Operator.NOT_IN, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNullHandling() {
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final ExpressionSpec lhs = new SelectColumnSpec(new Column(new Table("TEST"), "COL"));
        final CbDtoConditionClause<Object> clause = createClause(litebridgeContext, lhs);

        CbDtoConditionClauseTerminal<Object> terminal = clause.eq(null);
        assertEquals(Operator.IS_NULL, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        terminal = clause.neq(null);
        assertEquals(Operator.IS_NOT_NULL, ((ConditionNode) CbConditionClauseTerminalInspector.getNode(terminal)).operator());

        assertThrows(IllegalArgumentException.class, () -> clause.gt((Object) null));
    }
}
