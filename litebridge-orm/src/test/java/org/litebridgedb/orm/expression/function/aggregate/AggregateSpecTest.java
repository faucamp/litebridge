package org.litebridgedb.orm.expression.function.aggregate;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AggregateSpecTest {

    @Test
    void testAvgSpec() {
        final ColumnExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final AvgSpec<Double> spec = new AvgSpec<>(target, Double.class);

        assertEquals(target, spec.target());
        assertEquals(Double.class, spec.returnType());

        assertEquals(target.getColumn(), spec.getColumn());

        final Column newColumn = mock(Column.class);
        spec.setColumn(newColumn);
        assertEquals(newColumn, target.getColumn());
    }

    @Test
    void testMaxSpec() {
        final ColumnExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final MaxSpec<Integer> spec = new MaxSpec<>(target, Integer.class);

        assertEquals(target, spec.target());
        assertEquals(Integer.class, spec.returnType());

        assertEquals(target.getColumn(), spec.getColumn());

        final Column newColumn = mock(Column.class);
        spec.setColumn(newColumn);
        assertEquals(newColumn, target.getColumn());
    }

    @Test
    void testMinSpec() {
        final ColumnExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final MinSpec<Long> spec = new MinSpec<>(target, Long.class);

        assertEquals(target, spec.target());
        assertEquals(Long.class, spec.returnType());

        assertEquals(target.getColumn(), spec.getColumn());

        final Column newColumn = mock(Column.class);
        spec.setColumn(newColumn);
        assertEquals(newColumn, target.getColumn());
    }

    @Test
    void testCountSpec() {
        final CountSpec spec = new CountSpec();
        assertEquals(Long.class, spec.returnType());
    }
}
