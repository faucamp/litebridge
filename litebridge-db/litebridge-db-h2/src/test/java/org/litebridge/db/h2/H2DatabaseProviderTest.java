package org.litebridge.db.h2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class H2DatabaseProviderTest {

    @Test
    void getLogger() {
        final H2DatabaseProvider h2DatabaseProvider = new H2DatabaseProvider();
        assertNotNull(h2DatabaseProvider.getLogger());
    }
}