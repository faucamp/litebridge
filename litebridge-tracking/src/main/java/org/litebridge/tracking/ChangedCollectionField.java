package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * {@link List}-specific type of {@link ChangedField} which holds a snapshot of the original underlying map.
 * <p>
 * This class is a final implementation that provides additional functionality
 * for handling changes to {@code Map}-type fields.
 * <p>
 * It inherits the common properties and behavior from the {@link ChangedField} class
 * and extends it by including a snapshot of the original map associated with the field.
 * <p>
 * It ensures that the map snapshot is immutable.
 */
public final class ChangedCollectionField extends ChangedField {

    private final List<Integer> listSnapshot;
    private final List<Integer> prevListSnapshot;

    /**
     * Construct a {@code ChangedMapField} instance representing a change in a map field,
     * including a snapshot of its original state as an immutable map.
     *
     * @param fieldName        the name of the field being tracked
     * @param value            the new value of the field
     * @param listSnapshot     the original state of the map field, stored as an immutable map
     * @param prevListSnapshot the previous state of the list
     */
    public ChangedCollectionField(final String fieldName, final @Nullable Object value, @Nullable final List<Integer> listSnapshot, @Nullable final List<Integer> prevListSnapshot) {
        super(fieldName, value);
        this.listSnapshot = listSnapshot != null ? Collections.unmodifiableList(listSnapshot) : Collections.emptyList();
        this.prevListSnapshot = prevListSnapshot != null ? Collections.unmodifiableList(prevListSnapshot) : Collections.emptyList();
    }

    /**
     * Returns the snapshot of the list at the time tracking started.
     *
     * @return the list snapshot
     */
    public List<Integer> listSnapshot() {
        return listSnapshot;
    }

    /**
     * Returns the snapshot of the list before the last change.
     *
     * @return the previous list snapshot
     */
    public List<Integer> prevListSnapshot() {
        return prevListSnapshot;
    }

    /**
     * Returns a list of indices that have been updated.
     *
     * @return a list of updated indices
     */
    public List<Integer> updatedIndices() {
        final List<Integer> updatedIndices = new ArrayList<>();

        for (int i = 0; i < listSnapshot.size(); i++) {
            if (prevListSnapshot.size() <= i || !listSnapshot.get(i).equals(prevListSnapshot.get(i))) {
                updatedIndices.add(i);
            }
        }

        return updatedIndices;
    }

    /**
     * Returns a list of values that have been updated in the collection.
     *
     * @return a list of updated values
     */
    @SuppressWarnings("unchecked")
    public List<Object> updatedValues() {
        final List<Object> values = (List<Object>) Objects.requireNonNull(value);
        return updatedIndices().stream()
                .map(values::get)
                .toList();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final ChangedCollectionField that = (ChangedCollectionField) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.value, that.value) &&
                Objects.equals(this.listSnapshot, that.listSnapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, listSnapshot);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ChangedCollectionField.class.getSimpleName() + "[", "]")
                .add("fieldName='" + name + "'")
                .add("value=" + value)
                .add("listSnapshot=" + listSnapshot)
                .toString();
    }
}
