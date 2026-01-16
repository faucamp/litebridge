package org.litebridge.orm.api.spec;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TableSpecTest {

    @Test
    void fieldColumnSpecMap() {
        // Given
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = new HashMap<>();
        fieldColumnSpecMap.put(new FieldSpecImpl("testField", false), new ColumnSpecImpl("testColumn", false, null, null));
        final TableSpec tableSpec = new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);

        // When
        final Map<FieldSpec, ColumnSpec> result = tableSpec.fieldColumnSpecMap();

        // Then
        assertNotSame(fieldColumnSpecMap, result);
        assertEquals(fieldColumnSpecMap, result);
    }

    @Test
    void constructor_nullCatalogAndSchema() {
        // When
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = new HashMap<>();
        fieldColumnSpecMap.put(new FieldSpecImpl("testField", false), new ColumnSpecImpl("testColumn", false, null, null));
        final TableSpec result = new TableSpec(null, null, "users", fieldColumnSpecMap);

        // Then
        assertEquals("", result.catalog());
        assertEquals("", result.schema());
        assertEquals("users", result.name());
    }

    @Test
    void constructor_blankTableName() {
        assertThrows(IllegalArgumentException.class, () -> new TableSpec(null, null, "   ", Collections.emptyMap()));
    }

    @Test
    void constructor_emptyMap() {
        assertThrows(RuntimeException.class, () -> new TableSpec(null, null, "t", Collections.emptyMap()));
    }

    @Test
    void constructor_nullMap() {
        assertThrows(IllegalArgumentException.class, () -> new TableSpec("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", null));
    }

    @Test
    void t() {
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = new HashMap<>();
        fieldColumnSpecMap.put(new FieldSpecImpl("testField", false), new ColumnSpecImpl("testColumn", false, null, null));

        // When
        final TableSpec result = TableSpec.t("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);

        // Then
        assertNotNull(result);
        assertEquals("TEST_CATALOG", result.catalog());
        assertEquals("TEST_SCHEMA", result.schema());
        assertEquals("TEST_TABLE", result.name());
        assertNotSame(fieldColumnSpecMap, result.fieldColumnSpecMap());
        assertEquals(fieldColumnSpecMap, result.fieldColumnSpecMap());
    }

    @Test
    void t_noCatalog() {
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = new HashMap<>();
        fieldColumnSpecMap.put(new FieldSpecImpl("testField", false), new ColumnSpecImpl("testColumn", false, null, null));

        // When
        final TableSpec result = TableSpec.t("TEST_SCHEMA", "TEST_TABLE", fieldColumnSpecMap);

        // Then
        assertNotNull(result);
        assertEquals("", result.catalog());
        assertEquals("TEST_SCHEMA", result.schema());
        assertEquals("TEST_TABLE", result.name());
        assertNotSame(fieldColumnSpecMap, result.fieldColumnSpecMap());
        assertEquals(fieldColumnSpecMap, result.fieldColumnSpecMap());
    }

    @Test
    void t_noCatalogOrSchema() {
        final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap = new HashMap<>();
        fieldColumnSpecMap.put(new FieldSpecImpl("testField", false), new ColumnSpecImpl("testColumn", false, null, null));

        // When
        final TableSpec result = TableSpec.t("TEST_TABLE", fieldColumnSpecMap);

        // Then
        assertNotNull(result);
        assertEquals("", result.catalog());
        assertEquals("", result.schema());
        assertEquals("TEST_TABLE", result.name());
        assertNotSame(fieldColumnSpecMap, result.fieldColumnSpecMap());
        assertEquals(fieldColumnSpecMap, result.fieldColumnSpecMap());
    }
}