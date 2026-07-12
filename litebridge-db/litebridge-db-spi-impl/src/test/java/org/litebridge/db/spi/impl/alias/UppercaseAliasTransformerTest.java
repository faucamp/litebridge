package org.litebridge.db.spi.impl.alias;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UppercaseAliasTransformerTest {

    private final UppercaseAliasTransformer transformer = new UppercaseAliasTransformer();

    @Test
    void transformAlias() {
        // Given
        final String dbAlias = "TEST_ALIAS";

        // When
        final String result = transformer.transformAlias(dbAlias);

        // Then
        assertEquals("TEST_ALIAS", result);
    }

    @Test
    void transformAlias_null() {
        // Given
        final String dbAlias = null;

        // When
        final String result = transformer.transformAlias(dbAlias);

        // Then
        assertNull(result);
    }
}