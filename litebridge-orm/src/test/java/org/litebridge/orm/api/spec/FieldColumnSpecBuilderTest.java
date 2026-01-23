package org.litebridge.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldColumnSpecBuilderTest {

    @Test
    void column() {
        // Given
        final FieldColumnSpecBuilder fieldColumnSpecBuilder = new FieldColumnSpecBuilder(new FieldSpec("testField", false));

        // When
        final FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder result = fieldColumnSpecBuilder.column("testColumn");

        // Then
        assertNotNull(result);

        assertNotNull(result.field());
        assertEquals("testField", result.field().name());

        assertNotNull(result.column());
        assertInstanceOf(ColumnSpec.class, result.column());
        final ColumnSpec columnSpec = (ColumnSpec) result.column();
        assertEquals("testColumn", columnSpec.name());
    }

    @Test
    void column_notSet() {
        // Given
        final FieldColumnSpecBuilder fieldColumnSpecBuilder = new FieldColumnSpecBuilder(new FieldSpec("testField", false));

        // When/Then
        assertThrows(IllegalStateException.class, () -> fieldColumnSpecBuilder.build().column());
    }

    @Test
    void field() {
        // Given
        final FieldColumnSpecBuilder fieldColumnSpecBuilder = new FieldColumnSpecBuilder(new FieldSpec("testField", false));

        // When
        final FieldSpec result = fieldColumnSpecBuilder.c("testColumn").field();

        // Then
        assertNotNull(result);
        assertFalse(result.property());
        assertEquals("testField", result.name());
    }
}