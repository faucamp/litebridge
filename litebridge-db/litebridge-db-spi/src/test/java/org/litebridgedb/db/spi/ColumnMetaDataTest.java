package org.litebridgedb.db.spi;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ColumnMetaDataTest {

    @Test
    void constructor_full_setsAllFields() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);

        // When
        final ColumnMetaData column = new ColumnMetaData(
                table,
                "amount",
                true,
                12,
                10,
                2,
                true,
                generator
        );

        // Then
        assertEquals("amount", column.name());
        assertTrue(column.isNullable());
        assertEquals(12, column.getDataType());
        assertEquals(10, column.getSize());
        assertEquals(2, column.getDecimalDigits());
        assertTrue(column.isAutoIncrement());
        assertEquals(generator, column.getGenerator());

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
        assertNull(c1.getGenerator());

        // When 2
        final ColumnMetaData c2 = new ColumnMetaData(table, "id", false, 1);
        // Then 2
        assertEquals(0, c2.getSize());
        assertEquals(0, c2.getDecimalDigits());
        assertFalse(c2.isAutoIncrement());
        assertNull(c2.getGenerator());
    }

    @Test
    void setters_updateMutableFields() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column = new ColumnMetaData(table, "id", false, 1);

        assertFalse(column.isAutoIncrement());
        column.setAutoIncrement(true);
        assertTrue(column.isAutoIncrement());

        assertNull(column.getGenerator());
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        column.setGenerator(generator);
        assertEquals(generator, column.getGenerator());
        column.setGenerator(null);
        assertNull(column.getGenerator());

        assertNull(column.getJoinColumn());
        column.setJoinColumn("other_id");
        assertEquals("other_id", column.getJoinColumn());
        column.setJoinColumn(null);
        assertNull(column.getJoinColumn());
    }

    @Test
    void equals_and_hashCode_contract() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);

        final ColumnMetaData a = new ColumnMetaData(table, "id", false, 1, 20, 0, true, generator);
        final ColumnMetaData b = new ColumnMetaData(table, "id", false, 1, 20, 0, true, generator);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        assertNotEquals(a, null);
        assertNotEquals(a, "not a ColumnMetaData");

        assertNotEquals(a, new ColumnMetaData(table, "different", false, 1, 20, 0, true, generator));
        assertEquals(a, new ColumnMetaData(table, "id", false, 1, 20, 0, true, generator));
    }

    @Test
    void equals_differentFields() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        final ColumnMetaData base = new ColumnMetaData(table, "id", false, 1, 20, 0, true, generator);

        assertNotEquals(base, new ColumnMetaData(table, "id", true, 1, 20, 0, true, generator)); // nullable
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 2, 20, 0, true, generator)); // dataType
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 1, 21, 0, true, generator)); // size
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 1, 20, 1, true, generator)); // decimalDigits
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 1, 20, 0, false, generator)); // autoIncrement
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 1, 20, 0, true, mock(ColumnValueGenerator.class))); // sequence

        final Table table2 = new Table("OTHER", "OTHER", "OTHER");
        assertNotEquals(base, new ColumnMetaData(table2, "id", false, 1, 20, 0, true, generator)); // table

        ColumnMetaData withJoin = new ColumnMetaData(table, "id", false, 1, 20, 0, true, generator);
        withJoin.setJoinColumn("JOIN");
        assertNotEquals(base, withJoin); // joinColumn
    }

    @Test
    void equals_sameInstance() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column = new ColumnMetaData(table, "id", false, 1);
        assertEquals(column, column);
    }

    @Test
    void toString_containsUsefulParts() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column = new ColumnMetaData(table, "id", false, 1, 20, 0, true, mock(ColumnValueGenerator.class));

        // When
        final String s = column.toString();

        // Then
        assertNotNull(s);
        assertTrue(s.contains("ColumnMetaData["));
        assertTrue(s.contains("name='id'"));
    }

    @Test
    void table() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "id", false, 1, 20, 0, true, mock(ColumnValueGenerator.class));

        // When
        final Table result = columnMetaData.table();

        // Then
        assertEquals(table, result);
    }

    @Test
    void toColumn() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "id", false, 1, 20, 0, true, mock(ColumnValueGenerator.class));

        // When
        final Column result = columnMetaData.toColumn();

        // Then
        assertEquals(columnMetaData.table(), result.table());
        assertEquals(columnMetaData.name(), result.name());
        assertNull(result.alias());
    }
}