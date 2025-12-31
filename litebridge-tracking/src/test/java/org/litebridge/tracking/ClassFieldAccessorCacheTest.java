package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassFieldAccessorCacheTest {

    @Test
    void fieldAccessorOrThrow() {
        // When
        final FieldAccessor result = ClassFieldAccessorCache.fieldAccessorOrThrow(TestDto.class, "string");

        // Then
        assertNotNull(result);
    }

    @Test
    void fieldAccessors() {
        // When
        final List<FieldAccessor> result = ClassFieldAccessorCache.fieldAccessors(TestDto.class);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void isNestedDtoField() {
        // Given
        final FieldAccessor fieldAccessor =  new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "nestedDto"));

        // When
        final boolean result = ClassFieldAccessorCache.isNestedDtoField(TestDto.class, fieldAccessor);

        // Then
        assertTrue(result);
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

    @Nullable
    private class TestDto {
        private String string;
        private NestedDto nestedDto;

        public String getString() {
            return string;
        }

        public void setString(final String string) {
            this.string = string;
        }
    }

    @Nullable
    private class NestedDto {
        private String string;
    }
}