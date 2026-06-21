package org.litebridgedb.orm.expression.select;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.tracking.FieldAccessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SelectExpressionTest {

    @Test
    void testSelectColumnSpec() {
        // Given
        final Column column = mock(Column.class);

        // When
        final SelectColumnSpec spec = new SelectColumnSpec(column);

        // Then
        assertEquals(column, spec.column());
    }

    @Test
    void testSelectFieldSpec() {
        // Given
        final FieldAccessor field = mock(FieldAccessor.class);
        final Column column = mock(Column.class);

        // When
        final SelectFieldSpec spec = new SelectFieldSpec(field, column);

        // Then
        assertEquals(field, spec.field());
        assertEquals(column, spec.column());
    }

    @Test
    void testSubselectSpec() {
        // Given
        final SelectSpec selectSpec = mock(SelectSpec.class);

        // When
        final SubselectSpec spec = new SubselectSpec(selectSpec);

        // Then
        assertEquals(selectSpec, spec.selectSpec());
    }
}
