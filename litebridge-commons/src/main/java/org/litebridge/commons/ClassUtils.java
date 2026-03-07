package org.litebridge.commons;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for working with Java reflection.
 */
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
    public static List<Field> getAllFields(final Class<?> type, final MethodHandles.Lookup lookup) {
        return getAllFields(type, false, lookup);
    }

    /**
     * Retrieves all fields from a given class, including fields declared in its superclasses
     * (except for fields of the `Object` class) and (optionally) static fields.
     *
     * @param type the class from which to retrieve all declared fields
     * @return a list of all fields declared in the given class and its superclasses
     */
    public static List<Field> getAllFields(final Class<?> type, final boolean includeStatic, final MethodHandles.Lookup lookup) {
        try {
            lookup.accessClass(type);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("No access to class: %s. Please provide a suitable MethodHandles.Lookup or 'opens %s to %s;' to your module-info.java\",".formatted(type.getName(), lookup.getClass().getModule().getName(), ClassUtils.class.getModule().getName()), e);
        }

        // Add fields declared in the current class
        final List<Field> fields = Arrays.stream(type.getDeclaredFields())
                .filter(field -> includeStatic || !Modifier.isStatic(field.getModifiers()))
                .collect(Collectors.toCollection(ArrayList::new));

        // Recursively get fields from the superclass
        if (!type.getSuperclass().equals(Object.class)) {
            fields.addAll(getAllFields(type.getSuperclass(), includeStatic, lookup));
        }

        return fields;
    }

    /**
     * Retrieves a specific field from the specified class or its superclasses.
     * <p>
     * This method attempts to find a field in the given class by its name. If the field is not
     * found in the current class, it recursively checks the superclasses up to (but not including) the `Object` class.
     *
     * @param cls       the class to search for the field
     * @param fieldName the name of the field to retrieve
     * @return the {@code Field} object representing the specified field if found
     * @throws IllegalArgumentException if the field cannot be found in the specified class or its superclasses
     */
    public static Field getField(final Class<?> cls, final String fieldName) {
        try {
            return cls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            if (!cls.getSuperclass().equals(Object.class)) {
                return getField(cls.getSuperclass(), fieldName);
            } else {
                throw new IllegalArgumentException("Field '%s' does not exist in DTO class '%s'".formatted(fieldName, cls.getName()));
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

    /**
     * Retrieves the first generic type argument of a given {@link Field}, if the field is parameterized.
     * <p>
     * For example, if the field is of type {@code List<String>}, this method will return {@code String.class}.
     * However, if the field is of type {@code Map<Long, String>}, it will return {@code Long.class}.
     *
     * @param field the field from which to retrieve the first generic type argument
     * @return the {@code Class<?>} object representing the first generic type argument of the field
     */
    public static Class<?> getGenericType(final Field field) {
        return getGenericTypes(field)[0];
    }

    /**
     * Retrieves the generic type arguments of a given {@link Field}, if the field is parameterized.
     * <p>
     * For example, if the field is of type {@code List<String>}, this method will return
     * an array containing {@code String.class}.
     *
     * @param field the field from which to retrieve the generic type arguments
     * @return an array of {@code Class<?>} objects representing the generic type arguments of the field
     */
    public static Class<?>[] getGenericTypes(final Field field) {
        return getGenericTypes(field.getGenericType());
    }

    /**
     * Retrieves the generic type arguments of a given {@link Type}, if it is parameterized.
     * <p>
     * For example, if the generic type is {@code List<String>}, this method will return
     * an array containing {@code String.class}.
     *
     * @param genericType the type for which to retrieve the generic type arguments
     * @return an array of {@code Class<?>} objects representing the generic type arguments
     * @throws IllegalArgumentException if the generic type is not parameterized
     *                                  or if a concrete class cannot be determined for one of the arguments
     */
    public static Class<?>[] getGenericTypes(final Type genericType) {
        if (genericType instanceof final ParameterizedType parameterizedType) {
            // Get the actual type arguments (e.g., String)
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

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

        throw new IllegalArgumentException("Cannot determine generic type for type '%s'".formatted(genericType.getTypeName()));
    }

    /**
     * Retrieves the property descriptor for a specific property of the given class.
     *
     * @param dtoClass     the class for which the property descriptor is to be retrieved
     * @param propertyName the name of the property whose descriptor is to be retrieved
     * @return the {@code PropertyDescriptor} for the specified property
     * @throws IllegalArgumentException if the specified property is not found in the class
     * @throws IllegalStateException    if an introspection error occurs while retrieving the property information
     */
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

    /**
     * Creates a new instance of the specified class type using its no-argument constructor.
     * <p>
     * This method allows instantiating classes even if their constructors are not publicly accessible.
     *
     * @param <DTO>    the type of the object to be created
     * @param dtoClass the class object representing the type to instantiate
     * @return a new instance of the specified class type
     * @throws IllegalStateException if the instantiation fails due to an exception (e.g., no accessible constructor, security restriction)
     */
    public static <DTO> DTO newInstance(final Class<DTO> dtoClass) {
        try {
            final Constructor<DTO> constructor = getConcreteClass(dtoClass).getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate class: " + dtoClass.getName(), ex);
        }
    }

    public static <DTO> DTO newInstance(final Class<DTO> dtoClass, final Constructor<DTO> constructor, final Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate class: " + dtoClass.getName() + " with constructor: " + constructor, ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <DTO> Constructor<DTO>[] getConstructors(final Class<DTO> dtoClass) {
        return (Constructor<DTO>[]) getConcreteClass(dtoClass).getDeclaredConstructors();
    }

    private static <T> Class<T> getConcreteClass(final Class<T> type) {
        if (Collection.class.isAssignableFrom(type)) {
            return (Class<T>) ArrayList.class;
        } else {
            return type;
        }
    }
}
