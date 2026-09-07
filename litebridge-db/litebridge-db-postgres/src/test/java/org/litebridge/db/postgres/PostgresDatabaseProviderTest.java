package org.litebridge.db.postgres;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PostgresDatabaseProviderTest {

    @Test
    void constructor() {
        // When
        final PostgresDatabaseProvider databaseProvider = new PostgresDatabaseProvider();

        // Then
        assertNotNull(databaseProvider);
    }
}
