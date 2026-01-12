package org.litebridge.tracking;

import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassFieldCacheTest {

    @Test
    void getFields_class() {
        // Given
        final Class<?> cls = TestDto.class;

        // When
        final Set<Field> result = ClassFieldCache.getFields(cls);

        // Then
        assertEquals(5, result.size());
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("string")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("list")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("property")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("nestedDto")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("map")));
    }

    @Test
    void getFields_object() {
        // Given
        final TestDto testDto = new TestDto();

        // When
        final Set<Field> result = ClassFieldCache.getFields(testDto);

        // Then
        assertEquals(5, result.size());
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("string")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("list")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("property")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("nestedDto")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("map")));
    }

    @Test
    void nestedDtoFields() {
        // Given
        final Class<?> cls = TestDto.class;

        // When
        final Set<Field> result = ClassFieldCache.nestedDtoFields(cls);

        // Then
        assertEquals(1, result.size());
        assertEquals("nestedDto", result.iterator().next().getName());
    }

    @Test
    void getGenericType() {
        // Given
        final Field field = ClassUtils.getField(TestDto.class, "list");

        // When
        final Class<?> result = ClassFieldCache.getGenericType(field);

        // Then
        assertEquals(Long.class, result);
    }

    @Test
    void getGenericTypes() {
        // Given
        final Field field = ClassUtils.getField(TestDto.class, "list");

        // When
        final Class<?>[] result = ClassFieldCache.getGenericTypes(field);

        // Then
        assertEquals(1, result.length);
        assertEquals(Long.class, result[0]);
    }

    @NullUnmarked
    private class TestDto {
        private String string;
        private List<Long> list;
        private String property;
        private NestedDto nestedDto;
        private Map<String, Long> map;

        public String getProperty() {
            return property;
        }

        public void setProperty(final String property) {
            this.property = property;
        }
    }

    @NullUnmarked
    private class NestedDto {
        private String string;
    }
}