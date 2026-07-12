package org.litebridge.maven.reverse;

import java.util.Map;

/**
 * Provides utilities for mapping between primitive types and their corresponding wrapper types
 * in Java, as well as for resolving primitive types by name.
 */
final class PrimitiveLookup {

    private static final Map<String, Class<?>> PRIMITIVES = Map.of(
            "boolean", boolean.class,
            "byte", byte.class,
            "char", char.class,
            "short", short.class,
            "int", int.class,
            "long", long.class,
            "float", float.class,
            "double", double.class
    );

    private static final Map<Class<?>, Class<?>> OBJECT_TO_PRIMITIVE = Map.of(
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class
    );

    /**
     * Retrieves the primitive `Class` object corresponding to a given type name, if available.
     * <p>
     * If the provided name does not correspond to a primitive type, it attempts to
     * resolve the class by its name via `Class.forName(name)`.
     *
     * @param name The name of the primitive type.
     * @return The `Class` object representing the primitive type.
     * @throws ClassNotFoundException If the class cannot be found.
     */
    public static Class<?> getPrimitiveClass(final String name) throws ClassNotFoundException {
        final Class<?> clazz = PRIMITIVES.get(name);

        if (clazz == null) {
            // Fallback to normal objects
            return Class.forName(name);
        }

        return clazz;
    }

    /**
     * Retrieves the primitive `Class` object corresponding to a given wrapper type `Class`.
     * <p>
     * If the provided class is not a wrapper type, it returns the class itself.
     *
     * @param clazz The wrapper type `Class` object.
     * @return The `Class` object representing the primitive type.
     */
    public static Class<?> getPrimitiveClass(final Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return clazz;
        }

        final Class<?> primitiveClass = OBJECT_TO_PRIMITIVE.get(clazz);

        if (primitiveClass == null) {
            return clazz;
        }

        return primitiveClass;
    }
}