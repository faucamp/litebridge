package org.litebridgedb.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldMappingTest {

    @Test
    void f_returnsFieldSpecConfiguredForFieldAccess() {
        // When
        final FieldSpec result = FieldMapping.f("name");

        // Then
        assertEquals("name", result.name());
        assertFalse(result.property());
    }

    @Test
    void field_returnsFieldSpecConfiguredForFieldAccess() {
        // When
        final FieldSpec result = FieldMapping.field("age");

        // Then
        assertEquals("age", result.name());
        assertFalse(result.property());
    }

    @Test
    void p_returnsFieldSpecConfiguredForPropertyAccess() {
        // When
        final FieldSpec result = FieldMapping.p("email");

        // Then
        assertEquals("email", result.name());
        assertTrue(result.property());
    }

    @Test
    void property_returnsFieldSpecConfiguredForPropertyAccess() {
        // When
        final FieldSpec result = FieldMapping.property("phoneNumber");

        // Then
        assertEquals("phoneNumber", result.name());
        assertTrue(result.property());
    }
}