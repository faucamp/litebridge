package org.litebridgedb.maven.reverse;

import java.util.Map;

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

    public static Class<?> getPrimitiveClass(final String name) throws ClassNotFoundException {
        final Class<?> clazz = PRIMITIVES.get(name);

        if (clazz == null) {
            // Fallback to normal objects
            return Class.forName(name);
        }

        return clazz;
    }

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