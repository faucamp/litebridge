package org.litebridge.tracking;

import org.litebridge.commons.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ClassFieldCache {

    private static final Map<Class<?>, Set<Field>> fieldMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Set<Field>> nestedDtoFieldsMap = new ConcurrentHashMap<>();
    private static final Map<Type, Class<?>[]> genericTypesMap = new ConcurrentHashMap<>();

    private ClassFieldCache() {
    }

    public static Set<Field> getFields(final Object dto) {
        return getFields(dto.getClass());
    }

    public static Set<Field> getFields(final Class<?> dtoClass) {
        return fieldMap.computeIfAbsent(dtoClass, ClassUtils::getAllFields);
    }

    public static Set<Field> nestedDtoFields(final Class<?> dtoClass) {
        return nestedDtoFieldsMap.computeIfAbsent(dtoClass, key ->
                ClassUtils.getAllFields(dtoClass).stream()
                        .filter(field -> !ClassUtils.isBasicType(field.getType()))
                        .collect(Collectors.toSet()));
    }

    public static boolean isNestedDtoField(final Field field) {
        return nestedDtoFields(field.getDeclaringClass()).contains(field);
    }

    public static Class<?> getGenericType(final Field field) {
        return getGenericTypes(field)[0];
    }

    public static Class<?>[] getGenericTypes(final Field field) {
        return getGenericTypes(field.getGenericType());
    }

    public static Class<?>[] getGenericTypes(final Type genericType) {
        return genericTypesMap.computeIfAbsent(genericType, ClassUtils::getGenericTypes);
    }
}
