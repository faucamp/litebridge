package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.generator.ColumnValueGenerator;

import java.util.function.Supplier;

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
    void equals_sameOptionalFieldsAreEqual() {
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);

        final Supplier<ColumnMetaData> columnMetaDataSupplier = () -> mock(ColumnMetaData.class);
        final ColumnMetaData a = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);
        a.setJoinColumnSupplier(columnMetaDataSupplier);

        final ColumnMetaData b = new ColumnMetaData(table, "id", false, 1, 20, 0, true, null, generator);
        b.setJoinColumnSupplier(columnMetaDataSupplier);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
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
        final Column result = columnMetaData.column();

        // Then
        assertEquals(columnMetaData.table(), result.table());
        assertEquals(columnMetaData.name(), result.name());
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
    void getters_nullable_dataType_size_decimalDigits_defaultValue() {
        // Given
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", true, 1, 10, 2, false, "DEFAULT_VAL", null);

        // Then
        assertTrue(column.isNullable());
        assertEquals(1, column.getDataType());
        assertEquals(10, column.getSize());
        assertEquals(2, column.getDecimalDigits());
        assertEquals("DEFAULT_VAL", column.getDefaultValue());
    }

    @Test
    void setGenerator() {
        // Given
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", false, 1);
        final ColumnValueGenerator generator = mock(ColumnValueGenerator.class);

        // When
        column.setGenerator(generator);

        // Then
        assertEquals(generator, column.getGenerator());
    }

    @Test
    void getJoinColumn_withSupplierAndCaching() {
        // Given
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", false, 1);
        final ColumnMetaData targetJoinColumn = new ColumnMetaData(new Table("T2"), "C2", false, 1);

        // When
        assertNull(column.getJoinColumn());
        column.setJoinColumnSupplier(() -> targetJoinColumn);

        // Then
        assertEquals(targetJoinColumn, column.getJoinColumn());
        assertEquals(targetJoinColumn, column.getJoinColumn()); // Cached branch
    }

    @Test
    void addForeignKeyConstraint_multiple() {
        // Given
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", false, 1);
        final ForeignKeyConstraint c1 = new ForeignKeyConstraint("FK1", new Column(new Table("T2"), "C2"));
        final ForeignKeyConstraint c2 = new ForeignKeyConstraint("FK2", new Column(new Table("T3"), "C3"));

        // When
        column.addForeignKeyConstraint(c1);
        column.addForeignKeyConstraint(c2);

        // Then
        assertEquals(2, column.getForeignKeyConstraints().size());
        assertEquals(c1, column.getForeignKeyConstraints().get(0));
        assertEquals(c2, column.getForeignKeyConstraints().get(1));
    }

    @Test
    void addForeignReference_multiple() {
        // Given
        final Table table = new Table("T1");
        final ColumnMetaData column = new ColumnMetaData(table, "C1", false, 1);
        final ForeignKeyConstraint r1 = new ForeignKeyConstraint("FK1", new Column(new Table("T2"), "C2"));
        final ForeignKeyConstraint r2 = new ForeignKeyConstraint("FK2", new Column(new Table("T3"), "C3"));

        // When
        column.addForeignReference(r1);
        column.addForeignReference(r2);

        // Then
        assertEquals(2, column.getForeignReferences().size());
        assertEquals(r1, column.getForeignReferences().get(0));
        assertEquals(r2, column.getForeignReferences().get(1));
    }

    @Test
    void equals_and_hashCode_differences() {
        // Given
        final Table table = new Table("T1");
        final ColumnValueGenerator gen1 = mock(ColumnValueGenerator.class);
        final ColumnValueGenerator gen2 = mock(ColumnValueGenerator.class);
        final ColumnMetaData base = new ColumnMetaData(table, "C1", false, 1, 10, 2, false, null, gen1);

        // Then
        assertNotEquals(base, new ColumnMetaData(table, "C1", true, 1, 10, 2, false, null, gen1));
        assertNotEquals(base, new ColumnMetaData(table, "C1", false, 2, 10, 2, false, null, gen1));
        assertNotEquals(base, new ColumnMetaData(table, "C1", false, 1, 20, 2, false, null, gen1));
        assertNotEquals(base, new ColumnMetaData(table, "C1", false, 1, 10, 3, false, null, gen1));
        assertNotEquals(base, new ColumnMetaData(table, "C1", false, 1, 10, 2, true, null, gen1));
        assertNotEquals(base, new ColumnMetaData(table, "C1", false, 1, 10, 2, false, null, gen2));

        final ColumnMetaData withJoin1 = new ColumnMetaData(table, "C1", false, 1, 10, 2, false, null, gen1);
        final ColumnMetaData targetJoin = new ColumnMetaData(new Table("T2"), "C2", false, 1);
        withJoin1.setJoinColumnSupplier(() -> targetJoin);
        withJoin1.getJoinColumn();
        assertNotEquals(base, withJoin1);
    }
}