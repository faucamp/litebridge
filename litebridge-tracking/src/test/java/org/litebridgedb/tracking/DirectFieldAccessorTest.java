package org.litebridgedb.tracking;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridgedb.commons.ClassUtils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectFieldAccessorTest {

    @Test
    void name() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());

        // When
        final String result = directFieldAccessor.name();

        // Then
        assertEquals("myVar", result);
    }

    @Test
    void get() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final TestDto testDto = new TestDto();
        testDto.myVar = "testValue";

        // When
        final String result = (String) directFieldAccessor.get(testDto);

        // Then
        assertEquals("testValue", result);
    }

    @Test
    void get_exception() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final TestDto2 testDto2 = new TestDto2();

        // When
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> directFieldAccessor.get(testDto2));

        // Then
        assertEquals("DTO class does not match field accessor class", ex.getMessage());
        assertTrue(ex.getCause() instanceof ClassCastException);
    }

    @Test
    void set() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final TestDto testDto = new TestDto();

        // When
        directFieldAccessor.set(testDto, "testValue2");

        // Then
        assertEquals("testValue2", testDto.myVar);
    }

    @Test
    void type() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());

        // When
        final Class<?> result = directFieldAccessor.type();

        // Then
        assertEquals(String.class, result);
    }

    @Test
    void genericTypes() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto2.class, "list"), MethodHandles.lookup());

        // When
        final Class<?>[] result = directFieldAccessor.genericTypes();

        // Then
        assertEquals(1, result.length);
        assertEquals(Long.class, result[0]);
    }

    @Test
    void genericType() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto2.class, "list"), MethodHandles.lookup());

        // When
        final Class<?> result = directFieldAccessor.genericType();

        // Then
        assertEquals(Long.class, result);
    }

    @Test
    void genericType_failure() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto3.class, "map"), MethodHandles.lookup());

        // When/Then
        assertThrows(IllegalStateException.class, () -> directFieldAccessor.genericType());
    }

    @Test
    void dtoClass() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());

        // When
        final Class<?> result = directFieldAccessor.dtoClass();

        // Then
        assertEquals(TestDto.class, result);
    }

    @Test
    void equals_null() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());

        // When/Then
        assertFalse(directFieldAccessor.equals(null));
    }

    @Test
    void equals_differentType() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());

        // When/Then
        assertFalse(directFieldAccessor.equals(new Object()));
    }

    @Test
    void equals_sameFieldInstance() {
        // Given
        final Field field = ClassUtils.getField(TestDto.class, "myVar");
        final DirectFieldAccessor left = new DirectFieldAccessor(field, MethodHandles.lookup());
        final DirectFieldAccessor right = new DirectFieldAccessor(field, MethodHandles.lookup());

        // When/Then
        assertTrue(left.equals(right));
        assertTrue(right.equals(left));
    }

    @Test
    void equals_differentField() {
        // Given
        final DirectFieldAccessor left = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final DirectFieldAccessor right = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "otherVar"), MethodHandles.lookup());

        // When/Then
        assertFalse(left.equals(right));
        assertFalse(right.equals(left));
    }

    @Test
    void hashCode_equalWhenFieldsEqual() {
        // Given
        final Field field = ClassUtils.getField(TestDto.class, "myVar");
        final DirectFieldAccessor left = new DirectFieldAccessor(field, MethodHandles.lookup());
        final DirectFieldAccessor right = new DirectFieldAccessor(field, MethodHandles.lookup());

        // When
        final int leftHash = left.hashCode();
        final int rightHash = right.hashCode();

        // Then
        assertEquals(leftHash, rightHash);
        assertEquals(field.hashCode(), leftHash);
    }

    @Test
    void hashCode_differentWhenFieldsDifferent() {
        // Given
        final DirectFieldAccessor left = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());
        final DirectFieldAccessor right = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "otherVar"), MethodHandles.lookup());

        // When/Then
        assertNotEquals(left.hashCode(), right.hashCode());
    }

    @Test
    void constructor_illegalAccessException() {
        // Given
        final Field field = ClassUtils.getField(TestDto.class, "myVar");

        // When
        final IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new DirectFieldAccessor(field, MethodHandles.publicLookup())
        );

        // Then
        assertTrue(ex.getMessage().contains("Failed to unreflect VarHandle for field: 'myVar'"));
        assertTrue(ex.getMessage().contains(TestDto.class.getName()));
        assertInstanceOf(IllegalAccessException.class, ex.getCause());
    }

    @Test
    void test_toString() {
        // Given
        final DirectFieldAccessor directFieldAccessor = new DirectFieldAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup());

        // When
        final String result = directFieldAccessor.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("DirectFieldAccessor"));
        assertTrue(result.contains("field"));
        assertTrue(result.contains("type"));
        assertTrue(result.contains("dtoClass"));
    }

    private static class TestDto {
        private @Nullable String myVar;
        private @Nullable String otherVar;
    }

    private static class TestDto2 {
        private @Nullable List<Long> list;
    }

    private static class TestDto3 {
        private @Nullable Map<String, Long> map;
    }
}