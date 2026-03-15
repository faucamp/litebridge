package org.litebridge.db.h2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class H2DatabaseProviderTest {

    @Test
    void transformAlias() {
        // Given
        final H2DatabaseProvider provider = new H2DatabaseProvider();

        // When
        final String result = provider.transformAlias("TEST");

        // Then
        assertEquals("test", result);
    }

    @Test
    void transformAlias_null() {
        // Given
        final H2DatabaseProvider provider = new H2DatabaseProvider();

        // When
        final String result = provider.transformAlias(null);

        // Then
        assertNull(result);
    }
}