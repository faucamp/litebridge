package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.generator.ColumnValueGenerator;

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
                null,
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

        final ColumnMetaData a = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);
        final ColumnMetaData b = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        assertNotEquals(a, null);
        assertNotEquals(a, "not a ColumnMetaData");

        assertNotEquals(a, new ColumnMetaData(table, "different", false, 1, 20, 0, true, null, generator));
        assertEquals(a, new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator));
    }

    @Test
    void equals_differentFields() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);
        final ColumnMetaData base = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);

        assertNotEquals(base, new ColumnMetaData(table, "id", true, 1, 20, 0, true, null, generator)); // nullable
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 2, 20, 0, true, null, generator)); // dataType
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 1, 21, 0, true, null, generator)); // size
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 1, 20, 1, true, null, generator)); // decimalDigits
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 1, 20, 0, false, null, generator)); // autoIncrement
        assertNotEquals(base, new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, mock(ColumnValueGenerator.class))); // sequence

        final Table table2 = new Table("OTHER", "OTHER", "OTHER");
        assertNotEquals(base, new ColumnMetaData(table2, "id", false, 1, 20, 0, true, null, generator)); // table

        ColumnMetaData withJoin = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);
        withJoin.setJoinColumn("JOIN");
        assertNotEquals(base, withJoin); // joinColumn
    }

    @Test
    void equals_sameOptionalFieldsAreEqual() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);

        final ColumnMetaData a = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);
        a.setJoinColumn("JOIN");

        final ColumnMetaData b = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);
        b.setJoinColumn("JOIN");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_optionalFieldsDifferWhenOnlyOneSideIsNull() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);

        final ColumnMetaData withGenerator = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);
        final ColumnMetaData withoutGenerator = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, null);

        assertNotEquals(withGenerator, withoutGenerator);
        assertNotEquals(withoutGenerator, withGenerator);

        final ColumnMetaData withJoin = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);
        withJoin.setJoinColumn("JOIN");

        final ColumnMetaData withoutJoin = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);

        assertNotEquals(withJoin, withoutJoin);
        assertNotEquals(withoutJoin, withJoin);
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
        final ColumnMetaData column = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, mock(ColumnValueGenerator.class));

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
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, mock(ColumnValueGenerator.class));

        // When
        final Table result = columnMetaData.table();

        // Then
        assertEquals(table, result);
    }

    @Test
    void toColumn() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, mock(ColumnValueGenerator.class));

        // When
        final Column result = columnMetaData.toColumn();

        // Then
        assertEquals(columnMetaData.table(), result.table());
        assertEquals(columnMetaData.name(), result.name());
        assertNull(result.alias());
    }

    @Test
    void foreignKeyConstraints() {
        // Given
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", false, 1);
        final Column foreignKeyColumn = new Column(new Table("T2"), "C2");
        final ForeignKeyConstraint constraint = new ForeignKeyConstraint("FK1", foreignKeyColumn);

        // When
        column.addForeignKeyConstraint(constraint);

        // Then
        assertEquals(1, column.getForeignKeyConstraints().size());
        assertEquals(constraint, column.getForeignKeyConstraints().get(0));
    }

    @Test
    void foreignReferences() {
        // Given
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", false, 1);
        final Column foreignKeyColumn = new Column(new Table("T2"), "C2");
        final ForeignKeyConstraint constraint = new ForeignKeyConstraint("FK1", foreignKeyColumn);

        // When
        column.addForeignReference(constraint);

        // Then
        assertEquals(1, column.getForeignReferences().size());
        assertEquals(constraint, column.getForeignReferences().get(0));
    }

    @Test
    void foreignKeyConstraints_empty() {
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", false, 1);
        assertTrue(column.getForeignKeyConstraints().isEmpty());
    }

    @Test
    void foreignReferences_empty() {
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", false, 1);
        assertTrue(column.getForeignReferences().isEmpty());
    }

    @Test
    void getters_nullable_dataType_size_decimalDigits() {
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", true, 1, 10, 2, false, null, null);

        assertTrue(column.isNullable());
        assertEquals(1, column.getDataType());
        assertEquals(10, column.getSize());
        assertEquals(2, column.getDecimalDigits());
    }
}