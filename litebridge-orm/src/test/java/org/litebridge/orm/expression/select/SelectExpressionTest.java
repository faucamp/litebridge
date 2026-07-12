package org.litebridge.orm.expression.select;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.tracking.FieldAccessor;

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
        assertEquals(column, spec.getColumn());
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
        assertEquals(column, spec.getColumn());
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
