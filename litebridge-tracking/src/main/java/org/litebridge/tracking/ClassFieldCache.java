package org.litebridge.tracking;

import jakarta.annotation.Nonnull;
import org.litebridge.commons.ClassUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ClassFieldCache {

    private static final Map<Class<?>, Set<Field>> fieldMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Set<Field>> nestedDtoFieldsMap = new ConcurrentHashMap<>();

    private ClassFieldCache() {
    }

    public static Set<Field> getFields(@Nonnull final Object dto) {
        return getFields(dto.getClass());
    }

    public static Set<Field> getFields(final Class<?> dtoClass) {
        return fieldMap.computeIfAbsent(dtoClass, ClassUtils::getAllFields);
    }

    public static Set<Field> getNestedDtoFields(final Class<?> dtoClass) {
        return nestedDtoFieldsMap.computeIfAbsent(dtoClass, key ->
                ClassUtils.getAllFields(dtoClass).stream()
                        .filter(field -> !ClassUtils.isBasicType(field.getType()))
                        .collect(Collectors.toSet()));
    }

    public static boolean isNestedDtoField(final Field field) {
        return getNestedDtoFields(field.getDeclaringClass()).contains(field);
    }
}
