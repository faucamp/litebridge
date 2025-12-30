package org.litebridge.tracking;

import org.litebridge.commons.ClassUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassFieldAccessorCache {

    /**
     * Map of class -> field name -> field accessor
     */
    private static final Map<Class<?>, Map<String, FieldAccessor>> classFieldAccessors = new ConcurrentHashMap<>();

    protected ClassFieldAccessorCache() {
    }

    public static FieldAccessor fieldAccessorOrThrow(final Class<?> dtoClass, final String field) {
        final FieldAccessor fieldAccessor = ensureFieldAccessors(dtoClass).get(field);

        if (fieldAccessor == null) {
            throw new IllegalArgumentException("No field accessor found for field " + field + " in class " + dtoClass.getName());
        }

        return fieldAccessor;
    }

    public static Collection<FieldAccessor> fieldAccessors(final Class<?> dtoClass) {
        if (classFieldAccessors.containsKey(dtoClass)) {
            return classFieldAccessors.get(dtoClass).values();
        } else {
            final Map<String, FieldAccessor> fieldAccessors = ensureFieldAccessors(dtoClass);
            return fieldAccessors.values();
        }
    }

    public static boolean isNestedDtoField(final Class<?> dtoClass, final FieldAccessor field) {

        final Map<String, FieldAccessor> fieldAccessors = classFieldAccessors.get(dtoClass);

        if (fieldAccessors == null
                || ClassUtils.isBasicType(field.type())
                || Collection.class.isAssignableFrom(field.type())
                || Map.class.isAssignableFrom(field.type())) {
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
        return classFieldAccessors.computeIfAbsent(dtoClass, ClassFieldAccessorCache::createFieldAccessors);
    }

    private static Map<String, FieldAccessor> createFieldAccessors(final Class<?> dtoClass) {
        final Map<String, FieldAccessor> fieldAccessors = ClassUtils.getAllFields(dtoClass).stream()
                .map(FieldAccessorImpl::new)
                .collect(Collectors.toMap(FieldAccessor::name, Function.identity()));
        return fieldAccessors;
    }
}
