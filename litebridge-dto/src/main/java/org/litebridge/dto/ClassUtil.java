package org.litebridge.dto;

import jakarta.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ClassUtil {

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        // Add fields declared in the current class
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        // Recursively get fields from the superclass
        if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }

        return fields;
    }

    public static @Nullable Field getField(Class<?> type, String fieldName) {
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
                return getField(type.getSuperclass(), fieldName);
            } else {
                return null;
            }
        }
    }
}
