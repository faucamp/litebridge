package org.litebridge.tracking;

import jakarta.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

record FieldSnapshot(Field field, int hash, @Nullable Map<?, Integer> originalMapSnapshot) {

    public FieldSnapshot(final Field field, final int hash) {
        this(field, hash, null);
    }
}
