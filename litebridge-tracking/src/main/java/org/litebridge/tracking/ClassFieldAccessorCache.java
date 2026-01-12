package org.litebridge.tracking;

import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.StringUtils;

import java.util.Collection;
import java.util.List;
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
        if (field.indexOf('.') != -1) {
            final String[] subFieldAndRestOfPath = StringUtils.splitOnce(field, '.');
            final FieldAccessor subFieldAccessor = fieldAccessor(dtoClass, subFieldAndRestOfPath[0]);
            return chain(new FieldAccessorChain(subFieldAccessor, field), subFieldAndRestOfPath[1]);
        } else {
            final FieldAccessor fieldAccessor = ensureFieldAccessors(dtoClass).get(field);

            if (fieldAccessor == null) {
                throw new IllegalArgumentException("No field accessor found for field " + field + " in class " + dtoClass.getName());
            }

            return fieldAccessor;
        }
    }

    public static List<FieldAccessor> fieldAccessors(final Class<?> dtoClass) {
        if (classFieldAccessors.containsKey(dtoClass)) {
            return classFieldAccessors.get(dtoClass).values().stream().toList();
        } else {
            final Map<String, FieldAccessor> fieldAccessors = ensureFieldAccessors(dtoClass);
            return fieldAccessors.values().stream().toList();
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

    static void clear() {
        classFieldAccessors.clear();
    }

    protected static FieldAccessor fieldAccessor(final Class<?> dtoClass, final String fieldName) {
        if (fieldName.indexOf('.') != -1) {
            // Nested field specification - traverse the field/property path
            final String[] subFieldAndRestOfPath = StringUtils.splitOnce(fieldName, '.');
            final FieldAccessor subFieldAccessor = fieldAccessor(dtoClass, subFieldAndRestOfPath[0]);
            return chain(new FieldAccessorChain(subFieldAccessor, fieldName), subFieldAndRestOfPath[1]);
        } else {
            return ensureFieldAccessors(dtoClass).get(fieldName);
        }
    }

    private static FieldAccessorChain chain(final FieldAccessorChain fieldAccessorChain, final String fieldPath) {
        final FieldAccessor subFieldAccessor;

        if (fieldPath.indexOf('.') != -1) {
            // Nested field specification - traverse the field/property path
            final String[] subFieldAndRestOfPath = StringUtils.splitOnce(fieldPath, '.');
            subFieldAccessor = fieldAccessor(fieldAccessorChain.type(), subFieldAndRestOfPath[0]);
            return chain(fieldAccessorChain.add(subFieldAccessor), subFieldAndRestOfPath[1]);
        } else {
            // Final field in the chain
            subFieldAccessor = fieldAccessor(fieldAccessorChain.type(), fieldPath);
            return fieldAccessorChain.add(subFieldAccessor);
        }
    }

    protected static FieldAccessor propertyAccessor(final Class<?> dtoClass, final String propertyName) {
        return ensureFieldAccessors(dtoClass).get(propertyName);
    }

    private static Map<String, FieldAccessor> ensureFieldAccessors(final Class<?> dtoClass) {
        return classFieldAccessors.computeIfAbsent(dtoClass, ClassFieldAccessorCache::createFieldAccessors);
    }

    private static Map<String, FieldAccessor> createFieldAccessors(final Class<?> dtoClass) {
        return ClassUtils.getAllFields(dtoClass).stream()
                .map(FieldAccessorImpl::new)
                .collect(Collectors.toMap(FieldAccessor::name, Function.identity()));
    }
}
