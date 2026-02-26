package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A snapshot of a field's state for comparison or tracking changes.
 * <p>
 * This record holds the field metadata, hash value, and an optional map snapshot of associated data.
 */
record FieldSnapshot(FieldAccessor field,
                     int hash,
                     @Nullable Map<?, Integer> mapSnapshot,
                     @Nullable List<Integer> listSnapshot) {

    /**
     * Constructor for creating a field snapshot with a specified field and hash value.
     *
     * @param field the {@code FieldAccessor} that represents the field being tracked. Must not be null.
     * @param hash  the hash value of the field's state used for comparison or tracking changes.
     */
    public FieldSnapshot(final FieldAccessor field, final int hash) {
        this(field, hash, null, null);
    }

    public FieldSnapshot(final FieldAccessor field, final int hash, final @Nullable Map<?, Integer> mapSnapshot) {
        this(field, hash, mapSnapshot, null);
    }

    public FieldSnapshot(final FieldAccessor field, final int hash, final @Nullable List<Integer> listSnapshot) {
        this(field, hash, null, listSnapshot);
    }

    /**
     * Determines if a map snapshot is associated with the current field snapshot.
     *
     * @return {@code true} if a map snapshot is present, otherwise {@code false}.
     */
    public boolean isMap() {
        return mapSnapshot != null;
    }

    public boolean isCollection() {
        return listSnapshot != null;
    }
}
