package org.litebridge.orm.api.spec;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class TableMappingTest {

    @Test
    void testCanonicalConstructorAndAccessors() {
        // Given
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final Class<?> dtoClass = Object.class;
        final TableSpec tableSpec = mock(TableSpec.class);

        // When
        final TableMapping result = new TableMapping(lookup, dtoClass, tableSpec);

        // Then
        assertEquals(lookup, result.lookup());
        assertEquals(dtoClass, result.dtoClass());
        assertEquals(tableSpec, result.tableSpec());
    }

    @Test
    void testTwoArgConstructor() {
        // Given
        final Class<?> dtoClass = Object.class;
        final TableSpec tableSpec = mock(TableSpec.class);

        // When
        final TableMapping result = new TableMapping(dtoClass, tableSpec);

        // Then
        assertEquals(MethodHandles.publicLookup(), result.lookup());
        assertEquals(dtoClass, result.dtoClass());
        assertEquals(tableSpec, result.tableSpec());
    }

    @Test
    void testEqualsHashCodeToString() {
        final TableSpec tableSpec = mock(TableSpec.class);
        final TableMapping mapping1 = new TableMapping(Object.class, tableSpec);
        final TableMapping mapping1b = new TableMapping(Object.class, tableSpec);
        final TableMapping mapping2 = new TableMapping(String.class, tableSpec);

        assertEquals(mapping1, mapping1b);
        assertNotEquals(mapping1, mapping2);
        assertEquals(mapping1.hashCode(), mapping1b.hashCode());
        assertNotNull(mapping1.toString());
    }
}
