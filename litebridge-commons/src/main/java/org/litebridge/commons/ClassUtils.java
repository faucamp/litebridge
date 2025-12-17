package org.litebridge.commons;

import jakarta.annotation.Nonnull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ClassUtils {

    private ClassUtils() {
    }

    /**
     * Retrieves all fields from a given class, including fields declared in its superclasses
     * (except for the `Object` class).
     *
     * @param type the class from which to retrieve all declared fields
     * @return a list of all fields declared in the given class and its superclasses
     */
    public static Set<Field> getAllFields(@Nonnull final Class<?> type) {
        final Set<Field> fields = new HashSet<>();
        // Add fields declared in the current class
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        // Recursively get fields from the superclass
        if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }

        return fields;
    }

    /**
     * Retrieves a specific field from the specified class or its superclasses.
     * <p>
     * This method attempts to find a field in the given class by its name. If the field is not
     * found in the current class, it recursively checks the superclasses up to (but not including) the `Object` class.
     *
     * @param type      the class to search for the field
     * @param fieldName the name of the field to retrieve
     * @return the {@code Field} object representing the specified field if found
     * @throws IllegalArgumentException if the field cannot be found in the specified class or its superclasses
     */
    public static Field getField(@Nonnull final Class<?> type, @Nonnull final String fieldName) {
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
                return getField(type.getSuperclass(), fieldName);
            } else {
                throw new IllegalArgumentException("Field '%s' does not exist in DTO class '%s'".formatted(fieldName, type.getName()));
            }
        }
    }

    /**
     * Determines if the provided class type represents a basic type.
     * <p>
     * A basic type is defined as one of the following:
     * - A primitive type (e.g., int, double).
     * - An enum.
     * - A type that is a subclass or implementation of {@code CharSequence}.
     * - A type that is a subclass of {@code Number}.
     * - A type that is a subclass of {@code Boolean}.
     * - A {@code byte[]} type.
     *
     * @param type the {@code Class} object to check for being a basic type
     * @return {@code true} if the provided class type is considered a basic type, {@code false} otherwise
     */
    public static boolean isBasicType(@Nonnull final Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || CharSequence.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type)
                || byte[].class.equals(type);
    }

    public static Class<?> getGenericType(@Nonnull final Field field) {
        final Type genericFieldType = field.getGenericType();

        if (genericFieldType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) genericFieldType;
            // Get the actual type arguments (e.g., String)
            final Type[] fieldArgTypes = parameterizedType.getActualTypeArguments();

            if (!CollectionUtils.isEmpty(fieldArgTypes) && fieldArgTypes[0] instanceof Class) {
                return (Class<?>) fieldArgTypes[0];
            }
        }

        throw new IllegalArgumentException("Cannot determine generic type for field '%s'".formatted(field.getName()));
    }
}
