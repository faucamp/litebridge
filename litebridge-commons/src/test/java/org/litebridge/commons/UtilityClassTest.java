package org.litebridge.commons;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UtilityClassTest {

    @Test
    void testPrivateConstructors() throws Exception {
        assertPrivateConstructor(StringUtils.class);
        assertPrivateConstructor(MapUtils.class);
        assertPrivateConstructor(ClassUtils.class);
        assertPrivateConstructor(CollectionUtils.class);
        assertPrivateConstructor(TimeUtils.class);
        assertPrivateConstructor(ObjectUtils.class);
        assertPrivateConstructor(BooleanUtils.class);
        assertPrivateConstructor(ModuleUtils.class);
    }

    private void assertPrivateConstructor(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
