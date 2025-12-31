package org.litebridge.db.h2;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class H2DatabaseProviderTest {

    @Test
    void test() {
        final Connection connection = mock(Connection.class);
        final H2DatabaseProvider provider = new H2DatabaseProvider(connection);
        assertNotNull(provider);
    }
}