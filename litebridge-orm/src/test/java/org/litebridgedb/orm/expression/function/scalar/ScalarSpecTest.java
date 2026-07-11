package org.litebridgedb.orm.expression.function.scalar;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ScalarSpecTest {

    @Test
    void testAbsSpec() {
        final ColumnExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final AbsSpec spec = new AbsSpec(target);

        assertEquals(target, spec.target());
        assertEquals(Number.class, spec.returnType());
        assertEquals(target.getColumn(), spec.getColumn());

        final Column newColumn = mock(Column.class);
        spec.setColumn(newColumn);
        assertEquals(newColumn, target.getColumn());
    }

    @Test
    void testLowerSpec() {
        final ColumnExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final LowerSpec spec = new LowerSpec(target);

        assertEquals(target, spec.target());
        assertEquals(String.class, spec.returnType());
        assertEquals(target.getColumn(), spec.getColumn());

        final Column newColumn = mock(Column.class);
        spec.setColumn(newColumn);
        assertEquals(newColumn, target.getColumn());
    }

    @Test
    void testUpperSpec() {
        final ColumnExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final UpperSpec spec = new UpperSpec(target);

        assertEquals(target, spec.target());
        assertEquals(String.class, spec.returnType());
        assertEquals(target.getColumn(), spec.getColumn());

        final Column newColumn = mock(Column.class);
        spec.setColumn(newColumn);
        assertEquals(newColumn, target.getColumn());
    }

    @Test
    void testSubstringSpec() {
        final ColumnExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final SubstringSpec spec = new SubstringSpec(target, 1, 10);

        assertEquals(target, spec.target());
        assertEquals(1, spec.start());
        assertEquals(10, spec.length());
        assertEquals(String.class, spec.returnType());
        assertEquals(target.getColumn(), spec.getColumn());

        final Column newColumn = mock(Column.class);
        spec.setColumn(newColumn);
        assertEquals(newColumn, target.getColumn());

        final SubstringSpec specNoLength = new SubstringSpec(target, 1, null);
        assertEquals(null, specNoLength.length());
    }
}
