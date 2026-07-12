package org.litebridge.orm.api.select.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.dto.condition.CbDtoConditionClause;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.persistence.OrmTable;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractCbConditionClauseTest {

    private ConditionSpec conditionSpec;
    private ConditionGroupSpec conditionGroupSpec;
    private FromClauseEngine fromClauseEngine;
    private CbDtoConditionClause<Object> clause;

    @BeforeEach
    void setUp() {
        conditionSpec = new ConditionSpec();
        conditionGroupSpec = new ConditionGroupSpec();
        fromClauseEngine = mock(FromClauseEngine.class);
        OrmTable ormTable = mock(OrmTable.class);
        clause = new CbDtoConditionClause<>(conditionSpec, conditionGroupSpec, ormTable, fromClauseEngine);
    }

    @Test
    void eq() {
        // When
        clause.eq("value");

        // Then
        assertEquals(Operator.EQ, conditionSpec.getOperator());
        assertEquals("value", conditionSpec.getValue());
    }

    @Test
    void eq_null() {
        // When
        clause.eq(null);

        // Then
        assertEquals(Operator.IS_NULL, conditionSpec.getOperator());
    }

    @Test
    void eq_subselect() {
        // Given
        final Function<SelectEngine, SelectTerminal<?>> subselect = mock(Function.class);
        final AbstractSelector delegate = mock(AbstractSelector.class);
        final DelegatingSelector terminal = new DelegatingSelector(delegate);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(subselect.apply(any())).thenReturn(terminal);

        // When
        clause.eq(subselect);

        // Then
        assertEquals(Operator.EQ, conditionSpec.getOperator());
        assertEquals(selectSpec, conditionSpec.getValue());
    }

    @Test
    void eq_subselectNull() {
        // When
        clause.eq((Function<SelectEngine, SelectTerminal<?>>) null);

        // Then
        assertEquals(Operator.IS_NULL, conditionSpec.getOperator());
    }

    @Test
    void neq() {
        // When
        clause.neq("value");

        // Then
        assertEquals(Operator.NEQ, conditionSpec.getOperator());
    }

    @Test
    void neq_null() {
        // When
        clause.neq(null);

        // Then
        assertEquals(Operator.IS_NOT_NULL, conditionSpec.getOperator());
    }

    @Test
    void neq_subselect() {
        // Given
        final Function<SelectEngine, SelectTerminal<?>> subselect = mock(Function.class);
        final AbstractSelector delegate = mock(AbstractSelector.class);
        final DelegatingSelector terminal = new DelegatingSelector(delegate);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(subselect.apply(any())).thenReturn(terminal);

        // When
        clause.neq(subselect);

        // Then
        assertEquals(Operator.NEQ, conditionSpec.getOperator());
    }

    @Test
    void lt() {
        // When
        clause.lt("value");

        // Then
        assertEquals(Operator.LT, conditionSpec.getOperator());
    }

    @Test
    void lt_Subselect() {
        // Given
        final Function<SelectEngine, SelectTerminal<?>> subselect = mock(Function.class);
        final AbstractSelector delegate = mock(AbstractSelector.class);
        final DelegatingSelector terminal = new DelegatingSelector(delegate);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(subselect.apply(any())).thenReturn(terminal);

        // When
        clause.lt(subselect);

        // Then
        assertEquals(Operator.LT, conditionSpec.getOperator());
    }

    @Test
    void lte() {
        // When
        clause.lte("value");

        // Then
        assertEquals(Operator.LTE, conditionSpec.getOperator());
    }

    @Test
    void lte_subselect() {
        // Given
        final Function<SelectEngine, SelectTerminal<?>> subselect = mock(Function.class);
        final AbstractSelector delegate = mock(AbstractSelector.class);
        final DelegatingSelector terminal = new DelegatingSelector(delegate);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(subselect.apply(any())).thenReturn(terminal);

        // When
        clause.lte(subselect);

        // Then
        assertEquals(Operator.LTE, conditionSpec.getOperator());
    }

    @Test
    void gt() {
        // When
        clause.gt("value");

        // Then
        assertEquals(Operator.GT, conditionSpec.getOperator());
    }

    @Test
    void gt_Subselect() {
        // Given
        final Function<SelectEngine, SelectTerminal<?>> subselect = mock(Function.class);
        final AbstractSelector delegate = mock(AbstractSelector.class);
        final DelegatingSelector terminal = new DelegatingSelector(delegate);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(subselect.apply(any())).thenReturn(terminal);

        // When
        clause.gt(subselect);

        // Then
        assertEquals(Operator.GT, conditionSpec.getOperator());
    }

    @Test
    void gte() {
        // When
        clause.gte("value");

        // Then
        assertEquals(Operator.GTE, conditionSpec.getOperator());
    }

    @Test
    void gte_subselect() {
        // Given
        final Function<SelectEngine, SelectTerminal<?>> subselect = mock(Function.class);
        final AbstractSelector delegate = mock(AbstractSelector.class);
        final DelegatingSelector terminal = new DelegatingSelector(delegate);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(subselect.apply(any())).thenReturn(terminal);

        // When
        clause.gte(subselect);

        // Then
        assertEquals(Operator.GTE, conditionSpec.getOperator());
    }

    @Test
    void like() {
        // When
        clause.like("%val%");

        // Then
        assertEquals(Operator.LIKE, conditionSpec.getOperator());
    }

    @Test
    void in_varargs() {
        // When
        clause.in("a", "b");

        // Then
        assertEquals(Operator.IN, conditionSpec.getOperator());
        assertTrue(conditionSpec.getValue() instanceof Collection);
    }

    @Test
    void in_singleCollection() {
        // When
        clause.in(Arrays.asList("a", "b"), new Object[0]);

        // Then
        assertEquals(Operator.IN, conditionSpec.getOperator());
    }

    @Test
    void in_list() {
        // When
        clause.in(Arrays.asList("a", "b"));

        // Then
        assertEquals(Operator.IN, conditionSpec.getOperator());
    }

    @Test
    void in_subselect() {
        // Given
        final Function<SelectEngine, SelectTerminal<?>> subselect = mock(Function.class);
        final AbstractSelector delegate = mock(AbstractSelector.class);
        final DelegatingSelector terminal = new DelegatingSelector(delegate);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(subselect.apply(any())).thenReturn(terminal);

        // When
        clause.in(subselect);

        // Then
        assertEquals(Operator.IN, conditionSpec.getOperator());
    }

    @Test
    void notIn_varargs() {
        // When
        clause.notIn("a", "b");

        // Then
        assertEquals(Operator.NOT_IN, conditionSpec.getOperator());
    }

    @Test
    void notIn_singleCollection() {
        // When
        clause.notIn(Arrays.asList("a", "b"), new Object[0]);

        // Then
        assertEquals(Operator.NOT_IN, conditionSpec.getOperator());
    }

    @Test
    void notIn_list() {
        // When
        clause.notIn(Arrays.asList("a", "b"));

        // Then
        assertEquals(Operator.NOT_IN, conditionSpec.getOperator());
    }

    @Test
    void notIn_subselect() {
        // Given
        final Function<SelectEngine, SelectTerminal<?>> subselect = mock(Function.class);
        final AbstractSelector delegate = mock(AbstractSelector.class);
        final DelegatingSelector terminal = new DelegatingSelector(delegate);
        final SelectSpec selectSpec = mock(SelectSpec.class);
        when(delegate.selectSpec()).thenReturn(selectSpec);
        when(subselect.apply(any())).thenReturn(terminal);

        // When
        clause.notIn(subselect);

        // Then
        assertEquals(Operator.NOT_IN, conditionSpec.getOperator());
    }

    @Test
    void isNull() {
        // When
        clause.isNull();

        // Then
        assertEquals(Operator.IS_NULL, conditionSpec.getOperator());
    }

    @Test
    void isNotNull() {
        // When
        clause.isNotNull();

        // Then
        assertEquals(Operator.IS_NOT_NULL, conditionSpec.getOperator());
    }

    @Test
    void lte_invalidNullOperator() {
        assertThrows(IllegalArgumentException.class, () -> clause.lte((Object) null));
    }

    @Test
    void lt_subselectNullNotAllowed() {
        assertThrows(NullPointerException.class, () -> clause.lt((Function<SelectEngine, SelectTerminal<?>>) null));
    }
}
