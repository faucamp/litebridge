package org.litebridgedb.maven;

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
            "double", double.class,
            "void", void.class
    );

    public static Class<?> getPrimitiveClass(String name) throws ClassNotFoundException {
        Class<?> clazz = PRIMITIVES.get(name);
        if (clazz == null) {
            return Class.forName(name); // Fallback to normal objects
        }
        return clazz;
    }
}