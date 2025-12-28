package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.util.Map;

record FieldSnapshot(FieldAccessor field, int hash, @Nullable Map<?, Integer> mapSnapshot) {

    public FieldSnapshot(final FieldAccessor field, final int hash) {
        this(field, hash, null);
    }

    public boolean isMap() {
        return mapSnapshot != null;
    }
}
