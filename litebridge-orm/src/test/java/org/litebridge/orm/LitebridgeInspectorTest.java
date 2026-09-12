package org.litebridge.orm;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.engine.QueryPlanCache;

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
