package org.litebridge.orm.api.select.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.sql.SqlSelector;
import org.litebridge.orm.api.sql.SqlWhereConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConditionClauseImplTest {

    private ConditionClauseImpl<Object, TestConditionClause, TestConditionClauseTerminal> clause;
    private QueryNode[] capturedNode = new QueryNode[1];

    @BeforeEach
    void setUp() {
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final org.litebridge.orm.engine.FromClauseEngine fromClauseEngine = mock(org.litebridge.orm.engine.FromClauseEngine.class);
        when(context.fromClauseEngine()).thenReturn(fromClauseEngine);

        clause = new ConditionClauseImpl<>(context, LogicOperator.NOOP, new SelectColumnSpec(mock(org.litebridge.db.spi.Column.class)), null, n -> {
            capturedNode[0] = n;
            return mock(TestConditionClauseTerminal.class);
        });
    }

    @Test
    void eq() {
        clause.eq("column");
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.EQ, node.operator());
        assertEquals("column", node.rhs());
    }

    @Test
    void eq_null() {
        clause.eq((Object) null);
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.IS_NULL, node.operator());
        assertNull(node.rhs());
    }

    @Test
    void neq() {
        clause.neq("column");
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.NEQ, node.operator());
        assertEquals("column", node.rhs());
    }

    @Test
    void neq_null() {
        clause.neq(null);
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.IS_NOT_NULL, node.operator());
        assertNull(node.rhs());
    }

    @Test
    void lt() {
        clause.lt(10);
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.LT, node.operator());
        assertEquals(10, node.rhs());
    }

    @Test
    void lte() {
        clause.lte(10);
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.LTE, node.operator());
        assertEquals(10, node.rhs());
    }

    @Test
    void gt() {
        clause.gt(10);
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.GT, node.operator());
        assertEquals(10, node.rhs());
    }

    @Test
    void gte() {
        clause.gte(10);
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.GTE, node.operator());
        assertEquals(10, node.rhs());
    }

    @Test
    void isNull() {
        clause.isNull();
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.IS_NULL, node.operator());
        assertNull(node.rhs());
    }

    @Test
    void isNotNull() {
        clause.isNotNull();
        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(Operator.IS_NOT_NULL, node.operator());
        assertNull(node.rhs());
    }

    @Test
    void lt__null_unsupported() {
        assertThrows(NullPointerException.class, () -> clause.lt(null));
    }

    @Test
    void eq_subselect() {
        assertSubselectCondition(subselect -> clause.eq(subselect), Operator.EQ);
    }

    @Test
    void neq_subselect() {
        assertSubselectCondition(subselect -> clause.neq(subselect), Operator.NEQ);
    }

    @Test
    void lt_subselect() {
        assertSubselectCondition(subselect -> clause.lt(subselect), Operator.LT);
    }

    @Test
    void lte_subselect() {
        assertSubselectCondition(subselect -> clause.lte(subselect), Operator.LTE);
    }

    @Test
    void gt_subselect() {
        assertSubselectCondition(subselect -> clause.gt(subselect), Operator.GT);
    }

    @Test
    void gte_subselect() {
        assertSubselectCondition(subselect -> clause.gte(subselect), Operator.GTE);
    }

    private void assertSubselectCondition(final SubselectConditionInvoker invoker, final Operator expectedOperator) {
        final SqlSelector selector = mock(SqlSelector.class);
        final org.litebridge.orm.api.sql.SqlSelectSpec spec = mock(org.litebridge.orm.api.sql.SqlSelectSpec.class);
        when(selector.compile()).thenReturn(spec);

        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal(selector);

        invoker.apply(subselect -> terminal);

        ConditionNode node = (ConditionNode) capturedNode[0];
        assertEquals(expectedOperator, node.operator());
        assertInstanceOf(SelectTerminal.class, node.rhs());
    }

    @FunctionalInterface
    private interface SubselectConditionInvoker {
        void apply(Function<SelectEngine, SelectTerminal<?>> subselect);
    }

    private interface TestConditionClause extends org.litebridge.orm.api.select.ConditionClause<Object, TestConditionClause, TestConditionClauseTerminal> {
    }

    private interface TestConditionClauseTerminal extends ConditionClauseTerminal<Object, TestConditionClause, TestConditionClauseTerminal> {
    }

}
