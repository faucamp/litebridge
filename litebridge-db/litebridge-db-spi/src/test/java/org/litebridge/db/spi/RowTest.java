package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RowTest {

    @Test
    void withColumn() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Row row = new Row();

        // When
        row.withColumn(column, "testValue");

        // Then
        assertEquals(1, row.columnStream().count());
        assertTrue(row.columnStream().allMatch(rowColumn -> rowColumn.column().equals(column)
                && rowColumn.value().equals("testValue")));
    }

    @Test
    void column() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row().withColumn(column, "testValue");

        // When
        final Row.RowColumn result = row.column("TEST_COLUMN").orElseThrow();

        // Then
        assertEquals(column, result.column());
    }

    @Test
    void testToString() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row().withColumn(column, "testValue");

        // When
        final String result = row.toString();

        // Then
        assertNotNull(result);
        assertFalse(result.contains("testAlias"));
    }

    @Test
    void testToString_alias() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        column.setAlias("testAlias");
        final Row row = new Row().withColumn(column, "testValue");

        // When
        final String result = row.toString();

        // Then
        assertNotNull(result);
    }

    @Test
    void testToString_aliasSameAsColumn() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        column.setAlias("TEST_COLUMN");
        final Row row = new Row().withColumn(column, "testValue");

        // When
        final String result = row.toString();

        // Then
        assertNotNull(result);
    }

    @Test
    void rowColumn_toString() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row().withColumn(column, "testValue");
        final Row.RowColumn rowColumn = row.column("TEST_COLUMN").orElseThrow();

        // When
        final String result = rowColumn.toString();

        // Then
        assertNotNull(result);
    }

    @Test
    void rowColumn_toString_alias() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        column.setAlias("testAlias");
        final Row row = new Row().withColumn(column, "testValue");
        final Row.RowColumn rowColumn = row.column("TEST_COLUMN").orElseThrow();

        // When
        final String result = rowColumn.toString();

        // Then
        assertNotNull(result);
    }

    @Test
    void rowColumn_equals_true() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row().withColumn(column, "testValue");
        final Row.RowColumn rowColumn1 = row.column("TEST_COLUMN").orElseThrow();
        final Row.RowColumn rowColumn2 = row.column("TEST_COLUMN").orElseThrow();

        // When
        final boolean result = rowColumn1.equals(rowColumn2);

        // Then
        assertTrue(result);
    }

    @Test
    void rowColumn_equals_false() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column1 = new Column(table, "TEST_PK");
        final Column column2 = new Column(table, "TEST_COLUMN");
        final Row row = new Row().withColumn(column1, "testPk").withColumn(column2, "testValue");
        final Row.RowColumn rowColumn1 = row.column("TEST_PK").orElseThrow();
        final Row.RowColumn rowColumn2 = row.column("TEST_COLUMN").orElseThrow();

        // When
        final boolean result = rowColumn1.equals(rowColumn2);

        // Then
        assertFalse(result);
    }

    @Test
    void rowColumn_equals_false_differentType() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row().withColumn(column, "testValue");
        final Row.RowColumn rowColumn1 = row.column("TEST_COLUMN").orElseThrow();
        final Object other = new Object();

        // When
        final boolean result = rowColumn1.equals(other);

        // Then
        assertFalse(result);
    }

    @Test
    void rowColumn_hashCode() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row().withColumn(column, "testValue");
        final Row.RowColumn rowColumn = row.column("TEST_COLUMN").orElseThrow();
        final Object other = new Object();

        // When
        final int result = rowColumn.hashCode();

        // Then
        assertTrue(result != 0);
    }
}