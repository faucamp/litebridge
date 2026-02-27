package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
    void copyConstructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData original = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");
        original.setJoinColumn("parent_id");
        original.setAlias("c");

        // When
        final ColumnMetaData copy = new ColumnMetaData(original, "c");

        // Then
        assertEquals(original.table(), copy.table());
        assertEquals(original.name(), copy.name());
        assertEquals(original.alias(), copy.alias());

        assertEquals(original.isNullable(), copy.isNullable());
        assertEquals(original.getDataType(), copy.getDataType());
        assertEquals(original.getSize(), copy.getSize());
        assertEquals(original.getDecimalDigits(), copy.getDecimalDigits());
        assertEquals(original.isAutoIncrement(), copy.isAutoIncrement());
        assertEquals(original.getSequence(), copy.getSequence());
        assertEquals(original.getJoinColumn(), copy.getJoinColumn());
    }

    @Test
    void as_returnsCopyWithAlias() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData original = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");
        original.setJoinColumn("parent_id");

        // When
        final ColumnMetaData aliased = original.as("x");

        // Then
        assertNotSame(original, aliased);
        assertEquals("x", aliased.alias());

        // Should preserve core metadata
        assertEquals(original.name(), aliased.name());
        assertEquals(original.isNullable(), aliased.isNullable());
        assertEquals(original.getDataType(), aliased.getDataType());
        assertEquals(original.getSize(), aliased.getSize());
        assertEquals(original.getDecimalDigits(), aliased.getDecimalDigits());
        assertEquals(original.isAutoIncrement(), aliased.isAutoIncrement());
        assertEquals(original.getSequence(), aliased.getSequence());
        assertEquals(original.getJoinColumn(), aliased.getJoinColumn());
    }

    @Test
    void equals_and_hashCode_contract() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        final ColumnMetaData a = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");
        final ColumnMetaData b = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");

        assertEquals(a, a);
        assertEquals(a.hashCode(), b.hashCode());

        assertNotEquals(a, null);
        assertNotEquals(a, "not a ColumnMetaData");

        assertNotEquals(a, new ColumnMetaData(table, "different", false, 1, 20, 0, true, "SEQ_ID"));
        assertEquals(a, new ColumnMetaData(table, "id", true, 1, 20, 0, true, "SEQ_ID"));
    }

    @Test
    void equals_includesAliasAndJoinColumn() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        final ColumnMetaData a = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");
        final ColumnMetaData b = new ColumnMetaData(table, "id", false, 1, 20, 0, true, "SEQ_ID");

        a.setAlias("a1");
        b.setAlias("b1");

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
        assertTrue(s.contains("nullable=false"));
        assertTrue(s.contains("dataType=1"));
        assertTrue(s.contains("size=20"));
        assertTrue(s.contains("decimalDigits=0"));
        assertTrue(s.contains("autoIncrement=true"));
        assertTrue(s.contains("sequenceName='SEQ_ID'"));
    }
}