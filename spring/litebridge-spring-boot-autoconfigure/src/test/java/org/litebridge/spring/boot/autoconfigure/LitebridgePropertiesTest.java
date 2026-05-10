package org.litebridge.spring.boot.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LitebridgePropertiesTest {

    @Test
    void getSetDatabaseProviderClass() {
        final LitebridgeProperties litebridgeProperties = new LitebridgeProperties();
        assertNull(litebridgeProperties.getDatabaseProvider().getProviderClass());
        litebridgeProperties.getDatabaseProvider().setProviderClass("org.litebridge.db.h2.H2DatabaseProvider");
        assertEquals("org.litebridge.db.h2.H2DatabaseProvider", litebridgeProperties.getDatabaseProvider().getProviderClass());
    }

    @Test
    void getSetDatabaseProviderScanBasePackages() {
        final LitebridgeProperties litebridgeProperties = new LitebridgeProperties();
        assertNull(litebridgeProperties.getDatabaseProvider().getProviderClass());
        litebridgeProperties.getDatabaseProvider().setScanBasePackage("com.example");
        assertEquals("com.example", litebridgeProperties.getDatabaseProvider().getScanBasePackage());
    }

    @Test
    void getSetDatabaseProvider() {
        final LitebridgeProperties litebridgeProperties = new LitebridgeProperties();
        assertNotNull(litebridgeProperties.getDatabaseProvider());
        final LitebridgeProperties.DatabaseProviderProperties databaseProviderProperties = new LitebridgeProperties.DatabaseProviderProperties();
        litebridgeProperties.setDatabaseProvider(databaseProviderProperties);
        assertEquals(databaseProviderProperties, litebridgeProperties.getDatabaseProvider());
    }
}