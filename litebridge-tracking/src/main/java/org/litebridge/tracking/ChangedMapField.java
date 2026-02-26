package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * {@link Map}-specific type of {@link ChangedField} which holds a snapshot of the original underlying map.
 * <p>
 * This class is a final implementation that provides additional functionality
 * for handling changes to {@code Map}-type fields.
 * <p>
 * It inherits the common properties and behavior from the {@link ChangedField} class
 * and extends it by including a snapshot of the original map associated with the field.
 * <p>
 * It ensures that the map snapshot is immutable.
 */
public final class ChangedMapField extends ChangedField {

    private final Map<?, Integer> mapSnapshot;

    /**
     * Construct a {@code ChangedMapField} instance representing a change in a map field,
     * including a snapshot of its original state as an immutable map.
     *
     * @param fieldName   the name of the field being tracked
     * @param value       the new value of the field
     * @param mapSnapshot the original state of the map field, stored as an immutable map
     */
    public ChangedMapField(final String fieldName, @Nullable final Object value, @Nullable final Map<?, Integer> mapSnapshot) {
        super(fieldName, value);
        this.mapSnapshot = mapSnapshot != null ? Collections.unmodifiableMap(mapSnapshot) : Collections.emptyMap();
    }

    /**
     * Retrieve the snapshot of the original map associated with this field.
     *
     * @return an immutable {@link Map} containing the snapshot of the original map,
     * where keys are of an unspecified type and values are integers.
     */
    public Map<?, Integer> mapSnapshot() {
        return mapSnapshot;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final ChangedMapField that = (ChangedMapField) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.value, that.value) &&
                Objects.equals(this.mapSnapshot, that.mapSnapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, mapSnapshot);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ChangedMapField.class.getSimpleName() + "[", "]")
                .add("fieldName='" + name + "'")
                .add("value=" + value)
                .add("mapSnapshot=" + mapSnapshot)
                .toString();
    }
}
