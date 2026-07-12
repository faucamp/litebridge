package org.litebridge.orm.api.spec;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DtoTableSpecTest {

    @Test
    void testCanonicalConstructorAndAccessors() {
        // Given
        final Class<?> dtoClass = Object.class;
        final TableSpec tableSpec = mock(TableSpec.class);
        final List<Class<?>> interfaces = List.of(Runnable.class);

        // When
        final DtoTableSpec result = new DtoTableSpec(dtoClass, tableSpec, interfaces);

        // Then
        assertEquals(dtoClass, result.dtoClass());
        assertEquals(tableSpec, result.tableSpec());
        assertEquals(interfaces, result.dtoInterfaces());
    }

    @Test
    void testTwoArgConstructor() {
        // Given
        final Class<?> dtoClass = Object.class;
        final TableSpec tableSpec = mock(TableSpec.class);

        // When
        final DtoTableSpec result = new DtoTableSpec(dtoClass, tableSpec);

        // Then
        assertEquals(dtoClass, result.dtoClass());
        assertEquals(tableSpec, result.tableSpec());
        assertTrue(result.dtoInterfaces().isEmpty());
    }

    @Test
    void testEqualsHashCodeToString() {
        final TableSpec tableSpec = mock(TableSpec.class);
        final DtoTableSpec spec1 = new DtoTableSpec(Object.class, tableSpec);
        final DtoTableSpec spec1b = new DtoTableSpec(Object.class, tableSpec);
        final DtoTableSpec spec2 = new DtoTableSpec(String.class, tableSpec);

        assertEquals(spec1, spec1b);
        assertNotEquals(spec1, spec2);
        assertEquals(spec1.hashCode(), spec1b.hashCode());
        assertNotNull(spec1.toString());
    }
}
