package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;

import static org.junit.jupiter.api.Assertions.*;

class DtoAliasRegistryTest {

    @Test
    void newAlias() {
        // Given
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = dtoAliasRegistry.newAlias(table);

        // Then
        assertNotNull(result);
        assertEquals("tt1", result);
    }

    @Test
    void testAlias_column() {
        // Given
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final String tableAlias = "tt1";

        // When
        final String result = dtoAliasRegistry.alias(tableAlias, column);

        // Then
        assertNotNull(result);
        assertEquals("tt1tc1", result);
    }

    @Test
    void newAliasOrNull_null() {
        // Given
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = dtoAliasRegistry.aliasOrNull(table, 0);

        // Then
        assertNull(result);
    }

    @Test
    void aliasOrNull_column_null() {
        // Given
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final String tableAlias = "tt1";

        // When
        final String result = dtoAliasRegistry.aliasOrNull(tableAlias, column);

        // Then
        assertNull(result);
    }

    @Test
    void belongsTo() {
        // Given
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final String tableAlias = dtoAliasRegistry.newAlias(table);
        dtoAliasRegistry.alias(tableAlias, column);

        // When
        final boolean result = dtoAliasRegistry.belongsTo(tableAlias, column);

        // Then
        assertTrue(result);
    }
}