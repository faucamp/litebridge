package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableTest {

    @Test
    void catalog() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = table.catalog();

        // Then
        assertEquals("TEST_CATALOG", result);
    }

    @Test
    void schema() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = table.schema();

        // Then
        assertEquals("TEST_SCHEMA", result);
    }

    @Test
    void name() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = table.name();

        // Then
        assertEquals("TEST_TABLE", result);
    }

    @Test
    void qualifiedName() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = table.qualifiedName();

        // Then
        assertEquals("TEST_SCHEMA.TEST_TABLE", result);
    }

    @Test
    void constructor_parsesFullyQualifiedName() {
        // When
        final Table result = new Table("TEST_CATALOG.TEST_SCHEMA.TEST_TABLE", "testAlias");

        // Then
        assertEquals("TEST_CATALOG", result.catalog());
        assertEquals("TEST_SCHEMA", result.schema());
        assertEquals("TEST_TABLE", result.name());
        assertEquals("testAlias", result.alias());
    }

    @Test
    void constructor_parsesSchemaAndTableName() {
        // When
        final Table result = new Table("TEST_SCHEMA.TEST_TABLE", "testAlias");

        // Then
        assertEquals("", result.catalog());
        assertEquals("TEST_SCHEMA", result.schema());
        assertEquals("TEST_TABLE", result.name());
        assertEquals("testAlias", result.alias());
    }

    @Test
    void copyConstructor() {
        // Given
        final Table original = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "testAlias");

        // When
        final Table copy = new Table(original);

        // Then
        assertEquals(original.catalog(), copy.catalog());
        assertEquals(original.schema(), copy.schema());
        assertEquals(original.name(), copy.name());
        assertEquals(original.alias(), copy.alias());
        assertTrue(original.equals(copy));
    }

    @Test
    void as() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final Table result = table.as("testAlias");

        // Then
        assertEquals("testAlias", result.alias());
    }

    @Test
    void testEquals_true() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final boolean result = table1.equals(table2);

        // Then
        assertTrue(result);
    }

    @Test
    void testEquals_true_withMatchingAlias() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "testAlias");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "testAlias");

        // When
        final boolean result = table1.equals(table2);

        // Then
        assertTrue(result);
    }

    @Test
    void testEquals_false_catalog() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Table table2 = new Table("OTHER_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final boolean result = table1.equals(table2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_false_schema() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Table table2 = new Table("TEST_CATALOG", "OTHER_SCHEMA", "TEST_TABLE");

        // When
        final boolean result = table1.equals(table2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_false_table() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "OTHER_TABLE");

        // When
        final boolean result = table1.equals(table2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_false_alias() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "aliasOne");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "aliasTwo");

        // When
        final boolean result = table1.equals(table2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_false_differentType() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Object other = new Object();

        // When
        final boolean result = table1.equals(other);

        // Then
        assertFalse(result);
    }

    @Test
    void equalsIgnoreAlias_true() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "aliasOne");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "aliasTwo");

        // When
        final boolean result = table1.equalsIgnoreAlias(table2);

        // Then
        assertTrue(result);
    }

    @Test
    void equalsIgnoreAlias_false_differentType() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Aliased other = new Aliased("TEST_TABLE", "testAlias");

        // When
        final boolean result = table.equalsIgnoreAlias(other);

        // Then
        assertFalse(result);
    }

    @Test
    void testToString() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "testAlias");

        // When
        final String result = table.toString();

        // Then
        assertEquals(
                "Table[catalog='TEST_CATALOG', schema='TEST_SCHEMA', name='TEST_TABLE', alias='testAlias']",
                result
        );
    }

    @Test
    void testHashCode() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final int result = table.hashCode();

        // Then
        assertTrue(result != 0);
    }

    @Test
    void testHashCode_ignoresAlias() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "aliasOne");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "aliasTwo");

        // When
        final int result1 = table1.hashCode();
        final int result2 = table2.hashCode();

        // Then
        assertEquals(result1, result2);
    }
}