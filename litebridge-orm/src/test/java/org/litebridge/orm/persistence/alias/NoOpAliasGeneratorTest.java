package org.litebridge.orm.persistence.alias;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoOpAliasGeneratorTest {

    @Test
    void newTableAlias() {
        // Given
        final NoOpAliasGenerator noOpAliasGenerator = new NoOpAliasGenerator();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = noOpAliasGenerator.newTableAlias(table);

        // Then
        assertEquals(table.name(), result);
    }

    @Test
    void newColumnAlias() {
        // Given
        final NoOpAliasGenerator noOpAliasGenerator = new NoOpAliasGenerator();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "MY_VAR");

        // When
        final String result = noOpAliasGenerator.newColumnAlias(column);

        // Then
        assertEquals(column.name(), result);

        // When 2
        final String result2 = noOpAliasGenerator.newColumnAlias(column);

        // Then 2
        assertEquals(column.name(), result2);
    }

    private static class TestDto {
        private @Nullable Long id;
        private @Nullable String myVar;
    }
}