package org.litebridge.orm.api.select.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.ConditionClause;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.api.sql.SqlSelector;
import org.litebridge.orm.api.sql.SqlWhereConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class ConditionClauseImplTest {

    private ConditionSpec conditionSpec;
    private TestConditionClauseTerminal terminal;
    private ConditionClauseImpl<Object, TestConditionClause, TestConditionClauseTerminal> clause;

    @BeforeEach
    void setUp() {
        conditionSpec = new ConditionSpec();
        clause = new ConditionClauseImpl<>(conditionSpec, mock(LitebridgeContext.class), LogicOperator.NOOP, new org.litebridge.orm.expression.select.SelectColumnSpec(mock(org.litebridge.db.spi.Column.class)), null, n -> mock(TestConditionClauseTerminal.class));
    }

    @Test
    void eq() {
        clause.eq("column");
        assertEquals(Operator.EQ, conditionSpec.getOperator());
        assertEquals("column", conditionSpec.getValue());
    }

    @Test
    void eq_null() {
        clause.eq((Object) null);
        assertEquals(Operator.IS_NULL, conditionSpec.getOperator());
        assertEquals(null, conditionSpec.getValue());
    }

    @Test
    void neq() {
        clause.neq("column");
        assertEquals(Operator.NEQ, conditionSpec.getOperator());
        assertEquals("column", conditionSpec.getValue());
    }

    @Test
    void neq_null() {
        clause.neq(null);
        assertEquals(Operator.IS_NOT_NULL, conditionSpec.getOperator());
        assertEquals(null, conditionSpec.getValue());
    }

    @Test
    void lt() {
        clause.lt(10);
        assertEquals(Operator.LT, conditionSpec.getOperator());
        assertEquals(10, conditionSpec.getValue());
    }

    @Test
    void lte() {
        clause.lte(10);
        assertEquals(Operator.LTE, conditionSpec.getOperator());
        assertEquals(10, conditionSpec.getValue());
    }

    @Test
    void gt() {
        clause.gt(10);
        assertEquals(Operator.GT, conditionSpec.getOperator());
        assertEquals(10, conditionSpec.getValue());
    }

    @Test
    void gte() {
        clause.gte(10);
        assertEquals(Operator.GTE, conditionSpec.getOperator());
        assertEquals(10, conditionSpec.getValue());
    }

    @Test
    void isNull() {
        clause.isNull();
        assertEquals(Operator.IS_NULL, conditionSpec.getOperator());
        assertEquals(null, conditionSpec.getValue());
    }

    @Test
    void isNotNull() {
        clause.isNotNull();
        assertEquals(Operator.IS_NOT_NULL, conditionSpec.getOperator());
        assertEquals(null, conditionSpec.getValue());
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

    @Test
    void subselect_unsupported_terminal() {
        assertThrows(IllegalArgumentException.class, () -> clause.eq(subselect -> mock(SelectTerminal.class)));
    }

    private void assertSubselectCondition(final SubselectConditionInvoker invoker, final Operator expectedOperator) {
        final SqlWhereConditionClauseTerminal terminal = new SqlWhereConditionClauseTerminal(new SqlSelector(
                mock(TransactionalDatabaseProvider.class),
                mock(TableRegistry.class),
                mock(LitebridgeContext.class),
                null));

        invoker.apply(subselect -> terminal);

        assertEquals(expectedOperator, conditionSpec.getOperator());
        assertInstanceOf(SelectSpec.class, conditionSpec.getValue());
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
