package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TableTest {

    @Test
    void catalog() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = table.catalog();

        // Then
        assertEquals("TEST_CATALOG", result);
    }

    @Test
    void schema() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = table.schema();

        // Then
        assertEquals("TEST_SCHEMA", result);
    }

    @Test
    void name() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = table.name();

        // Then
        assertEquals("TEST_TABLE", result);
    }

    @Test
    void as() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final Table result = table.as("testAlias");

        // Then
        assertEquals("testAlias", result.alias());

    }

    @Test
    void isTableMetaData() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final boolean result = table.isTableMetaData();

        // Then
        assertFalse(result);
    }
}