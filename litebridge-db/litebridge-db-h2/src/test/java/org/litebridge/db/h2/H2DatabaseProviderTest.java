package org.litebridge.db.h2;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class H2DatabaseProviderTest {

    @Test
    void constructor() {
        // Given
        final Connection connection = mock(Connection.class);

        // When
        final H2DatabaseProvider result = new H2DatabaseProvider(connection);

        // Then
        assertNotNull(result);
    }

    @Test
    void transformAlias() {
        // Given
        final Connection connection = mock(Connection.class);
        final H2DatabaseProvider provider = new H2DatabaseProvider(connection);

        // When
        final String result = provider.transformAlias("TEST");

        // Then
        assertEquals("test", result);
    }

    @Test
    void transformAlias_null() {
        // Given
        final Connection connection = mock(Connection.class);
        final H2DatabaseProvider provider = new H2DatabaseProvider(connection);

        // When
        final String result = provider.transformAlias(null);

        // Then
        assertNull(result);
    }
}