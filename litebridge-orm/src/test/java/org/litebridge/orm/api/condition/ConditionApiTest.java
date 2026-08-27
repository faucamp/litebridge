//package org.litebridge.orm.api.condition;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.db.spi.query.Operator;
//import org.litebridge.orm.api.select.ast.ConditionNode;
//import org.litebridge.orm.api.select.ast.QueryNode;
//import org.litebridge.orm.engine.FromClauseEngine;
//import org.litebridge.orm.expression.ExpressionSpec;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.mock;
//
//class ConditionApiTest {
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testBasicOperators() {
//        final FromClauseEngine engine = mock(FromClauseEngine.class);
//        final ExpressionSpec lhs = new org.litebridge.orm.expression.select.SelectColumnSpec(new org.litebridge.db.spi.Column(new org.litebridge.db.spi.Table("TEST"), "COL"));
//
//        // Using a custom creator to capture the node
//        final QueryNode[] capturedNode = new QueryNode[1];
//        final AbstractCbConditionClause<Object> capturingClause = new AbstractCbConditionClause<>(engine, LogicOperator.NOOP, lhs, null, n -> {
//            capturedNode[0] = n;
//            return null;
//        }) {
//            @Override
//            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal(QueryNode conditionNode) {
//                capturedNode[0] = conditionNode;
//                return null;
//            }
//        };
//
//        capturingClause.eq("val");
//        assertEquals(Operator.EQ, ((ConditionNode) capturedNode[0]).operator());
//        assertEquals("val", ((ConditionNode) capturedNode[0]).rhs());
//
//        capturingClause.neq("val");
//        assertEquals(Operator.NEQ, ((ConditionNode) capturedNode[0]).operator());
//
//        capturingClause.lt(10);
//        assertEquals(Operator.LT, ((ConditionNode) capturedNode[0]).operator());
//
//        capturingClause.lte(10);
//        assertEquals(Operator.LTE, ((ConditionNode) capturedNode[0]).operator());
//
//        capturingClause.gt(10);
//        assertEquals(Operator.GT, ((ConditionNode) capturedNode[0]).operator());
//
//        capturingClause.gte(10);
//        assertEquals(Operator.GTE, ((ConditionNode) capturedNode[0]).operator());
//
//        capturingClause.like("%val%");
//        assertEquals(Operator.LIKE, ((ConditionNode) capturedNode[0]).operator());
//
//        capturingClause.isNull();
//        assertEquals(Operator.IS_NULL, ((ConditionNode) capturedNode[0]).operator());
//
//        capturingClause.isNotNull();
//        assertEquals(Operator.IS_NOT_NULL, ((ConditionNode) capturedNode[0]).operator());
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testInOperators() {
//        final FromClauseEngine engine = mock(FromClauseEngine.class);
//        final ExpressionSpec lhs = new org.litebridge.orm.expression.select.SelectColumnSpec(new org.litebridge.db.spi.Column(new org.litebridge.db.spi.Table("TEST"), "COL"));
//        final QueryNode[] capturedNode = new QueryNode[1];
//        final AbstractCbConditionClause<Object> capturingClause = new AbstractCbConditionClause<>(engine, LogicOperator.NOOP, lhs, null, n -> {
//            capturedNode[0] = n;
//            return null;
//        }) {
//            @Override
//            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal(QueryNode conditionNode) {
//                capturedNode[0] = conditionNode;
//                return null;
//            }
//        };
//
//        capturingClause.in(1, 2, 3);
//        assertEquals(Operator.IN, ((ConditionNode) capturedNode[0]).operator());
//        assertEquals(List.of(1, 2, 3), ((ConditionNode) capturedNode[0]).rhs());
//
//        capturingClause.in(List.of(4, 5));
//        assertEquals(List.of(4, 5), ((ConditionNode) capturedNode[0]).rhs());
//
//        capturingClause.notIn(1, 2);
//        assertEquals(Operator.NOT_IN, ((ConditionNode) capturedNode[0]).operator());
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    void testNullHandling() {
//        final FromClauseEngine engine = mock(FromClauseEngine.class);
//        final ExpressionSpec lhs = new org.litebridge.orm.expression.select.SelectColumnSpec(new org.litebridge.db.spi.Column(new org.litebridge.db.spi.Table("TEST"), "COL"));
//        final QueryNode[] capturedNode = new QueryNode[1];
//        final AbstractCbConditionClause<Object> capturingClause = new AbstractCbConditionClause<>(engine, LogicOperator.NOOP, lhs, null, n -> {
//            capturedNode[0] = n;
//            return null;
//        }) {
//            @Override
//            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal(QueryNode conditionNode) {
//                capturedNode[0] = conditionNode;
//                return null;
//            }
//        };
//
//        capturingClause.eq(null);
//        assertEquals(Operator.IS_NULL, ((ConditionNode) capturedNode[0]).operator());
//
//        capturingClause.neq(null);
//        assertEquals(Operator.IS_NOT_NULL, ((ConditionNode) capturedNode[0]).operator());
//
//        assertThrows(IllegalArgumentException.class, () -> capturingClause.gt((Object) null));
//    }
//}
