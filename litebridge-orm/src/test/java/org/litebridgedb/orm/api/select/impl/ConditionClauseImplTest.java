package org.litebridgedb.orm.api.select.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.ConditionClauseTerminal;
import org.litebridgedb.orm.api.select.SelectTerminal;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.api.sql.SqlSelector;
import org.litebridgedb.orm.api.sql.SqlWhereConditionClauseTerminal;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.TableRegistry;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ConditionClauseImplTest {

    private ConditionSpec conditionSpec;
    private TestConditionClauseTerminal terminal;
    private ConditionClauseImpl<Object, TestConditionClause, TestConditionClauseTerminal> clause;

    @BeforeEach
    void setUp() {
        conditionSpec = new ConditionSpec();
        terminal = mock(TestConditionClauseTerminal.class);
        clause = new ConditionClauseImpl<>(conditionSpec, terminal, mock(LitebridgeContext.class));
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
                mock(LitebridgeContext.class)));

        invoker.apply(subselect -> terminal);

        assertEquals(expectedOperator, conditionSpec.getOperator());
        assertInstanceOf(SelectSpec.class, conditionSpec.getValue());
    }

    @FunctionalInterface
    private interface SubselectConditionInvoker {
        void apply(Function<Subselect, SelectTerminal<?>> subselect);
    }

    private interface TestConditionClause extends org.litebridgedb.orm.api.select.ConditionClause<Object, TestConditionClause, TestConditionClauseTerminal> {
    }

    private interface TestConditionClauseTerminal extends ConditionClauseTerminal<Object, TestConditionClause, TestConditionClauseTerminal> {
    }

}
