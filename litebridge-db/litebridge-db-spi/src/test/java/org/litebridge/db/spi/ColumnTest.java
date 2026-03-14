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
    void alias() {
        // Given
        final Column column = new Column(table, "testName", "testAlias");

        // When
        final String result = column.alias();

        // Then
        assertEquals("testAlias", result);
    }

    @Test
    void as() {
        // Given
        final Column column = new Column(table, "testName");

        // When
        final Column result = column.as("testAlias");

        // Then
        assertEquals(table, result.table());
        assertEquals("testName", result.name());
        assertEquals("testAlias", result.alias());
    }

    @Test
    void c() {
        // When
        final Column result = Column.c(table, "testName");

        // Then
        assertEquals(table, result.table());
        assertEquals("testName", result.name());
    }

    @Test
    void c_tableName() {
        // When
        final Column result = Column.c("TEST_TABLE", "testName");

        // Then
        assertEquals("", result.table().catalog());
        assertEquals("", result.table().schema());
        assertEquals("TEST_TABLE", result.table().name());
        assertEquals("testName", result.name());
    }

    @Test
    void c_schemaTableNames() {
        // When
        final Column result = Column.c("TEST_SCHEMA", "TEST_TABLE", "testName");

        // Then
        assertEquals("", result.table().catalog());
        assertEquals("TEST_SCHEMA", result.table().schema());
        assertEquals("TEST_TABLE", result.table().name());
        assertEquals("testName", result.name());
    }

    @Test
    void c_catalogSchemaTableNames() {
        // When
        final Column result = Column.c("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "testName");

        // Then
        assertEquals("TEST_CATALOG", result.table().catalog());
        assertEquals("TEST_SCHEMA", result.table().schema());
        assertEquals("TEST_TABLE", result.table().name());
        assertEquals("testName", result.name());
    }

    @Test
    void equals_true() {
        // Given
        final Column column1 = new Column(table, "testName", "testAlias");
        final Column column2 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName", "testAlias");

        // When
        final boolean result = column1.equals(column2);

        // Then
        assertTrue(result);
    }

    @Test
    void equals_false_differentAlias() {
        // Given
        final Column column1 = new Column(table, "testName", "testAlias");
        final Column column2 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName", "otherAlias");

        // When
        final boolean result = column1.equals(column2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_false_differentName() {
        // Given
        final Column column1 = new Column(table, "testName", "testAlias");
        final Column column2 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "otherName", "testAlias");

        // When
        final boolean result = column1.equals(column2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_false_differentTable() {
        // Given
        final Column column1 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName", "testAlias");
        final Column column2 = new Column(new Table("OTHER_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName", "testAlias");

        // When
        final boolean result = column1.equals(column2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_false_differentType() {
        // Given
        final Column column = new Column(table, "testName", "testAlias");
        final Object other = new Object();

        // When
        final boolean result = column.equals(other);

        // Then
        assertFalse(result);
    }

    @Test
    void equalsIgnoreAlias_true() {
        // Given
        final Column column1 = new Column(table, "testName", "testAlias");
        final Column column2 = new Column(new Table("OTHER_CATALOG", "OTHER_SCHEMA", "OTHER_TABLE"), "testName", "otherAlias");

        // When
        final boolean result = column1.equalsIgnoreAlias(column2);

        // Then
        assertTrue(result);
    }

    @Test
    void equalsIgnoreAlias_false_name() {
        // Given
        final Column column1 = new Column(table, "testName", "testAlias");
        final Column column2 = new Column(table, "otherName", "otherAlias");

        // When
        final boolean result = column1.equalsIgnoreAlias(column2);

        // Then
        assertFalse(result);
    }

    @Test
    void equalsIgnoreAlias_false_differentType() {
        // Given
        final Column column = new Column(table, "testName", "testAlias");
        final Aliased other = new Aliased("testName", "testAlias");

        // When
        final boolean result = column.equalsIgnoreAlias(other);

        // Then
        assertFalse(result);
    }

    @Test
    void hashCode_sameWhenEqual() {
        // Given
        final Column column1 = new Column(table, "testName", "testAlias");
        final Column column2 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName", "testAlias");

        // When
        final int result1 = column1.hashCode();
        final int result2 = column2.hashCode();

        // Then
        assertEquals(result1, result2);
    }

    @Test
    void hashCode_differsWhenAliasDiffers() {
        // Given
        final Column column1 = new Column(table, "testName", "testAlias");
        final Column column2 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "testName", "otherAlias");

        // When
        final int result1 = column1.hashCode();
        final int result2 = column2.hashCode();

        // Then
        assertFalse(result1 == result2);
    }
}