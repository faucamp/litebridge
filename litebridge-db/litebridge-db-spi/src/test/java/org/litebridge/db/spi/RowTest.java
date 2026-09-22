package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RowTest {

    @Test
    void column() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row(List.of(new RowColumn(column.name(), "testValue", column)));

        // When
        final RowColumn result = row.column("TEST_COLUMN");

        // Then
        assertEquals("testValue", result.value());
        assertEquals(column, result.column());
    }

    @Test
    void column_alias() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row(List.of(new RowColumn("c1", "testValue", column)));

        // When
        final RowColumn result = row.column("c1");

        // Then
        assertEquals("testValue", result.value());
        assertEquals(column, result.column());
    }

    @Test
    void testToString() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row(List.of(new RowColumn("c1", "testValue", column)));

        // When
        final String result = row.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("c1"));
        assertTrue(result.contains("TEST_COLUMN"));
        assertTrue(result.contains("testValue"));
    }

    @Test
    void testToString_aliasSameAsColumn() {
        // Given
        final Column column = new Column(new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE"), "TEST_COLUMN");
        final Row row = new Row(List.of(new RowColumn("TEST_COLUMN", "testValue", column)));

        // When
        final String result = row.toString();

        // Then
        assertNotNull(result);
        assertFalse(result.contains("c1"));
        assertTrue(result.contains("TEST_COLUMN"));
        assertTrue(result.contains("testValue"));
    }

    @Test
    void testSize_emptyRow() {
        // Given
        final Row row = new Row(Collections.emptyList());

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
        final RowColumn rowColumn1 = new RowColumn("c1", "val1", column1);
        final RowColumn rowColumn2 = new RowColumn("c2", "val2", column2);
        final Row row = new Row(List.of(rowColumn1, rowColumn2));

        // When
        final int result = row.size();

        // Then
        assertEquals(2, result);
    }

    @Test
    void updateColumn() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");
        final RowColumn rowColumn = new RowColumn("c1", "val1", column);
        final List<RowColumn> rowColumns = new ArrayList<>();
        rowColumns.add(rowColumn);
        final Row row = new Row(rowColumns);

        // When
        row.updateColumn(0, new RowColumn("c1", "val2", column));

        // Then
        assertEquals("val2", row.value("c1"));
    }

    @Test
    void columns_and_columnByIndex() {
        // Given
        final Column column1 = new Column(new Table("T1"), "C1");
        final Column column2 = new Column(new Table("T1"), "C2");
        final RowColumn rowColumn1 = new RowColumn("c1", "val1", column1);
        final RowColumn rowColumn2 = new RowColumn("c2", "val2", column2);
        final Row row = new Row(List.of(rowColumn1, rowColumn2));

        // When
        final List<RowColumn> columns = row.columns();

        // Then
        assertEquals(2, columns.size());
        assertEquals("C1", row.column(0).column().name());
        assertEquals("C2", row.column(1).column().name());
    }
}