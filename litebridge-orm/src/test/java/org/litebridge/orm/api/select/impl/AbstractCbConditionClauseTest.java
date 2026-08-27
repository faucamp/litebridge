//package org.litebridge.orm.api.select.impl;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.db.spi.query.Operator;
//import org.litebridge.orm.api.condition.AbstractCbConditionClause;
//import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
//import org.litebridge.orm.api.select.SelectTerminal;
//import org.litebridge.orm.api.select.ast.ConditionNode;
//import org.litebridge.orm.api.select.ast.QueryNode;
//import org.litebridge.orm.engine.FromClauseEngine;
//import org.litebridge.orm.engine.SelectEngine;
//import org.litebridge.orm.expression.ExpressionSpec;
//import org.litebridge.orm.expression.select.SelectColumnSpec;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.function.Function;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.mock;
//
//class AbstractCbConditionClauseTest {
//
//    private FromClauseEngine fromClauseEngine;
//    private AbstractCbConditionClause<Object> clause;
//    private QueryNode[] capturedNode = new QueryNode[1];
//
//    @BeforeEach
//    void setUp() {
//        fromClauseEngine = mock(FromClauseEngine.class);
//        ExpressionSpec lhs = new SelectColumnSpec(new Column(new Table("TEST"), "COL"));
//        clause = new AbstractCbConditionClause<Object>(fromClauseEngine, LogicOperator.NOOP, lhs, null, n -> {
//            capturedNode[0] = n;
//            return null;
//        }) {
//            @Override
//            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal(QueryNode conditionNode) {
//                capturedNode[0] = conditionNode;
//                return null;
//            }
//        };
//    }
//
//    @Test
//    void eq() {
//        // When
//        clause.eq("value");
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.EQ, node.operator());
//        assertEquals("value", node.rhs());
//    }
//
//    @Test
//    void neq() {
//        // When
//        clause.neq("value");
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.NEQ, node.operator());
//    }
//
//    @Test
//    void lt() {
//        // When
//        clause.lt(10);
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.LT, node.operator());
//    }
//
//    @Test
//    void lte() {
//        // When
//        clause.lte(10);
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.LTE, node.operator());
//    }
//
//    @Test
//    void gt() {
//        // When
//        clause.gt(10);
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.GT, node.operator());
//    }
//
//    @Test
//    void gte() {
//        // When
//        clause.gte(10);
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.GTE, node.operator());
//    }
//
//    @Test
//    void like() {
//        // When
//        clause.like("%val%");
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.LIKE, node.operator());
//    }
//
//    @Test
//    void in() {
//        // When
//        clause.in(1, 2, 3);
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.IN, node.operator());
//        assertEquals(List.of(1, 2, 3), node.rhs());
//    }
//
//    @Test
//    void in_collection() {
//        // When
//        clause.in(Arrays.asList(4, 5));
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(List.of(4, 5), node.rhs());
//    }
//
//    @Test
//    void notIn() {
//        // When
//        clause.notIn(1, 2);
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.NOT_IN, node.operator());
//    }
//
//    @Test
//    void isNull() {
//        // When
//        clause.isNull();
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.IS_NULL, node.operator());
//    }
//
//    @Test
//    void isNotNull() {
//        // When
//        clause.isNotNull();
//
//        // Then
//        ConditionNode node = (ConditionNode) capturedNode[0];
//        assertEquals(Operator.IS_NOT_NULL, node.operator());
//    }
//
//    @Test
//    void lte_invalidNullOperator() {
//        assertThrows(IllegalArgumentException.class, () -> clause.lte((Object) null));
//    }
//
//    @Test
//    void lt_subselectNullNotAllowed() {
//        assertThrows(NullPointerException.class, () -> clause.lt((Function<SelectEngine, SelectTerminal<?>>) null));
//    }
//}
