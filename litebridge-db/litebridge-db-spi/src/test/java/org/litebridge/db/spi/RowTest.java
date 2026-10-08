package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void structureHashCode_sameStructureDifferentValues_sameHash() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column col1 = new Column(table, "COL1");
        final Column col2 = new Column(table, "COL2");

        final Row row1 = new Row(List.of(
                new RowColumn("c1", "val1", col1, "t1"),
                new RowColumn("c2", 123, col2, "t1")
        ));

        final Row row2 = new Row(List.of(
                new RowColumn("c1", "differentVal", col1, "t1"),
                new RowColumn("c2", 456, col2, "t1")
        ));

        // When
        final int hash1 = row1.structureHashCode();
        final int hash2 = row2.structureHashCode();

        // Then
        assertEquals(hash1, hash2);
        assertEquals(hash1, row1.structureHashCode()); // cached call
    }

    @Test
    void structureHashCode_differentStructure_differentHash() {
        // Given
        final Table table1 = new Table("CAT1", "SCHEMA1", "TABLE1");
        final Table table2 = new Table("CAT2", "SCHEMA2", "TABLE2");
        final Column col1 = new Column(table1, "COL1");
        final Column col2 = new Column(table2, "COL2");

        final Row row1 = new Row(List.of(new RowColumn("c1", "val", col1, "t1")));
        final Row row2 = new Row(List.of(new RowColumn("c2", "val", col1, "t1")));
        final Row row3 = new Row(List.of(new RowColumn("c1", "val", col2, "t1")));
        final Row row4 = new Row(List.of(new RowColumn("c1", "val", col1, "t2")));
        final Row row5 = new Row(List.of(new RowColumn("c1", "val", null, "t1")));

        // When & Then
        assertFalse(row1.structureHashCode() == row2.structureHashCode());
        assertFalse(row1.structureHashCode() == row3.structureHashCode());
        assertFalse(row1.structureHashCode() == row4.structureHashCode());
        assertFalse(row1.structureHashCode() == row5.structureHashCode());
    }

    @Test
    void structureHashCode_nullMetadataAndAliases_succeeds() {
        // Given
        final Row row = new Row(List.of(
                new RowColumn("c1", "val", null),
                new RowColumn("c2", null, new Column("PLAIN_COL"))
        ));

        // When
        final int hash = row.structureHashCode();

        // Then
        assertTrue(hash != 0);
    }

    @Test
    void structureHashCode_emptyRow_succeeds() {
        // Given
        final Row row = new Row(Collections.emptyList());

        // When
        final int hash = row.structureHashCode();

        // Then
        assertEquals(1, hash);
    }

    @Test
    void structureHashCode_updateColumn_recalculatesHash() {
        // Given
        final Column col1 = new Column(new Table("T1"), "C1");
        final Column col2 = new Column(new Table("T1"), "C2");
        final List<RowColumn> rowColumns = new ArrayList<>();
        rowColumns.add(new RowColumn("c1", "val1", col1));
        final Row row = new Row(rowColumns);

        final int initialHash = row.structureHashCode();

        // When
        row.updateColumn(0, new RowColumn("c2", "val1", col2));
        final int updatedHash = row.structureHashCode();

        // Then
        assertFalse(initialHash == updatedHash);
    }

    @Test
    void value_byIndex() {
        // Given
        final Column column = new Column("col");
        final Row row = new Row(List.of(new RowColumn("col", "val", column)));

        // When
        final Object val = row.value(0);

        // Then
        assertEquals("val", val);
    }

    @Test
    void columnMap_withFallbackColumnName() {
        // Given
        final Column column1 = new Column("real_name1");
        final Column column2 = new Column("alias_name2");
        final RowColumn rc1 = new RowColumn("alias_name1", "val1", column1);
        final RowColumn rc2 = new RowColumn("alias_name2", "val2", column2);
        final Row row = new Row(List.of(rc1, rc2));

        // When
        final Map<String, RowColumn> map = row.columnMap();
        final Map<String, RowColumn> cachedMap = row.columnMap();

        // Then
        assertEquals(3, map.size());
        assertEquals(map, cachedMap);
        assertEquals(rc1, map.get("alias_name1"));
        assertEquals(rc1, map.get("real_name1"));
        assertEquals(rc2, map.get("alias_name2"));
    }

    @Test
    void columnMap_columnNameOverlapsWithExistingLabel() {
        // Given
        final RowColumn rc1 = new RowColumn("C1", "val1", null);
        final Column col2 = new Column("C1");
        final RowColumn rc2 = new RowColumn("ALIAS2", "val2", col2);
        final Row row = new Row(List.of(rc1, rc2));

        // When
        final Map<String, RowColumn> map = row.columnMap();

        // Then
        assertEquals(2, map.size());
        assertEquals(rc1, map.get("C1"));
        assertEquals(rc2, map.get("ALIAS2"));
    }

    @Test
    void column_notFound_throwsNoSuchElementException() {
        // Given
        final Row row = new Row(Collections.emptyList());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> row.column("nonExistent"));
    }

    @Test
    void indexOf_byColumn() {
        // Given
        final Table table = new Table("T1");
        final Column col1 = new Column(table, "C1");
        final Column col2 = new Column(table, "C2");
        final Row row = new Row(List.of(new RowColumn("C1", "v1", col1), new RowColumn("C2", "v2", col2)));

        // When & Then
        assertEquals(0, row.indexOf(col1));
        assertEquals(1, row.indexOf(col2));
        assertEquals(-1, row.indexOf(new Column("C3")));
    }

    @Test
    void indexOf_byString() {
        // Given
        final Column col1 = new Column("C1");
        final Row row = new Row(List.of(new RowColumn("C1", "v1", col1)));

        // When & Then
        assertEquals(0, row.indexOf("c1")); // case insensitive
        assertEquals(-1, row.indexOf("C2"));
    }

    @Test
    void equals_and_hashCode() {
        // Given
        final Column col = new Column("C1");
        final Row row1 = new Row(List.of(new RowColumn("C1", "v1", col)));
        final Row row2 = new Row(List.of(new RowColumn("C1", "v1", col)));
        final Row rowDifferent = new Row(List.of(new RowColumn("C1", "v2", col)));

        // When & Then
        assertEquals(row1, row1);
        assertEquals(row1, row2);
        assertEquals(row1.hashCode(), row2.hashCode());

        assertNotEquals(row1, rowDifferent);
        assertNotEquals(row1, null);
        assertNotEquals(row1, "not a row");
    }
}