package org.litebridge.orm.meta;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QFInspectorTest {

    @Test
    void testGetFieldName() {
        // Given
        final QueryField field = new QueryField(TestDto.class, "name");

        // Then
        assertEquals("name", QFInspector.getFieldName(field));
    }

    @Test
    void testGetDtoClass() {
        // Given
        final QueryField field = new QueryField(TestDto.class, "name");

        // Then
        assertEquals(TestDto.class, QFInspector.getDtoClass(field));
    }

    @Test
    void testPrivateConstructor() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Given
        final Constructor<QFInspector> constructor = QFInspector.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));

        // When
        constructor.setAccessible(true);
        final QFInspector instance = constructor.newInstance();

        // Then
        assertNotNull(instance);
    }
}
