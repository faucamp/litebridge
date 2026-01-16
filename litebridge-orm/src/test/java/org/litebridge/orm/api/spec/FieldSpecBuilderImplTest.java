package org.litebridge.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldSpecBuilderImplTest {

    @Test
    void property_true() {
        // Given
        final FieldSpecBuilderImpl fieldSpecBuilder = new FieldSpecBuilderImpl("testField")
                .property(true);

        // When
        final boolean result = fieldSpecBuilder.property();

        // Then
        assertTrue(result);
    }

    @Test
    void property_false() {
        // Given
        final FieldSpecBuilderImpl fieldSpecBuilder = new FieldSpecBuilderImpl("testField")
                .property(false);

        // When
        final boolean result = fieldSpecBuilder.property();

        // Then
        assertFalse(result);
    }

    @Test
    void property_false_null() {
        // Given
        final FieldSpecBuilderImpl fieldSpecBuilder = new FieldSpecBuilderImpl("testField");

        // When
        final boolean result = fieldSpecBuilder.property();

        // Then
        assertFalse(result);
    }

    @Test
    void column() {
        // Given
        final FieldSpecBuilderImpl fieldSpecBuilder = new FieldSpecBuilderImpl("testField");

        // When
        final FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder result = fieldSpecBuilder.column("testColumn");

        // Then
        assertNotNull(result);
        assertEquals("testColumn", result.name());
    }
}