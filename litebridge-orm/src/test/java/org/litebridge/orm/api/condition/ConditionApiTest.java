package org.litebridge.orm.api.condition;

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
        final ExpressionSpec lhs = new org.litebridge.orm.expression.select.SelectColumnSpec(new Column(new Table("TEST"), "COL"));
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);

        // Using a custom creator to capture the node
        final QueryNode[] capturedNode = new QueryNode[1];
        final AbstractCbConditionClause<Object> capturingClause = createCapturingClause(litebridgeContext, lhs, capturedNode);
        assertEquals(Operator.EQ, ((ConditionNode) capturedNode[0]).operator());
        assertEquals("val", ((ConditionNode) capturedNode[0]).rhs());

        capturingClause.neq("val");
        assertEquals(Operator.NEQ, ((ConditionNode) capturedNode[0]).operator());

        capturingClause.lt(10);
        assertEquals(Operator.LT, ((ConditionNode) capturedNode[0]).operator());

        capturingClause.lte(10);
        assertEquals(Operator.LTE, ((ConditionNode) capturedNode[0]).operator());

        capturingClause.gt(10);
        assertEquals(Operator.GT, ((ConditionNode) capturedNode[0]).operator());

        capturingClause.gte(10);
        assertEquals(Operator.GTE, ((ConditionNode) capturedNode[0]).operator());

        capturingClause.like("%val%");
        assertEquals(Operator.LIKE, ((ConditionNode) capturedNode[0]).operator());

        capturingClause.isNull();
        assertEquals(Operator.IS_NULL, ((ConditionNode) capturedNode[0]).operator());

        capturingClause.isNotNull();
        assertEquals(Operator.IS_NOT_NULL, ((ConditionNode) capturedNode[0]).operator());
    }

    private static AbstractCbConditionClause<Object> createCapturingClause(final LitebridgeContext litebridgeContext, final ExpressionSpec lhs, final QueryNode[] capturedNode) {
        final AbstractCbConditionClause<Object> capturingClause = new AbstractCbConditionClause<>(
                litebridgeContext,
                LogicOperator.NOOP,
                null,
                lhs,
                null,
                n -> {
                    capturedNode[0] = n;
                    return null;
                }) {
            @Override
            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal(QueryNode conditionNode) {
                capturedNode[0] = conditionNode;
                return null;
            }
        };

        capturingClause.eq("val");
        return capturingClause;
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInOperators() {
        final ExpressionSpec lhs = new SelectColumnSpec(new Column(new org.litebridge.db.spi.Table("TEST"), "COL"));
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);

        final QueryNode[] capturedNode = new QueryNode[1];
        final AbstractCbConditionClause<Object> capturingClause = new AbstractCbConditionClause<>(
                litebridgeContext,
                LogicOperator.NOOP,
                null,
                lhs, null,
                n -> {
                    capturedNode[0] = n;
                    return null;
                }) {
            @Override
            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal(QueryNode conditionNode) {
                capturedNode[0] = conditionNode;
                return null;
            }
        };

        capturingClause.in(1, 2, 3);
        assertEquals(Operator.IN, ((ConditionNode) capturedNode[0]).operator());
        assertEquals(List.of(1, 2, 3), ((ConditionNode) capturedNode[0]).rhs());

        capturingClause.in(List.of(4, 5));
        assertEquals(List.of(4, 5), ((ConditionNode) capturedNode[0]).rhs());

        capturingClause.notIn(1, 2);
        assertEquals(Operator.NOT_IN, ((ConditionNode) capturedNode[0]).operator());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNullHandling() {
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final ExpressionSpec lhs = new org.litebridge.orm.expression.select.SelectColumnSpec(new Column(new Table("TEST"), "COL"));
        final QueryNode[] capturedNode = new QueryNode[1];
        final AbstractCbConditionClause<Object> capturingClause = createCapturingClause(litebridgeContext, lhs, capturedNode);

        capturingClause.eq(null);
        assertEquals(Operator.IS_NULL, ((ConditionNode) capturedNode[0]).operator());

        capturingClause.neq(null);
        assertEquals(Operator.IS_NOT_NULL, ((ConditionNode) capturedNode[0]).operator());

        assertThrows(IllegalArgumentException.class, () -> capturingClause.gt((Object) null));
    }
}
