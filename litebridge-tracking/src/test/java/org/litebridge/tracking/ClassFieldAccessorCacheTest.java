package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassFieldAccessorCacheTest {

    private ClassFieldAccessorCache classFieldAccessorCache;

    @BeforeEach
    void beforeEach() {
        classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
    }

    @Test
    void fieldAccessorOrThrow() {
        // When
        final FieldAccessor result = classFieldAccessorCache.fieldAccessorOrThrow(TestDto.class, "string");

        // Then
        assertNotNull(result);

        // When 2
        final FieldAccessor result2 = classFieldAccessorCache.fieldAccessorOrThrow(TestDto.class, "string");

        // Then 2
        assertEquals(result, result2);
    }

    @Test
    void fieldAccessorOrThrow_invalidField() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> classFieldAccessorCache.fieldAccessorOrThrow(TestDto.class, "invalid"));
    }

    @Test
    void fieldAccessorOrThrow_dotDelimitedPath() {
        // When
        final FieldAccessor result = classFieldAccessorCache.fieldAccessorOrThrow(TestDto.class, "nestedDto.secondNestedDto.thirdNestedDto.string");

        // Then
        assertNotNull(result);
        assertEquals(String.class, result.type());
        assertEquals(ThirdNestedDto.class, result.dtoClass());
        assertInstanceOf(FieldAccessorChain.class, result);

        final FieldAccessorChain fieldAccessorChain = (FieldAccessorChain) result;
        assertEquals("nestedDto.secondNestedDto.thirdNestedDto.string", fieldAccessorChain.fieldPath());
        assertEquals(4, fieldAccessorChain.fieldAccessors().size());

        assertEquals(NestedDto.class, fieldAccessorChain.fieldAccessors().getFirst().type());
        assertEquals(TestDto.class, fieldAccessorChain.fieldAccessors().getFirst().dtoClass());

        assertEquals(SecondNestedDto.class, fieldAccessorChain.fieldAccessors().get(1).type());
        assertEquals(NestedDto.class, fieldAccessorChain.fieldAccessors().get(1).dtoClass());

        assertEquals(ThirdNestedDto.class, fieldAccessorChain.fieldAccessors().get(2).type());
        assertEquals(SecondNestedDto.class, fieldAccessorChain.fieldAccessors().get(2).dtoClass());
    }

    @Test
    void fieldAccessor_dotDelimitedPath() {
        // When
        final FieldAccessor result = classFieldAccessorCache.fieldAccessor(TestDto.class, "nestedDto.secondNestedDto");

        // Then
        assertNotNull(result);
        assertEquals(SecondNestedDto.class, result.type());
        assertEquals(NestedDto.class, result.dtoClass());
        assertInstanceOf(FieldAccessorChain.class, result);

        final FieldAccessorChain fieldAccessorChain = (FieldAccessorChain) result;
        assertEquals("nestedDto.secondNestedDto", fieldAccessorChain.fieldPath());
        assertEquals(2, fieldAccessorChain.fieldAccessors().size());

        assertEquals(NestedDto.class, fieldAccessorChain.fieldAccessors().getFirst().type());
        assertEquals(TestDto.class, fieldAccessorChain.fieldAccessors().getFirst().dtoClass());
    }

    @Test
    void fieldAccessors() {
        // When
        final List<FieldAccessor> result = classFieldAccessorCache.fieldAccessors(TestDto.class);

        // Then
        assertEquals(4, result.size());

        // When 2
        final List<FieldAccessor> result2 = classFieldAccessorCache.fieldAccessors(TestDto.class);

        // Then 2
        assertEquals(result, result2);
    }

    @Test
    void fieldAccessors_privateLookupCreationFails() {
        // Given
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.publicLookup());

        // When
        final IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cache.fieldAccessors(ArrayList.class)
        );

        // Then
        assertTrue(ex.getMessage().contains("Cannot create private lookup for declaring class: java.util.ArrayList"));
        assertTrue(ex.getMessage().contains("while building accessors for DTO: java.util.ArrayList"));
        assertNotNull(ex.getCause());
        assertInstanceOf(IllegalAccessException.class, ex.getCause());
    }

    @Test
    void isNestedDtoField_true() {
        // Given
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessor(TestDto.class, "nestedDto");

        // When
        final boolean result = classFieldAccessorCache.isNestedDtoField(TestDto.class, fieldAccessor);

        // Then
        assertTrue(result);
    }

    @Test
    void isNestedDtoField_false_basicType() {
        // Given
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessor(TestDto.class, "string");

        // When
        final boolean result = classFieldAccessorCache.isNestedDtoField(TestDto.class, fieldAccessor);

        // Then
        assertFalse(result);
    }

    @Test
    void isNestedDtoField_false_list() {
        // Given
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessor(TestDto.class, "list");

        // When
        final boolean result = classFieldAccessorCache.isNestedDtoField(TestDto.class, fieldAccessor);

        // Then
        assertFalse(result);
    }

    @Test
    void isNestedDtoField_false_map() {
        // Given
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessor(TestDto.class, "map");

        // When
        final boolean result = classFieldAccessorCache.isNestedDtoField(TestDto.class, fieldAccessor);

        // Then
        assertFalse(result);
    }

    @Test
    void fieldAccessor() {
        // When
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessor(TestDto.class, "string");

        // Then
        assertNotNull(fieldAccessor);
    }

    @Test
    void propertyAccessor() {
        // When
        final FieldAccessor fieldAccessor = classFieldAccessorCache.propertyAccessor(TestDto.class, "string");

        // Then
        assertNotNull(fieldAccessor);
    }

    @Test
    void fieldAccessor_canAccessPrivateInheritedField() {
        // Given
        final ClassFieldAccessorCache cache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final ChildDto dto = new ChildDto();

        // When
        final FieldAccessor ageAccessor = cache.fieldAccessorOrThrow(ChildDto.class, "age");
        ageAccessor.set(dto, 42);

        // Then
        assertNotNull(ageAccessor);
        assertEquals(42, ageAccessor.get(dto));
        assertEquals(ParentDto.class, ageAccessor.dtoClass());
        assertEquals(int.class, ageAccessor.type());
    }

    private static class ParentDto {
        private int age;
    }

    private static class ChildDto extends ParentDto {
        private String name;
    }

    private static class TestDto {
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

    private static class NestedDto {
        @Nullable
        private String string;

        @Nullable
        private SecondNestedDto secondNestedDto;
    }

    private static class SecondNestedDto {
        @Nullable
        private String string;
        @Nullable
        private ThirdNestedDto thirdNestedDto;
    }

    private static class ThirdNestedDto {
        @Nullable
        private String string;
    }
}