package org.litebridgedb.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FieldColumnSpecTest {

    @Test
    void testConstructorAndAccessors() {
        // Given
        final FieldSpec field = new FieldSpec("field", false);
        final ColumnMapping column = new ColumnSpec("COL");

        // When
        final FieldColumnSpec result = new FieldColumnSpec(field, column);

        // Then
        assertEquals(field, result.field());
        assertEquals(column, result.column());
    }

    @Test
    void testEqualsHashCodeToString() {
        final FieldSpec field = new FieldSpec("field", false);
        final ColumnMapping column = new ColumnSpec("COL");
        final FieldColumnSpec spec1 = new FieldColumnSpec(field, column);
        final FieldColumnSpec spec1b = new FieldColumnSpec(field, column);
        final FieldColumnSpec spec2 = new FieldColumnSpec(new FieldSpec("other", false), column);

        assertEquals(spec1, spec1b);
        assertNotEquals(spec1, spec2);
        assertEquals(spec1.hashCode(), spec1b.hashCode());
        assertNotNull(spec1.toString());
    }
}
