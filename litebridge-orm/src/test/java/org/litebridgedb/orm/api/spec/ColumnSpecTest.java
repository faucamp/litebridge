package org.litebridgedb.orm.api.spec;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class ColumnSpecTest {

    @Test
    void testCanonicalConstructorAndAccessors() {
        // Given
        final String name = "COL_NAME";
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        final String joinColumn = "JOIN_COL";
        final TableMapping mappedTable = mock(TableMapping.class);

        // When
        final ColumnSpec result = new ColumnSpec(name, generator, joinColumn, mappedTable);

        // Then
        assertEquals(name, result.name());
        assertEquals(generator, result.generator());
        assertEquals(joinColumn, result.joinColumn());
        assertEquals(mappedTable, result.mappedTable());
    }

    @Test
    void testThreeArgConstructor() {
        // Given
        final String name = "COL_NAME";
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        final String joinColumn = "JOIN_COL";

        // When
        final ColumnSpec result = new ColumnSpec(name, generator, joinColumn);

        // Then
        assertEquals(name, result.name());
        assertEquals(generator, result.generator());
        assertEquals(joinColumn, result.joinColumn());
        assertNull(result.mappedTable());
    }

    @Test
    void testTwoArgConstructor() {
        // Given
        final String name = "COL_NAME";
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);

        // When
        final ColumnSpec spec = new ColumnSpec(name, generator);

        // Then
        assertEquals(name, spec.name());
        assertEquals(generator, spec.generator());
        assertNull(spec.joinColumn());
        assertNull(spec.mappedTable());
    }

    @Test
    void testOneArgConstructor() {
        // Given
        final String name = "COL_NAME";

        // When
        final ColumnSpec spec = new ColumnSpec(name);

        // Then
        assertEquals(name, spec.name());
        assertNull(spec.generator());
        assertNull(spec.joinColumn());
        assertNull(spec.mappedTable());
    }

    @Test
    void testEqualsHashCodeToString() {
        final ColumnSpec spec1 = new ColumnSpec("COL1");
        final ColumnSpec spec1b = new ColumnSpec("COL1");
        final ColumnSpec spec2 = new ColumnSpec("COL2");

        assertEquals(spec1, spec1b);
        assertNotEquals(spec1, spec2);
        assertEquals(spec1.hashCode(), spec1b.hashCode());
        assertNotNull(spec1.toString());
    }
}
