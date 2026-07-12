package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.alias.DefaultAliasTransformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAliasTransformerTest {

    private final DefaultAliasTransformer transformer = new DefaultAliasTransformer();

    @Test
    void transformAlias() {
        // Given
        final String alias = "TestAlias";

        // When
        final String result = transformer.transformAlias(alias);

        // Then
        assertEquals(alias, result);
    }
}