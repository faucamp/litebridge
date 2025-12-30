package org.litebridge.commons;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ClassUtils {

    private ClassUtils() {
    }

    /**
     * Retrieves all non-static fields from a given class, including fields declared in its superclasses
     * (except for fields the `Object` class).
     * Equivalent to {@code getAllFields(type, false)}.
     *
     * @param type the class from which to retrieve all declared fields
     * @return a set of all fields declared in the given class and its superclasses
     */
    public static Set<Field> getAllFields(final Class<?> type) {
        return getAllFields(type, false);
    }

    /**
     * Retrieves all fields from a given class, including fields declared in its superclasses
     * (except for fields of the `Object` class) and (optionally) static fields.
     *
     * @param type the class from which to retrieve all declared fields
     * @return a set of all fields declared in the given class and its superclasses
     */
    public static Set<Field> getAllFields(final Class<?> type, final boolean includeStatic) {
        // Add fields declared in the current class
        final Set<Field> fields = new HashSet<>(Arrays.stream(type.getDeclaredFields())
                .filter(field -> includeStatic || !Modifier.isStatic(field.getModifiers()))
                .toList());

        // Recursively get fields from the superclass
        if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
            fields.addAll(getAllFields(type.getSuperclass(), includeStatic));
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
    public static Field getField(final Class<?> type, final String fieldName) {
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
    public static boolean isBasicType(final Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || CharSequence.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type)
                || byte[].class.equals(type);
    }

    public static Class<?> getGenericType(final Field field) {
        return getGenericTypes(field)[0];
    }

    public static Class<?>[] getGenericTypes(final Field field) {
        return getGenericTypes(field.getGenericType());
    }

    public static Class<?>[] getGenericTypes(final Type genericType) {
        if (genericType instanceof final ParameterizedType parameterizedType) {
            // Get the actual type arguments (e.g., String)
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (!CollectionUtils.isEmpty(actualTypeArguments)) {
                // Convert each Type to Class, handling different Type implementations
                final Class<?>[] result = new Class<?>[actualTypeArguments.length];

                for (int i = 0; i < actualTypeArguments.length; i++) {
                    final Type argType = actualTypeArguments[i];

                    if (argType instanceof Class<?> clazz) {
                        result[i] = clazz;
                    } else if (argType instanceof ParameterizedType paramType) {
                        // For parameterized types like List<String>, get the raw type
                        result[i] = (Class<?>) paramType.getRawType();
                    } else {
                        // For other types (TypeVariable, WildcardType, etc.), we can't determine a concrete class
                        throw new IllegalArgumentException("Cannot determine concrete class for generic type argument '%s' of type '%s'".formatted(argType, genericType.getTypeName()));
                    }
                }

                return result;
            }
        }

        throw new IllegalArgumentException("Cannot determine generic type for type '%s'".formatted(genericType.getTypeName()));
    }

    public static PropertyDescriptor getProperty(final Class<?> dtoClass, final String propertyName) {
        try {
            return Arrays.stream(Introspector.getBeanInfo(dtoClass).getPropertyDescriptors())
                    .filter(propertyDescriptor -> propertyName.equals(propertyDescriptor.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Property '%s' not found in class: %s".formatted(propertyName, dtoClass.getName())));
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("Failed to introspect class: " + dtoClass.getName(), ex);
        }
    }
}
