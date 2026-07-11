package org.litebridgedb.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class SelectSpecTest {

    @Test
    void whereConditionStack() {
        // Given
        final SelectSpec spec = new TestSelectSpec(mock(LitebridgeContext.class));

        // When
        final ConditionGroupSpec group1 = spec.currentWhereConditionGroupSpec();
        final ConditionGroupSpec group2 = spec.pushWhereConditionGroup(LogicOperator.AND);
        
        // Then
        assertSame(group2, spec.currentWhereConditionGroupSpec());
        
        // When
        spec.popWhereConditionGroup();
        
        // Then
        assertSame(group1, spec.currentWhereConditionGroupSpec());
    }

    @Test
    void havingConditionStack() {
        // Given
        final SelectSpec spec = new TestSelectSpec(mock(LitebridgeContext.class));

        // When
        final ConditionGroupSpec group1 = spec.currentHavingConditionGroupSpec();
        final ConditionGroupSpec group2 = spec.pushHavingConditionGroup(LogicOperator.OR);

        // Then
        assertSame(group2, spec.currentHavingConditionGroupSpec());

        // When
        spec.popHavingConditionGroup();

        // Then
        assertSame(group1, spec.currentHavingConditionGroupSpec());
    }

    @Test
    void groupBy() {
        // Given
        final SelectSpec spec = new TestSelectSpec(mock(LitebridgeContext.class));
        final SelectColumnSpec selectColumnSpec = new SelectColumnSpec(new Column(new Table("TEST"), "COL"));
        final GroupBySpec groupBySpec = new GroupBySpec(List.of(selectColumnSpec));

        // When
        spec.setGroupBy(groupBySpec);

        // Then
        assertSame(groupBySpec, spec.getGroupBy());
    }

    private static class TestSelectSpec extends SelectSpec {
        public TestSelectSpec(LitebridgeContext litebridgeContext) {
            super(litebridgeContext);
        }
    }
}
