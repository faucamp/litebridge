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
    void as() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final Table result = table.as("testAlias");

        // Then
        assertEquals("testAlias", result.alias());

    }

    @Test
    void isTableMetaData() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final boolean result = table.isTableMetaData();

        // Then
        assertFalse(result);
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
    void testToString() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = table.toString();

        // Then
        assertNotNull(result);
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
}