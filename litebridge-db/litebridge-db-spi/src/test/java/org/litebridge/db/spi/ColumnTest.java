package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnTest {

    private final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

    @Test
    void table() {
        // Given
        final Column column = new Column(table, "TEST_COLUMN");

        // When
        final Table result = column.table();

        // Then
        assertEquals(table, result);
    }

    @Test
    void name() {
        // Given
        final Column column = new Column(table, "testName");

        // When
        final String result = column.name();

        // Then
        assertEquals("testName", result);
    }

    @Test
    void equals_sameInstance() {
        // Given
        final Column column = new Column(table, "testName");

        // When/Then
        assertTrue(column.equals(column));
    }

    @Test
    void equals_true() {
        // Given
        final Column column1 = new Column(table, "testName");
        final Column column2 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName");

        // When
        final boolean result = column1.equals(column2);

        // Then
        assertTrue(result);
    }

    @Test
    void equals_false_differentName() {
        // Given
        final Column column1 = new Column(table, "testName");
        final Column column2 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "otherName");

        // When
        final boolean result = column1.equals(column2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_false_differentTable() {
        // Given
        final Column column1 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName");
        final Column column2 = new Column(new Table("OTHER_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName");

        // When
        final boolean result = column1.equals(column2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_false_differentType() {
        // Given
        final Column column = new Column(table, "testName");
        final Object other = new Object();

        // When
        final boolean result = column.equals(other);

        // Then
        assertFalse(result);
    }

    @Test
    void testToString() {
        // Given
        final Column column = new Column(table, "testName");

        // When
        final String result = column.toString();

        // Then
        assertTrue(result.contains("Column"));
        assertTrue(result.contains("testName"));
    }

    @Test
    void hashCode_sameWhenEqual() {
        // Given
        final Column column1 = new Column(table, "testName");
        final Column column2 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName");

        // When
        final int result1 = column1.hashCode();
        final int result2 = column2.hashCode();

        // Then
        assertEquals(result1, result2);
    }
}