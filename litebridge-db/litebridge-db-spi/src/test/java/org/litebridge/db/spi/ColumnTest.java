package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColumnTest {

    private final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

    @Test
    void table() {
        // Given
        final Column Column = new Column(table, "TEST_COLUMN");

        // When
        final Table result = Column.table();

        // Then
        assertEquals(table, result);
    }

    @Test
    void name() {
        // Given
        final Column Column = new Column(table, "testName");

        // When
        final String result = Column.name();

        // Then
        assertEquals("testName", result);
    }

    @Test
    void alias() {
        // Given
        final Column Column = new Column(table, "testName", "testAlias");

        // When
        final String result = Column.alias();

        // Then
        assertEquals("testAlias", result);
    }

    @Test
    void as() {
        // Given
        final Column Column = new Column(table, "testName");

        // When
        final Column result = Column.as("testAlias");

        // Then
        assertEquals(table, result.table());
        assertEquals("testName", result.name());
        assertEquals("testAlias", result.alias());
    }

    @Test
    void c() {
        // When
        final Column result = Column.c(table, "testName");

        // Then
        assertEquals(table, result.table());
        assertEquals("testName", result.name());
    }

    @Test
    void c_tableName() {
        // When
        final Column result = Column.c("TEST_TABLE", "testName");

        // Then
        assertEquals("", result.table().catalog());// Then
        assertEquals("", result.table().schema());
        assertEquals("TEST_TABLE", result.table().name());
        assertEquals("testName", result.name());
    }

    @Test
    void c_schemaTableNames() {
        // When
        final Column result = Column.c("TEST_SCHEMA", "TEST_TABLE", "testName");

        // Then
        assertEquals("", result.table().catalog());
        assertEquals("TEST_SCHEMA", result.table().schema());
        assertEquals("TEST_TABLE", result.table().name());
        assertEquals("testName", result.name());
    }

    @Test
    void c_catalogSchemaTableNames() {
        // When
        final Column result = Column.c("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "testName");

        // Then
        assertEquals("TEST_CATALOG", result.table().catalog());
        assertEquals("TEST_SCHEMA", result.table().schema());
        assertEquals("TEST_TABLE", result.table().name());
        assertEquals("testName", result.name());
    }
}