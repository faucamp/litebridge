package org.litebridge.spring.boot.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void getScanBasePackage() {
        final LitebridgeProperties litebridgeProperties = new LitebridgeProperties();
        assertNull(litebridgeProperties.getScanBasePackage());
        litebridgeProperties.setScanBasePackage(new String[]{"com.example"});
        assertArrayEquals(new String[]{"com.example"}, litebridgeProperties.getScanBasePackage());
    }

    @Test
    void getSetRelatedDtoStrategy() {
        final LitebridgeProperties litebridgeProperties = new LitebridgeProperties();
        assertEquals(org.litebridge.orm.config.RelatedDtoStrategy.NULL_IF_NO_JOIN, litebridgeProperties.getRelatedDtoStrategy());
        litebridgeProperties.setRelatedDtoStrategy(org.litebridge.orm.config.RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);
        assertEquals(org.litebridge.orm.config.RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN, litebridgeProperties.getRelatedDtoStrategy());
    }

    @Test
    void getSetDatabaseProviderScanBasePackage() {
        final LitebridgeProperties.DatabaseProviderProperties properties = new LitebridgeProperties.DatabaseProviderProperties();
        assertEquals("org.litebridge.db", properties.getScanBasePackage());
        properties.setScanBasePackage("com.example");
        assertEquals("com.example", properties.getScanBasePackage());
    }

    @Test
    void setDatabaseProviderClassAlias() {
        final LitebridgeProperties.DatabaseProviderProperties properties = new LitebridgeProperties.DatabaseProviderProperties();
        properties.setClass("org.litebridge.db.h2.H2DatabaseProvider");
        assertEquals("org.litebridge.db.h2.H2DatabaseProvider", properties.getProviderClass());
    }
}