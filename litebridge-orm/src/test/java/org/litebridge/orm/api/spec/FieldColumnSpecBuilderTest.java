package org.litebridge.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldColumnSpecBuilderTest {

    @Test
    void column() {
        // Given
        final FieldColumnSpecBuilder fieldColumnSpecBuilder = new FieldColumnSpecBuilder("testField");

        // When
        final FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder result = fieldColumnSpecBuilder.column("testColumn");

        // Then
        assertNotNull(result);
        assertNotNull(result.column());
        assertEquals("testColumn", result.name());
        assertNotNull(result.field());
        assertEquals("testField", result.field().name());
    }

    @Test
    void column_notSet() {
        // Given
        final FieldColumnSpecBuilder fieldColumnSpecBuilder = new FieldColumnSpecBuilder("testField");

        // When/Then
        assertThrows(IllegalStateException.class, fieldColumnSpecBuilder::column);
    }

    @Test
    void field() {
        // Given
        final FieldColumnSpecBuilder fieldColumnSpecBuilder = new FieldColumnSpecBuilder("testField");

        // When
        final FieldSpec result = fieldColumnSpecBuilder.field();

        // Then
        assertNotNull(result);
        assertFalse(result.property());
        assertEquals("testField", result.name());
    }
}