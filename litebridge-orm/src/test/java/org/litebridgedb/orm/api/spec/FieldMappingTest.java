package org.litebridgedb.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void testFieldSpecCanonicalConstructor() {
        // Given
        Class<?> dtoClass = Object.class;
        String name = "field";
        boolean property = true;

        // When
        FieldSpec spec = new FieldSpec(dtoClass, name, property);

        // Then
        assertEquals(dtoClass, spec.dtoClass());
        assertEquals(name, spec.name());
        assertTrue(spec.property());
    }

    @Test
    void testFieldSpecEqualsHashCodeToString() {
        FieldSpec spec1 = new FieldSpec(Object.class, "field", false);
        FieldSpec spec1b = new FieldSpec(Object.class, "field", false);
        FieldSpec spec2 = new FieldSpec(String.class, "field", false);

        assertEquals(spec1, spec1b);
        assertNotEquals(spec1, spec2);
        assertEquals(spec1.hashCode(), spec1b.hashCode());
        assertNotNull(spec1.toString());
    }
}