package org.litebridge.db.postgres;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PostgresAliasTransformerTest {

    private final PostgresAliasTransformer transformer = new PostgresAliasTransformer();

    @Test
    void transformAlias_withAlias() {
        // When
        final String result = transformer.transformAlias("TEST_ALIAS");

        // Then
        assertEquals("test_alias", result);
    }

    @Test
    void transformAlias_withNullAlias() {
        // When
        final String result = transformer.transformAlias(null);

        // Then
        assertNull(result);
    }
}