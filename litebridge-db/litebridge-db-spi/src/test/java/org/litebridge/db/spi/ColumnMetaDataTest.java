package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnMetaDataTest {

    @Test
    void constructor_full_setsAllFields() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final ColumnMetaData column = new ColumnMetaData(
                table,
                "amount",
                true,
                12,
                10,
                2,
                true,
                "SEQ_AMOUNT"
        );

        // Then
        assertEquals("amount", column.name());
        assertTrue(column.isNullable());
        assertEquals(12, column.getDataType());
        assertEquals(10, column.getSize());
        assertEquals(2, column.getDecimalDigits());
        assertTrue(column.isAutoIncrement());
        assertEquals("SEQ_AMOUNT", column.getSequence());

        assertNull(column.getJoinColumn());
    }

    @Test
    void constructor_convenience_defaultValues() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When 1
        final ColumnMetaData c1 = new ColumnMetaData(table, "id", false, 1, 20);
        // Then 1
        assertEquals(0, c1.getDecimalDigits());
        assertFalse(c1.isAutoIncrement());
        assertNull(c1.getSequence());

        // When 2
        final ColumnMetaData c2 = new ColumnMetaData(table, "id", false, 1);
        // Then 2
        assertEquals(0, c2.getSize());
        assertEquals(0, c2.getDecimalDigits());
        assertFalse(c2.isAutoIncrement());
        assertNull(c2.getSequence());
    }

    @Test
    void setters_updateMutableFields() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column = new ColumnMetaData(table, "id", false, 1);

        assertFalse(column.isAutoIncrement());
        column.setAutoIncrement(true);
        assertTrue(column.isAutoIncrement());

        assertNull(column.getSequence());
        column.setSequence("SEQ_ID");
        assertEquals("SEQ_ID", column.getSequence());
        column.setSequence(null);
        assertNull(column.getSequence());

        assertNull(column.getJoinColumn());
        column.setJoinColumn("other_id");
        assertEquals("other_id", column.getJoinColumn());
        column.setJoinColumn(null);
        assertNull(column.getJoinColumn());
    }

    @Test
    void equals_and_hashCode_contract() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        final ColumnMetaData a = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");
        final ColumnMetaData b = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        assertNotEquals(a, null);
        assertNotEquals(a, "not a ColumnMetaData");

        assertNotEquals(a, new ColumnMetaData(table, "different", false, 1, 20, 0, true, "SEQ_ID"));
        assertEquals(a, new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID"));
    }

    @Test
    void equals_includesAliasAndJoinColumn() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        final ColumnMetaData a = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");
        final ColumnMetaData b = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");

        a.setJoinColumn("x");
        b.setJoinColumn("y");

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_containsUsefulParts() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");

        final String s = column.toString();

        assertNotNull(s);
        assertTrue(s.contains("ColumnMetaData["));
        assertTrue(s.contains("name='id'"));
    }
}