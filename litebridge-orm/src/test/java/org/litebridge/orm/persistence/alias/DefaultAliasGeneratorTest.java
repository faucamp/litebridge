package org.litebridge.orm.persistence.alias;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.alias.DefaultAliasTransformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAliasGeneratorTest {

    @Test
    void newTableAlias() {
        // Given
        final DefaultAliasGenerator defaultAliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        // When
        final String result = defaultAliasGenerator.newTableAlias(table);

        // Then
        assertEquals("tt", result);
    }

    @Test
    void newColumnAlias() {
        // Given
        final DefaultAliasGenerator defaultAliasGenerator = new DefaultAliasGenerator(new DefaultAliasTransformer());
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "MY_VAR");

        // When
        final String result = defaultAliasGenerator.newColumnAlias(column);

        // Then
        assertEquals("ttmv", result);

        // When 2
        final String result2 = defaultAliasGenerator.newColumnAlias(column);

        // Then 2
        assertEquals("ttmv1", result2);
    }

    private static class TestDto {
        private @Nullable Long id;
        private @Nullable String myVar;
    }
}