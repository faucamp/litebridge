package org.litebridge.tracking;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyAccessorTest {

    @Test
    void name() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);

        // When
        final String result = propertyAccessor.name();

        // Then
        assertEquals("myVar", result);
    }

    @Test
    void get() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);
        final TestDto testDto = new TestDto();
        testDto.setMyVar("testValue");

        // When
        final String result = (String) propertyAccessor.get(testDto);

        // Then
        assertEquals("testValue", result);
    }

    @Test
    void get_exception() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);
        final TestDto2 testDto2 = new TestDto2();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> propertyAccessor.get(testDto2));
    }

    @Test
    void set() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);
        final TestDto testDto = new TestDto();

        // When
        propertyAccessor.set(testDto, "testValue2");

        // Then
        assertEquals("testValue2", testDto.getMyVar());
    }

    @Test
    void set_exception() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);
        final TestDto2 testDto2 = new TestDto2();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> propertyAccessor.set(testDto2, "invalidSetterValue"));
    }

    @Test
    void type() {
        // When
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);
        final Class<?> result = propertyAccessor.type();

        // Then
        assertEquals(String.class, result);
    }

    @Test
    void genericTypes() throws Exception {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto2.class, "list"), MethodHandles.lookup(), classFieldAccessorCache);

        // When
        final Class<?>[] result = propertyAccessor.genericTypes();

        // Then
        assertEquals(1, result.length);
        assertEquals(Long.class, result[0]);
    }

    @Test
    void dtoClass() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);

        // When
        final Class<?> result = propertyAccessor.dtoClass();

        // Then
        assertEquals(TestDto.class, result);
    }

    @Test
    void equals_null() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);

        // When/Then
        assertFalse(propertyAccessor.equals(null));
    }

    @Test
    void equals_differentType() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor propertyAccessor = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);

        // When/Then
        assertFalse(propertyAccessor.equals(new Object()));
    }

    @Test
    void equals_samePropertyDescriptorInstance() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final Field field = ClassUtils.getField(TestDto.class, "myVar");
        final PropertyAccessor left = new PropertyAccessor(field, MethodHandles.lookup(), classFieldAccessorCache);
        final PropertyAccessor right = new PropertyAccessor(field, MethodHandles.lookup(), classFieldAccessorCache);

        // When/Then
        assertTrue(left.equals(right));
        assertTrue(right.equals(left));
    }

    @Test
    void equals_differentPropertyDescriptor() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor left = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);
        final PropertyAccessor right = new PropertyAccessor(ClassUtils.getField(TestDto.class, "otherVar"), MethodHandles.lookup(), classFieldAccessorCache);

        // When/Then
        assertFalse(left.equals(right));
        assertFalse(right.equals(left));
    }

    @Test
    void hashCode_equalWhenDescriptorsEqual() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final Field field = ClassUtils.getField(TestDto.class, "myVar");
        final PropertyAccessor left = new PropertyAccessor(field, MethodHandles.lookup(), classFieldAccessorCache);
        final PropertyAccessor right = new PropertyAccessor(field, MethodHandles.lookup(), classFieldAccessorCache);

        // When
        final int leftHash = left.hashCode();
        final int rightHash = right.hashCode();

        // Then
        assertEquals(leftHash, rightHash);
        assertEquals(field.hashCode(), leftHash);
    }

    @Test
    void hashCode_differentWhenDescriptorsDifferent() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final PropertyAccessor left = new PropertyAccessor(ClassUtils.getField(TestDto.class, "myVar"), MethodHandles.lookup(), classFieldAccessorCache);
        final PropertyAccessor right = new PropertyAccessor(ClassUtils.getField(TestDto.class, "otherVar"), MethodHandles.lookup(), classFieldAccessorCache);

        // When/Then
        assertNotEquals(left.hashCode(), right.hashCode());
    }

    @Test
    void constructor_illegalAccessException() {
        // Given
        final ClassFieldAccessorCache classFieldAccessorCache = new ClassFieldAccessorCache(MethodHandles.lookup());
        final Field field = ClassUtils.getField(TestDto.class, "myVar");

        // When
        final IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new PropertyAccessor(field, MethodHandles.publicLookup(), classFieldAccessorCache)
        );

        // Then
        assertTrue(ex.getMessage().contains("Failed to unreflect getter and setter for field: 'myVar'"));
        assertTrue(ex.getMessage().contains(TestDto.class.getName()));
        assertTrue(ex.getCause() instanceof IllegalAccessException);
    }

    private class TestDto {
        private String myVar;
        private String otherVar;

        public String getMyVar() {
            return myVar;
        }

        public void setMyVar(final String myVar) {
            this.myVar = myVar;
        }

        public String getOtherVar() {
            return otherVar;
        }

        public void setOtherVar(final String otherVar) {
            this.otherVar = otherVar;
        }
    }

    private class TestDto2 {
        private List<Long> list;

        public List<Long> getList() {
            return list;
        }

        public void setList(final List<Long> list) {
            this.list = list;
        }
    }
}