package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A collection of modified fields in a DTO, since the last snapshot.
 * <p>
 * This class is designed to be immutable and provides various methods to query and operate
 * on the collection of changed fields.
 */
public final class ChangedFields {

    private final Map<String, ChangedField> changedFieldMap;

    /**
     * Construct a new {@code ChangedFields} instance with the provided map of changed fields.
     * <p>
     * The map is expected to contain field names as keys and corresponding {@code ChangedField}
     * objects representing the modified fields as values. The map is made immutable.
     *
     * @param changedFieldMap a map where keys represent field names and values represent
     *                        {@code ChangedField} objects; the map cannot be {@code null}
     * @throws IllegalArgumentException if {@code changedFieldMap} is {@code null}
     */
    public ChangedFields(final Map<String, ChangedField> changedFieldMap) {
        this.changedFieldMap = Collections.unmodifiableMap(ObjectUtils.requireNonNull(changedFieldMap, "Changed fields map cannot be null"));
    }

    /**
     * Retrieve an {@code Optional} containing the {@code ChangedField} associated with the specified field name.
     * <p>
     * If no such field exists, an empty {@code Optional} is returned.
     *
     * @param fieldName the name of the field to retrieve; must not be {@code null}
     * @return an {@code Optional} containing the {@code ChangedField} if present, or an empty {@code Optional} if the field does not exist
     */
    public Optional<ChangedField> get(final String fieldName) {
        return Optional.ofNullable(changedFieldMap.get(fieldName));
    }

    /**
     * Retrieve the {@code ChangedField} associated with the specified field name, or {@code null} if no such field exists.
     *
     * @param fieldName the name of the field to retrieve; must not be {@code null}
     * @return the {@code ChangedField} for the specified field name, or {@code null} if the field does not exist
     */
    public @Nullable ChangedField getOrNull(final String fieldName) {
        return changedFieldMap.get(fieldName);
    }

    /**
     * Provides a stream of all {@code ChangedField} instances contained within the collection.
     *
     * @return a {@code Stream} of {@code ChangedField} objects representing the modified fields in the collection
     */
    public Stream<ChangedField> stream() {
        return changedFieldMap.values().stream();
    }

    /**
     * Perform the given action for each {@code ChangedField} in the collection.
     *
     * @param action the action to be performed for each {@code ChangedField}; must not be {@code null}
     * @throws NullPointerException if the provided {@code action} is {@code null}
     */
    public void forEach(final Consumer<ChangedField> action) {
        changedFieldMap.values().forEach(action);
    }

    /**
     * Check if the collection of changed fields is empty.
     *
     * @return {@code true} if there are no changed fields, {@code false} otherwise
     */
    public boolean isEmpty() {
        return changedFieldMap.isEmpty();
    }

    /**
     * Check if the provided field name exists within the collection of changed fields.
     *
     * @param fieldName the name of the field to check for presence; must not be null
     * @return true if the specified field name exists in the collection, false otherwise
     */
    public boolean contains(final String fieldName) {
        return changedFieldMap.containsKey(fieldName);
    }

    /**
     * Return the number of changed fields in the collection.
     *
     * @return the size of the collection as an integer, representing the number of changed fields
     */
    public int size() {
        return changedFieldMap.size();
    }
}
