package org.litebridge.dto;

import jakarta.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;

record FieldSnapshot(Field field, TrackedField trackedField, int hash, @Nullable Map<?, Integer> originalMapSnapshot) {

    public FieldSnapshot(final Field field, final int hash) {
        this(field, getTrackedFieldAnnotation(field), hash, null);
    }

    public FieldSnapshot(final Field field, final int hash, final Map<?, Integer> originalMapSnapshot) {
        this(field, getTrackedFieldAnnotation(field), hash, originalMapSnapshot);
    }

    private static TrackedField getTrackedFieldAnnotation(final Field field) {
        final TrackedField trackedField = field.getAnnotation(TrackedField.class);

        if (trackedField == null) {
            throw new IllegalArgumentException("Field %s is not annotated with @TrackedField".formatted(field));
        }

        return trackedField;
    }
}
