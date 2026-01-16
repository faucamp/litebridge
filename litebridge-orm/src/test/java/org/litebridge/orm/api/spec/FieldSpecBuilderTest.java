package org.litebridge.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldSpecBuilderTest {

    @Test
    void c() {
        // Given
        final FieldSpecBuilder<FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder> fieldSpecBuilder = FieldSpecBuilder.f("testField");

        // When
        final FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder result = fieldSpecBuilder.c("testColumn");

        // Then
        assertNotNull(result);
        assertEquals("testColumn", result.name());
    }

    @Test
    void f() {
        // When
        final FieldSpecBuilder<FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder> result = FieldSpecBuilder.f("testField");

        // Then
        assertNotNull(result);
        assertEquals("testField", result.name());
        assertFalse(result.property());
    }

    @Test
    void p() {
        // When
        final FieldSpecBuilder<FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder> result = FieldSpecBuilder.p("testField");

        // Then
        assertNotNull(result);
        assertEquals("testField", result.name());
        assertTrue(result.property());
    }
}