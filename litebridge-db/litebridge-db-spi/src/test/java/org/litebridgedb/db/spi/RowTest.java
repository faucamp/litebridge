package org.litebridgedb.db.spi;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void column_spiColumn() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row().withColumn(column, "testValue");
        final Column queryColumn = new Column(column.table(), "TEST_COLUMN");

        // When
        final Row.RowColumn result = row.column(queryColumn).orElseThrow();

        // Then
        assertEquals(column, result.column());
    }

    @Test
    void column_spiColumn_aliased() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row().withColumn(column.as("c1"), "testValue");
        final Column queryColumn = new Column(column.table(), "TEST_COLUMN").as("c1");

        // When
        final Row.RowColumn result = row.column(queryColumn).orElseThrow();

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
        assertEquals("{TEST_COLUMN=testValue}", result);
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
        assertEquals("TEST_COLUMN=testValue", result);
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

    @Test
    void columnForAlias_exists() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        column.setAlias("testAlias");
        final Row row = new Row().withColumn(column, "value");

        // When
        final Row.RowColumn result = row.columnForAlias("testAlias").orElseThrow();

        // Then
        assertEquals(column, result.column());
    }

    @Test
    void columnForAlias_doesNotExist() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        column.setAlias("testAlias");
        final Row row = new Row().withColumn(column, "value");

        // When
        final Optional<Row.RowColumn> result = row.columnForAlias("nonExistentAlias");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void columnForAlias_nullAlias() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row().withColumn(column, "value");

        // When/Then
        assertThrows(NullPointerException.class, () -> row.columnForAlias(null));
    }

    @Test
    void testSize_emptyRow() {
        // Given
        final Row row = new Row();

        // When
        final int result = row.size();

        // Then
        assertEquals(0, result);
    }

    @Test
    void testSize_nonEmptyRow() {
        // Given
        final Column column1 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN1");
        final Column column2 = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN2");
        final Row row = new Row().withColumn(column1, "value1").withColumn(column2, "value2");

        // When
        final int result = row.size();

        // Then
        assertEquals(2, result);
    }
}