package org.litebridge.orm;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LitebridgeInspectorTest {

    @Test
    void testGetQueryPlanCache() {
        // Given
        final LitebridgeCore litebridge = mock(LitebridgeCore.class);
        final QueryPlanCache expectedCache = new QueryPlanCache();
        when(litebridge.queryPlanCache()).thenReturn(expectedCache);

        // When
        final QueryPlanCache actualCache = LitebridgeInspector.getQueryPlanCache(litebridge);

        // Then
        assertSame(expectedCache, actualCache);
    }

    @Test
    void testGetTableRegistry() {
        // Given
        final LitebridgeCore litebridge = mock(LitebridgeCore.class);
        final TableRegistry expectedRegistry = mock(TableRegistry.class);
        when(litebridge.tableRegistry()).thenReturn(expectedRegistry);

        // When
        final TableRegistry actualRegistry = LitebridgeInspector.getTableRegistry(litebridge);

        // Then
        assertSame(expectedRegistry, actualRegistry);
    }

    @Test
    void testGetDatabaseProvider() {
        // Given
        final LitebridgeCore litebridge = mock(LitebridgeCore.class);
        final DatabaseProvider expectedProvider = mock(DatabaseProvider.class);
        when(litebridge.databaseProvider()).thenReturn(expectedProvider);

        // When
        final DatabaseProvider actualProvider = LitebridgeInspector.getDatabaseProvider(litebridge);

        // Then
        assertSame(expectedProvider, actualProvider);
    }

    @Test
    void testPrivateConstructor() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Given
        final Constructor<LitebridgeInspector> constructor = LitebridgeInspector.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));

        // When
        constructor.setAccessible(true);
        final LitebridgeInspector instance = constructor.newInstance();

        // Then
        assertNotNull(instance);
    }
}
