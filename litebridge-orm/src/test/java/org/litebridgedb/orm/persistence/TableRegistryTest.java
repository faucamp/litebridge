package org.litebridgedb.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;

import java.lang.invoke.MethodHandles;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableRegistryTest {

    @Test
    void getTable_byDtoClass() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final OrmTable ormTable = ormTable(TestDto.class, "public", "test_table");
        tableRegistry.addTable(TestDto.class, ormTable);

        // When
        final OrmTable result = tableRegistry.getTable(TestDto.class);

        // Then
        assertSame(ormTable, result);
    }

    @Test
    void getTable_byDtoClass_notFound() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When
        final OrmTable result = tableRegistry.getTable(TestDto.class);

        // Then
        assertNull(result);
    }

    @Test
    void getTable_byDtoClass_null() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When/Then
        assertThrows(NullPointerException.class, () -> tableRegistry.getTable((Class<?>) null));
    }

    @Test
    void getTableOrThrow() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final OrmTable ormTable = ormTable(TestDto.class, "public", "test_table");
        tableRegistry.addTable(TestDto.class, ormTable);

        // When
        final OrmTable result = tableRegistry.getTableOrThrow(TestDto.class);

        // Then
        assertSame(ormTable, result);
    }

    @Test
    void getTableOrThrow_notFound() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When/Then
        assertThrows(NullPointerException.class, () -> tableRegistry.getTableOrThrow(TestDto.class));
    }

    @Test
    void getTableInContext() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final OrmTable contextTable = ormTable(ContextDto.class, "public", "context_table");
        final OrmTable nestedTable = ormTable(TestDto.class, "public", "test_table");
        contextTable.getContextTableRegistry().addTable(TestDto.class, nestedTable);
        tableRegistry.addTable(ContextDto.class, contextTable);

        // When
        final Optional<OrmTable> result = tableRegistry.getTableInContext(TestDto.class, ContextDto.class);

        // Then
        assertTrue(result.isPresent());
        assertSame(nestedTable, result.get());
    }

    @Test
    void getTableInContext_notFound() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final OrmTable contextTable = ormTable(ContextDto.class, "public", "context_table");
        tableRegistry.addTable(ContextDto.class, contextTable);

        // When
        final Optional<OrmTable> result = tableRegistry.getTableInContext(TestDto.class, ContextDto.class);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getTableInContext_contextNotFound() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When
        final Optional<OrmTable> result = tableRegistry.getTableInContext(TestDto.class, ContextDto.class);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getTableInContextOrThrow() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final OrmTable contextTable = ormTable(ContextDto.class, "public", "context_table");
        final OrmTable nestedTable = ormTable(TestDto.class, "public", "test_table");
        contextTable.getContextTableRegistry().addTable(TestDto.class, nestedTable);
        tableRegistry.addTable(ContextDto.class, contextTable);

        // When
        final OrmTable result = tableRegistry.getTableInContextOrThrow(TestDto.class, ContextDto.class);

        // Then
        assertSame(nestedTable, result);
    }

    @Test
    void getTable_string() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final OrmTable ormTable = ormTable(TestDto.class, "public", "test_table");
        tableRegistry.addTable(TestDto.class, ormTable);

        // When
        final OrmTable result = tableRegistry.getTable("public.test_table");

        // Then
        assertSame(ormTable, result);
    }

    @Test
    void getTable_schemaAndTable() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final OrmTable ormTable = ormTable(TestDto.class, "public", "test_table");
        tableRegistry.addTable(TestDto.class, ormTable);

        // When
        final OrmTable result = tableRegistry.getTable("public", "test_table");

        // Then
        assertSame(ormTable, result);
    }

    @Test
    void getTable_schemaAndTable_notFound() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When
        final OrmTable result = tableRegistry.getTable("public", "test_table");

        // Then
        assertNull(result);
    }

    @Test
    void getTable_table() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final OrmTable ormTable = ormTable(TestDto.class, "public", "test_table");
        tableRegistry.addTable(TestDto.class, ormTable);

        // When
        final OrmTable result = tableRegistry.getTable(new Table("", "public", "test_table"));

        // Then
        assertSame(ormTable, result);
    }

    @Test
    void getTable_table_nullSchema() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When/Then
        assertThrows(NullPointerException.class, () -> tableRegistry.getTable(new Table("", null, "test_table")));
    }

    @Test
    void containsTable() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable(TestDto.class, "public", "test_table"));

        // When
        final boolean result = tableRegistry.containsTable(TestDto.class);

        // Then
        assertTrue(result);
    }

    @Test
    void containsTable_notFound() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When
        final boolean result = tableRegistry.containsTable(TestDto.class);

        // Then
        assertFalse(result);
    }

    @Test
    void tableStream() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        final OrmTable firstTable = ormTable(TestDto.class, "public", "test_table");
        final OrmTable secondTable = ormTable(AnotherTestDto.class, "other", "another_test_table");
        tableRegistry.addTable(TestDto.class, firstTable);
        tableRegistry.addTable(AnotherTestDto.class, secondTable);

        // When
        final List<OrmTable> result = tableRegistry.tableStream().toList();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(firstTable));
        assertTrue(result.contains(secondTable));
    }

    @Test
    void getOrCreateSpiTable_existingOrmTable() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();
        tableRegistry.addTable(TestDto.class, ormTable(TestDto.class, "public", "test_table"));

        // When
        final Table result = tableRegistry.getOrCreateSpiTable("public.test_table");

        // Then
        assertNull(result.catalog());
        assertEquals("public", result.schema());
        assertEquals("test_table", result.name());
    }

    @Test
    void getOrCreateSpiTable_notRegistered() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When
        final Table result = tableRegistry.getOrCreateSpiTable("public.test_table");

        // Then
        assertNull(result.catalog());
        assertEquals("public", result.schema());
        assertEquals("test_table", result.name());
    }

    @Test
    void getOrCreateSpiTable_withCatalog() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When
        final Table result = tableRegistry.getOrCreateSpiTable("cat.public.test_table");

        // Then
        assertEquals("cat", result.catalog());
        assertEquals("public", result.schema());
        assertEquals("test_table", result.name());
    }

    @Test
    void getOrCreateSpiTable_onlyTable() {
        // Given
        final TableRegistry tableRegistry = new TableRegistry();

        // When
        final Table result = tableRegistry.getOrCreateSpiTable("test_table");

        // Then
        assertNull(result.catalog());
        assertNull(result.schema());
        assertEquals("test_table", result.name());
    }

    private static OrmTable ormTable(final Class<?> dtoClass, final String schema, final String tableName) {
        final Table table = new Table("", schema, tableName);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "id", false, Types.BIGINT);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("id"), List.of(idColumn));
        return new OrmTable(dtoClass, tableMetaData, Map.of(), new ChangeTracker(MethodHandles.lookup()), new ClassFieldAccessorCache(MethodHandles.lookup()));
    }

    private static class TestDto {
        private String myVar;
    }

    private static class AnotherTestDto {
        private String myVar;
    }

    private static class ContextDto {
        private String myVar;
    }
}