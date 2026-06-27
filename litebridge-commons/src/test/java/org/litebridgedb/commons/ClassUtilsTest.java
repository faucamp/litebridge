package org.litebridgedb.commons;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassUtilsTest {

    @Test
    void getAllFields_illegalAccess() {
        // Given
        final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getAllFields(TestDto.class, lookup));
    }

    @Test
    void getAllFields() {
        // When
        final List<Field> result = ClassUtils.getAllFields(TestDto.class, MethodHandles.lookup());

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("name")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("age")));
    }

    @Test
    void getAllFields_includeStatic() {
        // When
        final List<Field> result = ClassUtils.getAllFields(TestDto.class, true, MethodHandles.lookup());

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("staticField")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("name")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("age")));
    }

    @Test
    void getAllFields_inheritance() {
        // When
        final List<Field> result = ClassUtils.getAllFields(ChildTestDto.class, MethodHandles.lookup());

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("active")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("name")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("age")));
    }

    @Test
    void getAllFields_inheritance_includeStatic() {
        // When
        final List<Field> result = ClassUtils.getAllFields(ChildTestDto.class, true, MethodHandles.lookup());

        // Then
        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("active")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("staticField")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("name")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("age")));
    }

    @Test
    void getAllMethods() {
        // When
        final List<Method> result = ClassUtils.getAllMethods(TestDto.class, false, MethodHandles.lookup());

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("getName")));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("setName")));
        assertFalse(result.stream().anyMatch(method -> method.getName().equals("staticMethod")));
        assertFalse(result.stream().anyMatch(method -> method.getDeclaringClass().equals(Object.class)));
    }

    @Test
    void getAllMethods_includeStatic() {
        // When
        final List<Method> result = ClassUtils.getAllMethods(TestDto.class, true, MethodHandles.lookup());

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("getName")));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("setName")));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("staticMethod")));
        assertFalse(result.stream().anyMatch(method -> method.getDeclaringClass().equals(Object.class)));
    }

    @Test
    void getAllMethods_inheritance() {
        // When
        final List<Method> result = ClassUtils.getAllMethods(ChildTestDto.class, false, MethodHandles.lookup());

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("isActive")));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("getName")));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("setName")));
        assertFalse(result.stream().anyMatch(method -> method.getName().equals("staticMethod")));
        assertFalse(result.stream().anyMatch(method -> method.getName().equals("childStaticMethod")));
        assertFalse(result.stream().anyMatch(method -> method.getDeclaringClass().equals(Object.class)));
    }

    @Test
    void getAllMethods_inheritance_includeStatic() {
        // When
        final List<Method> result = ClassUtils.getAllMethods(ChildTestDto.class, true, MethodHandles.lookup());

        // Then
        assertEquals(5, result.size());
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("isActive")));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("childStaticMethod")));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("getName")));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("setName")));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("staticMethod")));
        assertFalse(result.stream().anyMatch(method -> method.getDeclaringClass().equals(Object.class)));
    }

    @Test
    void getAllMethods_excludesSyntheticBridgeMethods() {
        // When
        final List<Method> result = ClassUtils.getAllMethods(StringChild.class, false, MethodHandles.lookup());

        // Then
        assertTrue(result.stream().noneMatch(Method::isSynthetic));
        assertTrue(result.stream().anyMatch(method -> method.getName().equals("value") && method.getReturnType().equals(String.class)));
    }

    @Test
    void getAllMethods_illegalAccess() {
        // Given
        final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getAllMethods(TestDto.class, false, lookup));
    }

    @Test
    void getField() {
        // Given
        final String fieldName = "age";

        // When
        final Field field = ClassUtils.getField(TestDto.class, fieldName);

        // Then
        assertNotNull(field);
        assertEquals(fieldName, field.getName());
    }

    @Test
    void getField_notFound() {
        // Given
        final String fieldName = "nonExistingField";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getField(TestDto.class, fieldName));
    }

    @Test
    void getField_inheritedField() {
        // Given
        final String fieldName = "age";

        // When
        final Field field = ClassUtils.getField(ChildTestDto.class, fieldName);

        // Then
        assertNotNull(field);
        assertEquals(fieldName, field.getName());
    }

    @Test
    void getField_inheritance_notFound() {
        // Given
        final String fieldName = "nonExistingField";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getField(ChildTestDto.class, fieldName));
    }

    @Test
    void isBasicType_String() {
        // Given
        final String object = "Hello World!";

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_StringBuilder() {
        // Given
        final StringBuilder object = new StringBuilder("Hello World!");

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Short() {
        // Given
        final Short object = Short.valueOf((short) 1);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Integer() {
        // Given
        final Integer object = Integer.valueOf(1);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Long() {
        // Given
        final Long object = Long.valueOf(1);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_BigDecimal() {
        // Given
        final BigDecimal object = BigDecimal.valueOf(1);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Double() {
        // Given
        final Double object = Double.valueOf(1.234);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Float() {
        // Given
        final Float object = Float.valueOf(1.234f);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Enum() {
        // When
        final boolean result = ClassUtils.isBasicType(TestEnum.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Boolean() {
        // Given
        final Boolean object = Boolean.TRUE;

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_byteArray() {
        // Given
        final byte[] byteArray = new byte[1];

        // When
        final boolean result = ClassUtils.isBasicType(byteArray.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_stringArray_false() {
        // Given
        final String[] stringArray = new String[1];

        // When
        final boolean result = ClassUtils.isBasicType(stringArray.getClass());

        // Then
        assertFalse(result);
    }

    @Test
    void isBasicType_int() {
        // When
        final boolean result = ClassUtils.isBasicType(int.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_long() {
        // When
        final boolean result = ClassUtils.isBasicType(long.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_double() {
        // When
        final boolean result = ClassUtils.isBasicType(double.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_float() {
        // When
        final boolean result = ClassUtils.isBasicType(float.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_byte() {
        // When
        final boolean result = ClassUtils.isBasicType(byte.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_false() {
        // Given
        final Object object = new Object();

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertFalse(result);
    }

    @Test
    void getGenericType() {
        // Given
        final Field listField = ClassUtils.getField(TestDtoWithList.class, "list");

        // When
        final Class<?> result = ClassUtils.getGenericType(listField);

        // Then
        assertEquals(String.class, result);
    }

    @Test
    void getGenericType_nonGenericField() {
        // Given
        final Field listField = ClassUtils.getField(TestDtoWithList.class, "rawList");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getGenericType(listField));
    }

    @Test
    void getGenericType_basicField() {
        // Given
        final Field listField = ClassUtils.getField(TestDto.class, "name");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getGenericType(listField));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGenericTypes() {
        // Given
        final Field mapField = ClassUtils.getField(TestDtoWithMap.class, "map");

        // When
        final Class<?>[] result = ClassUtils.getGenericTypes(mapField);

        // Then
        assertEquals(2, result.length);
        assertEquals(String.class, result[0]);
        assertEquals(Long.class, result[1]);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGenericTypes_parameterizedTypes() {
        // Given
        final Field mapField = ClassUtils.getField(TestDtoWithMap.class, "mapOfLists");

        // When
        final Class<?>[] result = ClassUtils.getGenericTypes(mapField);

        // Then
        assertEquals(2, result.length);
        assertEquals(String.class, result[0]);
        assertEquals(List.class, result[1]);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGenericTypes_wildcardTypes() {
        // Given
        final Field mapField = ClassUtils.getField(TestDtoWithMap.class, "mapOfWildcards");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getGenericTypes(mapField));
    }

    @Test
    void getGenericTypes_typeVariable() {
        // Given
        final Field genericField = ClassUtils.getField(GenericTestDto.class, "value");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getGenericTypes(genericField));
    }

    @Test
    void newInstance() {
        // When
        final TestDto result = ClassUtils.newInstance(TestDto.class);

        // Then
        assertNotNull(result);
    }

    @Test
    void newInstance_failure() {
        assertThrows(IllegalStateException.class, () -> ClassUtils.newInstance(UnsupportedConstructorClass.class));
    }

    @Test
    void newInstance_withConstructor() throws NoSuchMethodException {
        // Given
        final java.lang.reflect.Constructor<TestDto> constructor = TestDto.class.getDeclaredConstructor();

        // When
        final TestDto result = ClassUtils.newInstance(TestDto.class, constructor);

        // Then
        assertNotNull(result);
    }

    @Test
    void newInstance_withConstructorAndArguments() throws NoSuchMethodException {
        // Given
        final java.lang.reflect.Constructor<ConstructorArgumentClass> constructor = ConstructorArgumentClass.class.getDeclaredConstructor(String.class);

        // When
        final ConstructorArgumentClass result = ClassUtils.newInstance(ConstructorArgumentClass.class, constructor, "test-value");

        // Then
        assertNotNull(result);
        assertEquals("test-value", result.value);
    }

    @Test
    void newInstance_withConstructor_failure() throws NoSuchMethodException {
        // Given
        final java.lang.reflect.Constructor<UnsupportedConstructorClass> constructor = UnsupportedConstructorClass.class.getDeclaredConstructor(String.class);

        // When/Then
        // Passing wrong number of arguments to force failure
        assertThrows(IllegalStateException.class, () -> ClassUtils.newInstance(UnsupportedConstructorClass.class, constructor, "too", "many", "args"));
    }

    @Test
    void newInstance_withCollectionInterface() {
        // When
        final Collection<?> result = ClassUtils.newInstance(Collection.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(ArrayList.class, result);
    }

    @Test
    void newInstance_withListInterface() {
        // When
        final List<?> result = ClassUtils.newInstance(List.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(ArrayList.class, result);
    }

    @Test
    void newInstance_withSetInterface() {
        // When
        final Set<?> result = ClassUtils.newInstance(Set.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(HashSet.class, result);
    }

    @Test
    void newInstance_withSortedSetInterface() {
        // When
        final SortedSet<?> result = ClassUtils.newInstance(SortedSet.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(TreeSet.class, result);
    }

    @Test
    void newInstance_withNavigableSetInterface() {
        // When
        final NavigableSet<?> result = ClassUtils.newInstance(NavigableSet.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(TreeSet.class, result);
    }

    @Test
    void newInstance_withQueueInterface() {
        // When
        final Queue<?> result = ClassUtils.newInstance(Queue.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(ArrayDeque.class, result);
    }

    @Test
    void newInstance_withDequeInterface() {
        // When
        final Deque<?> result = ClassUtils.newInstance(Deque.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(ArrayDeque.class, result);
    }

    @Test
    void newInstance_withConcreteCollectionClass() {
        // When
        final CustomList<?> result = ClassUtils.newInstance(CustomList.class);

        // Then
        assertNotNull(result);
        assertInstanceOf(CustomList.class, result);
    }

    @Test
    void newInstance_withConcreteCollectionClassWithoutNoArgsConstructor_failure() {
        // When/Then
        assertThrows(IllegalStateException.class, () -> ClassUtils.newInstance(CustomListWithoutNoArgsConstructor.class));
    }

    @Test
    void getConstructors() {
        // When
        final java.lang.reflect.Constructor<TestDto>[] result = ClassUtils.getConstructors(TestDto.class);

        // Then
        assertEquals(1, result.length);
    }

    @Test
    void getConstructors_withCollection() {
        // When
        final java.lang.reflect.Constructor<List>[] result = ClassUtils.getConstructors(List.class);

        // Then
        assertTrue(result.length > 0);
    }

    @Test
    void getConstructors_withCollectionInterface() {
        // When
        final java.lang.reflect.Constructor<Collection>[] result = ClassUtils.getConstructors(Collection.class);

        // Then
        assertTrue(result.length > 0);
    }

    @Test
    void getConstructors_withSetInterface() {
        // When
        final java.lang.reflect.Constructor<Set>[] result = ClassUtils.getConstructors(Set.class);

        // Then
        assertTrue(result.length > 0);
    }

    @Test
    void getConstructors_withSortedSetInterface() {
        // When
        final java.lang.reflect.Constructor<SortedSet>[] result = ClassUtils.getConstructors(SortedSet.class);

        // Then
        assertTrue(result.length > 0);
    }

    @Test
    void getConstructors_withNavigableSetInterface() {
        // When
        final java.lang.reflect.Constructor<NavigableSet>[] result = ClassUtils.getConstructors(NavigableSet.class);

        // Then
        assertTrue(result.length > 0);
    }

    @Test
    void getConstructors_withQueueInterface() {
        // When
        final java.lang.reflect.Constructor<Queue>[] result = ClassUtils.getConstructors(Queue.class);

        // Then
        assertTrue(result.length > 0);
    }

    @Test
    void getConstructors_withDequeInterface() {
        // When
        final java.lang.reflect.Constructor<Deque>[] result = ClassUtils.getConstructors(Deque.class);

        // Then
        assertTrue(result.length > 0);
    }

    @Test
    void getConstructors_withConcreteCollectionClass() {
        // When
        final java.lang.reflect.Constructor<CustomList>[] result = ClassUtils.getConstructors(CustomList.class);

        // Then
        assertEquals(1, result.length);
        assertEquals(CustomList.class, result[0].getDeclaringClass());
    }

    @Test
    void getGenericTypes_notParameterized() {
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getGenericTypes(String.class));
    }

    private static class TestDto {
        private static String staticField;
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public static String staticMethod() {
            return staticField;
        }
    }

    private static class TestDtoWithList {
        private List<String> list;
        private List rawList;
    }

    private static class TestDtoWithMap {
        private Map<String, Long> map;
        private Map<String, List<String>> mapOfLists;
        private Map<String, ?> mapOfWildcards;
    }

    private static class GenericTestDto<T> {
        private List<T> value;
    }

    private static class ChildTestDto extends TestDto {
        private boolean active;

        public boolean isActive() {
            return active;
        }

        public static boolean childStaticMethod() {
            return true;
        }
    }

    private enum TestEnum {
        VALUE_A,
        VALUE_B
    }

    private static class UnsupportedConstructorClass {
        private UnsupportedConstructorClass(final String param) {
        }
    }

    private static class ConstructorArgumentClass {
        private final String value;

        private ConstructorArgumentClass(final String value) {
            this.value = value;
        }
    }

    private static class CustomList<T> extends ArrayList<T> {
    }

    private static class CustomListWithoutNoArgsConstructor<T> extends ArrayList<T> {
        private CustomListWithoutNoArgsConstructor(final String value) {
        }
    }

    private static class GenericParent<T> {
        T value() {
            return null;
        }
    }

    private static class StringChild extends GenericParent<String> {
        @Override
        String value() {
            return "test-value";
        }
    }
}