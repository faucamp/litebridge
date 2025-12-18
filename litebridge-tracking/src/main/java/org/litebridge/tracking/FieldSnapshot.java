package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

record FieldSnapshot(Field field, int hash, @Nullable Map<?, Integer> mapSnapshot) {

    public FieldSnapshot(final Field field, final int hash) {
        this(field, hash, null);
    }

    public boolean isMap() {
        return mapSnapshot != null;
    }
}
