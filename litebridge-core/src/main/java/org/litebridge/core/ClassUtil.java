package org.litebridge.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClassUtil {

    private ClassUtil() {
    }

    public static List<Field> getAllFields(Class<?> type) {
        final List<Field> fields = new ArrayList<>();
        // Add fields declared in the current class
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        // Recursively get fields from the superclass
        if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }

        return fields;
    }

    public static Field getField(Class<?> type, String fieldName) {
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

    public static boolean isBasicType(final Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || CharSequence.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type)
                || byte[].class.equals(type);
    }
}
