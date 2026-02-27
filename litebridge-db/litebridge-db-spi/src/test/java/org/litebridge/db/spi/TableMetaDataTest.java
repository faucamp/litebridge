package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableMetaDataTest {

    /**
     * Test to verify the primaryKey method returns the correct list of primary key column metadata.
     */
    @Test
    void primaryKey() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = List.of("id");
        final List<ColumnMetaData> columns = List.of(column1, column2);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        List<ColumnMetaData> result = tableMetaData.primaryKey();

        // Then
        assertEquals(1, result.size());
        assertEquals(column1, result.getFirst());
    }

    /**
     * Test to verify that primaryKey method correctly handles an empty primary key list.
     */
    @Test
    void primaryKey_emptyList() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = Collections.emptyList();
        final List<ColumnMetaData> columns = List.of(column1, column2);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        final List<ColumnMetaData> result = tableMetaData.primaryKey();

        // Then
        assertTrue(result.isEmpty());
    }

    /**
     * Test to verify primaryKey throws an exception when primary key column metadata is missing.
     */
    @Test
    void primaryKey_missingMetadata() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = List.of("id");
        final List<ColumnMetaData> columns = List.of(column1);

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () ->
                new TableMetaData(table, primaryKeyNames, columns));

        // Then
        assertTrue(result.getMessage().contains("All column metadata for PKs not found"));
    }

    /**
     * Test to verify that primaryKey method filters out non-primary key columns.
     */
    @Test
    void primaryKey_excludesNonPKColumns() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);
        final ColumnMetaData column3 = new ColumnMetaData(table, "description", true, 12);

        final List<String> primaryKeyNames = List.of("id", "name");
        final List<ColumnMetaData> columns = List.of(column1, column2, column3);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        final List<ColumnMetaData> result = tableMetaData.primaryKey();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(column1));
        assertTrue(result.contains(column2));
        assertFalse(result.contains(column3));
    }

    @Test
    void columns() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = List.of("id");
        final List<ColumnMetaData> columns = List.of(column1, column2);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        final List<ColumnMetaData> result = tableMetaData.columns();

        // Then
        assertEquals(2, result.size());
        assertEquals(column1, result.getFirst());
        assertEquals(column2, result.getLast());
    }

    @Test
    void column() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = List.of("id");
        final List<ColumnMetaData> columns = List.of(column1, column2);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        final ColumnMetaData result = tableMetaData.column("id");

        // Then
        assertEquals(column1, result);
    }

    @Test
    void column_invalid() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = List.of("id");
        final List<ColumnMetaData> columns = List.of(column1, column2);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> tableMetaData.column("randomColumn"));
    }

    @Test
    void hasColumn_true() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = List.of("id");
        final List<ColumnMetaData> columns = List.of(column1, column2);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        final boolean result = tableMetaData.hasColumn("id");

        // Then
        assertTrue(result);
    }

    @Test
    void column_false() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = List.of("id");
        final List<ColumnMetaData> columns = List.of(column1, column2);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        final boolean result = tableMetaData.hasColumn("otherColumn");

        // Then
        assertFalse(result);
    }

    @Test
    void equals_sameObject() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final List<ColumnMetaData> columns = List.of(new ColumnMetaData(table, "id", false, 1));
        final List<String> primaryKeyNames = List.of("id");
        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        boolean result = tableMetaData.equals(tableMetaData);

        // Then
        assertTrue(result);
    }

    @Test
    void equals_equalObjects() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final List<ColumnMetaData> columns1 = List.of(new ColumnMetaData(table1, "id", false, 1));
        final List<ColumnMetaData> columns2 = List.of(new ColumnMetaData(table2, "id", false, 1));
        final List<String> primaryKeyNames = List.of("id");

        final TableMetaData tableMetaData1 = new TableMetaData(table1, primaryKeyNames, columns1);
        final TableMetaData tableMetaData2 = new TableMetaData(table2, primaryKeyNames, columns2);

        // When
        boolean result = tableMetaData1.equals(tableMetaData2);

        // Then
        assertTrue(result);
    }

    @Test
    void equals_differentColumns() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final List<ColumnMetaData> columns1 = List.of(new ColumnMetaData(table1, "id", false, 1));
        final List<ColumnMetaData> columns2 = List.of(new ColumnMetaData(table2, "name", true, 2));

        final TableMetaData tableMetaData1 = new TableMetaData(table1, List.of("id"), columns1);
        final TableMetaData tableMetaData2 = new TableMetaData(table2, List.of("name"), columns2);

        // When
        boolean result = tableMetaData1.equals(tableMetaData2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_differentPrimaryKey() {
        // Given
        final Table table1 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final List<ColumnMetaData> columns1 = List.of(new ColumnMetaData(table1, "id", false, 1), new ColumnMetaData(table1, "name", true, 2));
        final List<ColumnMetaData> columns2 = List.of(new ColumnMetaData(table2, "id", false, 1), new ColumnMetaData(table2, "name", true, 2));
        final List<String> primaryKeyNames1 = List.of("id");
        final List<String> primaryKeyNames2 = List.of("name");

        final TableMetaData tableMetaData1 = new TableMetaData(table1, primaryKeyNames1, columns1);
        final TableMetaData tableMetaData2 = new TableMetaData(table2, primaryKeyNames2, columns2);

        // When
        boolean result = tableMetaData1.equals(tableMetaData2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_differentTableAttributes() {
        // Given
        final Table table1 = new Table("DIFFERENT_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Table table2 = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final List<ColumnMetaData> columns = List.of(new ColumnMetaData(table1, "id", false, 1));
        final List<String> primaryKeyNames = List.of("id");

        final TableMetaData tableMetaData1 = new TableMetaData(table1, primaryKeyNames, columns);
        final TableMetaData tableMetaData2 = new TableMetaData(table2, primaryKeyNames, columns);

        // When
        boolean result = tableMetaData1.equals(tableMetaData2);

        // Then
        assertFalse(result);
    }

    @Test
    void testToString() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = List.of("id");
        final List<ColumnMetaData> columns = List.of(column1, column2);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        final String result = tableMetaData.toString();

        // Then
        assertNotNull(result);
    }

    @Test
    void testHashCode() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column1 = new ColumnMetaData(table, "id", false, 1);
        final ColumnMetaData column2 = new ColumnMetaData(table, "name", true, 12);

        final List<String> primaryKeyNames = List.of("id");
        final List<ColumnMetaData> columns = List.of(column1, column2);

        final TableMetaData tableMetaData = new TableMetaData(table, primaryKeyNames, columns);

        // When
        final int result = tableMetaData.hashCode();

        // Then
        assertTrue(result != 0);
    }
}