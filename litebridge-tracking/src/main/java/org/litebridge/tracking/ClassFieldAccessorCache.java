package org.litebridge.tracking;

import org.litebridge.commons.ClassUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassFieldAccessorCache {

    /**
     * Map of class -> field name -> field accessor
     */
    private static final Map<Class<?>, Map<String, FieldAccessor>> classFieldAccessors = new ConcurrentHashMap<>();

    protected ClassFieldAccessorCache() {
    }

    public static FieldAccessor fieldAccessorOrThrow(final Class<?> dtoClass, final String field) {
        final Map<String, FieldAccessor> fieldAccessors = classFieldAccessors.get(dtoClass);

        if (fieldAccessors == null) {
            throw new IllegalArgumentException("No field accessors found for class " + dtoClass.getName());
        }

        final FieldAccessor fieldAccessor = fieldAccessors.get(field);

        if (fieldAccessor == null) {
            throw new IllegalArgumentException("No field accessor found for field " + field + " in class " + dtoClass.getName());
        }

        return fieldAccessor;
    }

    public static Collection<FieldAccessor> fieldAccessors(final Class<?> dtoClass) {
        return ensureFieldAccessors(dtoClass).values();
    }

    public static boolean isNestedDtoField(final Class<?> dtoClass, final FieldAccessor field) {
        final Map<String, FieldAccessor> fieldAccessors = classFieldAccessors.get(dtoClass);

        if (fieldAccessors == null) {
            return false;
        }

        return fieldAccessors.containsKey(field.name());
    }

    protected static FieldAccessor fieldAccessor(final Class<?> dtoClass, final String fieldName) {
        return ensureFieldAccessors(dtoClass)
                .computeIfAbsent(fieldName, fn -> new FieldAccessorImpl(ClassUtils.getField(dtoClass, fieldName)));
    }

    protected static FieldAccessor propertyAccessor(final Class<?> dtoClass, final String propertyName) {
        return ensureFieldAccessors(dtoClass)
                .computeIfAbsent(propertyName, pn -> new PropertyAccessor(ClassUtils.getProperty(dtoClass, propertyName)));
    }

    private static Map<String, FieldAccessor> ensureFieldAccessors(final Class<?> dtoClass) {
        return classFieldAccessors.computeIfAbsent(dtoClass, cls -> new ConcurrentHashMap<>());
    }
}
