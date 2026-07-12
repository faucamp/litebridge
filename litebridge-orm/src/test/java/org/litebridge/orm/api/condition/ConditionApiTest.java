package org.litebridge.orm.api.condition;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.dto.condition.CbDtoConditionClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.SelectEngine;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import org.litebridge.orm.api.select.impl.SelectorInspector;

class ConditionApiTest {

    @Test
    @SuppressWarnings("unchecked")
    void testBasicOperators() {
        final ConditionSpec spec = new ConditionSpec();
        final ConditionGroupSpec group = mock(ConditionGroupSpec.class);
        final FromClauseEngine engine = mock(FromClauseEngine.class);
        
        final AbstractCbConditionClause<Object> clause = new AbstractCbConditionClause<>(spec, group, engine) {
            @Override
            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal() {
                return mock(CbDtoConditionClauseTerminal.class);
            }
        };

        clause.eq("val");
        assertEquals(Operator.EQ, spec.getOperator());
        assertEquals("val", spec.getValue());

        clause.neq("val");
        assertEquals(Operator.NEQ, spec.getOperator());

        clause.lt(10);
        assertEquals(Operator.LT, spec.getOperator());

        clause.lte(10);
        assertEquals(Operator.LTE, spec.getOperator());

        clause.gt(10);
        assertEquals(Operator.GT, spec.getOperator());

        clause.gte(10);
        assertEquals(Operator.GTE, spec.getOperator());

        clause.like("%val%");
        assertEquals(Operator.LIKE, spec.getOperator());

        clause.isNull();
        assertEquals(Operator.IS_NULL, spec.getOperator());

        clause.isNotNull();
        assertEquals(Operator.IS_NOT_NULL, spec.getOperator());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInOperators() {
        final ConditionSpec spec = new ConditionSpec();
        final AbstractCbConditionClause<Object> clause = new AbstractCbConditionClause<>(spec, null, null) {
            @Override
            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal() {
                return mock(CbDtoConditionClauseTerminal.class);
            }
        };

        clause.in(1, 2, 3);
        assertEquals(Operator.IN, spec.getOperator());
        assertEquals(List.of(1, 2, 3), spec.getValue());

        clause.in(List.of(4, 5));
        assertEquals(List.of(4, 5), spec.getValue());

        clause.notIn(1, 2);
        assertEquals(Operator.NOT_IN, spec.getOperator());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNullHandling() {
        final ConditionSpec spec = new ConditionSpec();
        final AbstractCbConditionClause<Object> clause = new AbstractCbConditionClause<>(spec, null, null) {
            @Override
            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal() {
                return mock(CbDtoConditionClauseTerminal.class);
            }
        };

        clause.eq(null);
        assertEquals(Operator.IS_NULL, spec.getOperator());

        clause.neq(null);
        assertEquals(Operator.IS_NOT_NULL, spec.getOperator());

        assertThrows(IllegalArgumentException.class, () -> clause.gt((Object) null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSubselect() {
        final ConditionSpec spec = new ConditionSpec();
        final FromClauseEngine engine = mock(FromClauseEngine.class);
        final AbstractCbConditionClause<Object> clause = new AbstractCbConditionClause<>(spec, null, engine) {
            @Override
            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal() {
                return mock(CbDtoConditionClauseTerminal.class);
            }
        };

        final SelectSpec selectSpec = mock(SelectSpec.class);
        final SelectTerminal<?> terminal = mock(SelectTerminal.class);
        final Function<SelectEngine, SelectTerminal<?>> subselect = e -> terminal;

        try (MockedStatic<SelectorInspector> inspector = mockStatic(SelectorInspector.class)) {
            inspector.when(() -> SelectorInspector.getSelectSpec(terminal)).thenReturn(selectSpec);

            clause.eq(subselect);
            assertEquals(Operator.EQ, spec.getOperator());
            assertEquals(selectSpec, spec.getValue());

            clause.neq(subselect);
            assertEquals(Operator.NEQ, spec.getOperator());

            clause.lt(subselect);
            assertEquals(Operator.LT, spec.getOperator());

            clause.lte(subselect);
            assertEquals(Operator.LTE, spec.getOperator());

            clause.gt(subselect);
            assertEquals(Operator.GT, spec.getOperator());

            clause.gte(subselect);
            assertEquals(Operator.GTE, spec.getOperator());

            clause.in(subselect);
            assertEquals(Operator.IN, spec.getOperator());

            clause.notIn(subselect);
            assertEquals(Operator.NOT_IN, spec.getOperator());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSubselectNull() {
        final AbstractCbConditionClause<Object> clause = new AbstractCbConditionClause<>(new ConditionSpec(), null, null) {
            @Override
            protected AbstractCbConditionClauseTerminal<Object> createCbConditionClauseTerminal() {
                return mock(CbDtoConditionClauseTerminal.class);
            }
        };

        // eq/neq allow null subselect (though it just sets value to null)
        clause.eq((Function<SelectEngine, SelectTerminal<?>>) null);
        clause.neq((Function<SelectEngine, SelectTerminal<?>>) null);

        // Others should throw NPE
        assertThrows(NullPointerException.class, () -> clause.lt((Function<SelectEngine, SelectTerminal<?>>) null));
        assertThrows(NullPointerException.class, () -> clause.lte((Function<SelectEngine, SelectTerminal<?>>) null));
        assertThrows(NullPointerException.class, () -> clause.gt((Function<SelectEngine, SelectTerminal<?>>) null));
        assertThrows(NullPointerException.class, () -> clause.gte((Function<SelectEngine, SelectTerminal<?>>) null));
        assertThrows(NullPointerException.class, () -> clause.in((Function<SelectEngine, SelectTerminal<?>>) null));
        assertThrows(NullPointerException.class, () -> clause.notIn((Function<SelectEngine, SelectTerminal<?>>) null));
    }
}
