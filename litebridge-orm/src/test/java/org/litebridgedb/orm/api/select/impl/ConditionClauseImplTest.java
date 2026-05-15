package org.litebridgedb.orm.api.select.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.ConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        clause = new ConditionClauseImpl<>(conditionSpec, terminal);
    }

    @Test
    void eq() {
        clause.eq("value");
        assertEquals(Operator.EQ, conditionSpec.getOperator());
        assertEquals("value", conditionSpec.getValue());
    }

    @Test
    void eq_null() {
        clause.eq(null);
        assertEquals(Operator.IS_NULL, conditionSpec.getOperator());
        assertEquals(null, conditionSpec.getValue());
    }

    @Test
    void neq() {
        clause.neq("value");
        assertEquals(Operator.NEQ, conditionSpec.getOperator());
        assertEquals("value", conditionSpec.getValue());
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
    void condition_null_unsupported() {
        assertThrows(IllegalArgumentException.class, () -> clause.lt(null));
    }

    private interface TestConditionClause extends org.litebridgedb.orm.api.select.ConditionClause<Object, TestConditionClause, TestConditionClauseTerminal> {}
    private interface TestConditionClauseTerminal extends ConditionClauseTerminal<Object, TestConditionClause, TestConditionClauseTerminal> {}
}
