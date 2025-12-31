package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassFieldAccessorCacheTest {

    @AfterEach
    void afterEach() {
        ClassFieldAccessorCache.clear();
    }

    @Test
    void fieldAccessorOrThrow() {
        // When
        final FieldAccessor result = ClassFieldAccessorCache.fieldAccessorOrThrow(TestDto.class, "string");

        // Then
        assertNotNull(result);

        // When 2
        final FieldAccessor result2 = ClassFieldAccessorCache.fieldAccessorOrThrow(TestDto.class, "string");

        // Then 2
        assertEquals(result, result2);
    }

    @Test
    void fieldAccessorOrThrow_invalidField() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassFieldAccessorCache.fieldAccessorOrThrow(TestDto.class, "invalid"));
    }

    @Test
    void fieldAccessors() {
        // When
        final List<FieldAccessor> result = ClassFieldAccessorCache.fieldAccessors(TestDto.class);

        // Then
        assertEquals(4, result.size());

        // When 2
        final List<FieldAccessor> result2 = ClassFieldAccessorCache.fieldAccessors(TestDto.class);

        // Then 2
        assertEquals(result, result2);
    }

    @Test
    void isNestedDtoField_true() {
        // Given
        final FieldAccessor fieldAccessor = ClassFieldAccessorCache.fieldAccessor(TestDto.class, "nestedDto");

        // When
        final boolean result = ClassFieldAccessorCache.isNestedDtoField(TestDto.class, fieldAccessor);

        // Then
        assertTrue(result);
    }

    @Test
    void isNestedDtoField_false_basicType() {
        // Given
        final FieldAccessor fieldAccessor = ClassFieldAccessorCache.fieldAccessor(TestDto.class, "string");

        // When
        final boolean result = ClassFieldAccessorCache.isNestedDtoField(TestDto.class, fieldAccessor);

        // Then
        assertFalse(result);
    }

    @Test
    void isNestedDtoField_false_list() {
        // Given
        final FieldAccessor fieldAccessor = ClassFieldAccessorCache.fieldAccessor(TestDto.class, "list");

        // When
        final boolean result = ClassFieldAccessorCache.isNestedDtoField(TestDto.class, fieldAccessor);

        // Then
        assertFalse(result);
    }

    @Test
    void isNestedDtoField_false_map() {
        // Given
        final FieldAccessor fieldAccessor = ClassFieldAccessorCache.fieldAccessor(TestDto.class, "map");

        // When
        final boolean result = ClassFieldAccessorCache.isNestedDtoField(TestDto.class, fieldAccessor);

        // Then
        assertFalse(result);
    }

    @Test
    void isNestedDtoField_false_null() {
        // When
        final boolean result = ClassFieldAccessorCache.isNestedDtoField(TestDto.class, null);

        // Then
        assertFalse(result);
    }

    @Test
    void fieldAccessor() {
        // When
        final FieldAccessor fieldAccessor = ClassFieldAccessorCache.fieldAccessor(TestDto.class, "string");

        // Then
        assertNotNull(fieldAccessor);
    }

    @Test
    void propertyAccessor() {
        // When
        final FieldAccessor fieldAccessor = ClassFieldAccessorCache.propertyAccessor(TestDto.class, "string");

        // Then
        assertNotNull(fieldAccessor);
    }

    @Test
    void constructor() {
        final TestClassFieldAccessorCache result = new TestClassFieldAccessorCache();
        assertNotNull(result);
    }

    private class TestDto {
        @Nullable
        private String string;
        @Nullable
        private NestedDto nestedDto;
        @Nullable
        private List<String> list;
        @Nullable
        private Map<Long, String> map;

        public @Nullable String getString() {
            return string;
        }

        public void setString(final String string) {
            this.string = string;
        }
    }

    private class NestedDto {
        @Nullable
        private String string;
    }

    class TestClassFieldAccessorCache extends ClassFieldAccessorCache {
    }
}