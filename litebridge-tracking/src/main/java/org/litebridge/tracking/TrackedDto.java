package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.commons.collector.MapCollector;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Wrapper that tracks changes made to an external DTO.
 * <p>
 * {@code TrackedDto} allows tracking changes in the fields of a given DTO (Data Transfer Object) over time.
 * It maintains snapshots of field states and can identify changes by comparing the current state
 * of the DTO's fields with previous snapshots.
 * <p>
 * This class is immutable and thread-safe. A typical usage involves taking snapshots of the DTO's
 * fields, detecting changes, and optionally tracking nested DTOs or collections/maps of nested DTOs.
 *
 * @param <DTO> the type of the DTO object being tracked
 */
public final class TrackedDto<DTO> {

    private final WeakReference<DTO> dtoRef;
    private final Collection<FieldAccessor> fields;
    private final Consumer<Object> trackDtoCallback;
    @Nullable
    private List<FieldSnapshot> fieldSnapshots;
    @Nullable
    private ChangedFields changedFields;

    /**
     * Construct a {@code TrackedDto} instance that tracks the given DTO object.
     *
     * @param dto              the data transfer object (DTO) to be wrapped and tracked; must not be null
     * @param trackDtoCallback the callback function to be triggered when tracking changes
     */
    public TrackedDto(final DTO dto, final Consumer<Object> trackDtoCallback) {
        this(dto, ClassFieldAccessorCache.fieldAccessors(dto.getClass()), trackDtoCallback);
    }

    /**
     * Construct a {@code TrackedDto} instance that wraps and tracks a given data transfer object (DTO).
     *
     * @param dto              the data transfer object (DTO) to be wrapped and tracked; must not be null
     * @param fields           the collection of {@code FieldAccessor} objects representing the fields to be tracked; must not be null
     * @param trackDtoCallback the callback function to be triggered when tracking changes; must not be null
     * @throws IllegalArgumentException if any of the parameters are null
     */
    public TrackedDto(final DTO dto, final Collection<FieldAccessor> fields, final Consumer<Object> trackDtoCallback) {
        this.dtoRef = new WeakReference<>(ObjectUtils.requireNonNull(dto, "DTO cannot be null"));
        this.fields = ObjectUtils.requireNonNull(fields, "No tracked fields provided");
        this.trackDtoCallback = ObjectUtils.requireNonNull(trackDtoCallback, "No \"track DTO\" callback provided");
    }

    /**
     * Retrieve the underlying DTO that is being tracked.
     * <p>
     * Ensures that the DTO has not been garbage collected.
     * If the DTO has been garbage collected, an exception is thrown.
     *
     * @return the non-null tracked {@code DTO} instance
     * @throws IllegalStateException if the DTO object has been garbage collected
     */
    public DTO dto() {
        return ObjectUtils.requireNonNull(dtoRef.get(), () -> new IllegalStateException("DTO object has been garbage collected: " + this));
    }

    /**
     * Captures the current state of the tracked fields, creating a snapshot for later comparison.
     *
     * @param overwrite if {@code true}, any existing snapshots are cleared, and a new snapshot is taken;
     *                  if {@code false}, an exception is thrown if a snapshot already exists
     * @throws IllegalStateException if a previous snapshot has already been taken and {@code overwrite} is {@code false}.
     */
    public void snapshot(final boolean overwrite) {
        snapshot(fields, overwrite);
    }

    private void snapshot(final Collection<FieldAccessor> fields, final boolean overwrite) {
        if (fieldSnapshots != null) {
            if (overwrite) {
                fieldSnapshots.clear();
                changedFields = null;
            } else {
                throw new IllegalStateException("Field snapshots already taken for object: " + this);
            }
        }

        fieldSnapshots = createFieldSnapshots(fields);
    }

    /**
     * Initializes an empty snapshot of the tracked fields for the current object.
     * <p>
     * This method captures the initial state of all fields as {@code null}, with a hash value of 0.
     *
     * @throws IllegalStateException if a previous snapshot has already been taken.
     */
    public void snapshotEmpty() {
        if (fieldSnapshots != null) {
            throw new IllegalStateException("Field snapshots already taken for object: " + this);
        }

        fieldSnapshots = new ArrayList<>();
        fields.forEach(field -> fieldSnapshots.add(new FieldSnapshot(field, 0)));
    }

    /**
     * Retrieves the collection of modified fields in the tracked DTO since the last snapshot.
     * <p>
     * On first invocation after a snapshot, the DTO is evaluated against the existing snapshot and
     * a {@link ChangedFields} result is generated. This {@link ChangedFields} instance is cached,
     * so subsequent calls to this method will return the same instance and is safe to use repeatedly.
     *
     * @return a {@link ChangedFields} instance representing the modified fields
     * @see #changedFields(boolean)
     */
    public ChangedFields changedFields() {
        return changedFields(false);
    }

    /**
     * Retrieves the collection of modified fields in the tracked DTO since the last snapshot.
     * <p>
     * If {@code refresh} is {@code true}, the method updates the internal snapshots based on the
     * current modifications before returning the changed fields; effectively refreshing the tracking state.
     * This means that the next call to {@link #changedFields()} will no longer return the same result
     * <p>
     * If {@code refresh} is {@code false}, the method behaves exactly as {@link #changedFields()}.
     *
     * @param refresh a boolean flag indicating whether to refresh the state of tracked fields
     *                before determining the changed fields. If {@code true}, the method updates
     *                the internal snapshots to reflect the latest state of the tracked fields.
     *                If {@code false}, the method uses the previously cached snapshots to
     *                determine changed fields.
     * @return a {@code ChangedFields} instance representing the modified fields in the tracked DTO.
     * If no changes have occurred, this instance will represent an empty set of changed fields.
     * @throws IllegalStateException if field snapshots have not been previously taken.
     * @see #changedFields()
     */
    public ChangedFields changedFields(final boolean refresh) {
        final Object dto = dto();

        if (refresh && changedFields != null) {
            updateFieldSnapshotsWithChangedFields();
        }

        if (changedFields == null) {
            if (fieldSnapshots == null) {
                throw new IllegalStateException("Field snapshots not taken for object: " + dto);
            }

            final Map<String, FieldSnapshot> fieldSnapshotMap = fieldSnapshots.stream()
                    .collect(Collectors.toMap(fieldSnapshot -> fieldSnapshot.field().name(), Function.identity()));

            final LinkedHashMap<String, ChangedField> changedFieldMap = createFieldSnapshots(this.fields).stream()
                    .filter(fieldSnapshot -> {
                        // Filter on changed fields by comparing the current field value hash with the previous snapshot hash
                        final FieldSnapshot oldFieldSnapshot = fieldSnapshotMap.get(fieldSnapshot.field().name());
                        return fieldSnapshot.hash() != oldFieldSnapshot.hash();
                    })
                    .peek(fieldSnapshot -> {
                        // Update internal DTO tracking if the field is a nested DTO
                        if (fieldSnapshot.hash() != 0 && ClassFieldAccessorCache.isNestedDtoField(dto.getClass(), fieldSnapshot.field())) {
                            // This nested DTO field was null previously, so we need to track the new value
                            final Object nestedDto = getFieldValue(dto, fieldSnapshot.field());
                            trackDtoCallback.accept(nestedDto);
                        }
                    })
                    .map(fieldSnapshot -> {
                        if (fieldSnapshot.isMap()) {
                            final FieldSnapshot oldFieldSnapshot = fieldSnapshotMap.get(fieldSnapshot.field().name());
                            return new ChangedMapField(fieldSnapshot.field().name(), getFieldValue(dto, fieldSnapshot.field()), oldFieldSnapshot.mapSnapshot());
                        } else {
                            return new ChangedField(fieldSnapshot.field().name(), getFieldValue(dto, fieldSnapshot.field()));
                        }
                    })
                    .collect(MapCollector.toLinkedHashMap(ChangedField::name, Function.identity()));
            changedFields = new ChangedFields(changedFieldMap);
        }

        return changedFields;
    }

    private List<FieldSnapshot> createFieldSnapshots(final Collection<FieldAccessor> fields) {
        final DTO dto = dto();
        final List<FieldSnapshot> fieldSnapshots = new ArrayList<>();

        fields.forEach(field -> {
            if (Map.class.isAssignableFrom(field.type())) {
                // To track changes in a map, we need to snapshot the current map values
                final Map<?, ?> currentMap = (Map<?, ?>) getFieldValue(dto, field);
                final int overallFieldHash = getFieldHash(dto, field);

                final Map<?, Integer> mapSnapshot;

                if (!CollectionUtils.isEmpty(currentMap)) {
                    // Create snapshot of current map contents
                    mapSnapshot = currentMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> getValueHash(entry.getValue())));

                    // Track changes to its keys/values if they are nested DTOs
                    final Class<?>[] genericTypes = field.genericTypes();

                    if (!ClassUtils.isBasicType(genericTypes[0])) {
                        currentMap.keySet().forEach(trackDtoCallback);
                    }

                    if (!ClassUtils.isBasicType(genericTypes[1])) {
                        currentMap.values().forEach(trackDtoCallback);
                    }
                } else {
                    mapSnapshot = Collections.emptyMap();
                }

                fieldSnapshots.add(new FieldSnapshot(field, overallFieldHash, mapSnapshot));
            } else {
                fieldSnapshots.add(new FieldSnapshot(field, getFieldHash(dto, field)));

                if (Collection.class.isAssignableFrom(field.type())) {
                    // Snapshot nested collection
                    final Collection<?> collection = (Collection<?>) getFieldValue(dto, field);

                    if (!CollectionUtils.isEmpty(collection)) {
                        final Class<?> genericType = field.genericType();

                        if (!ClassUtils.isBasicType(genericType)) {
                            collection.forEach(trackDtoCallback);
                        }
                    }
                } else if (!ClassUtils.isBasicType(field.type())) {
                    // Snapshot nested DTO
                    final Object nestedDto = getFieldValue(dto, field);

                    if (nestedDto != null) {
                        trackDtoCallback.accept(nestedDto);
                    }
                }
            }
        });

        return fieldSnapshots;
    }

    private void updateFieldSnapshotsWithChangedFields() {
        final ListIterator<FieldSnapshot> fieldSnapshotIterator = fieldSnapshots.listIterator();

        while (fieldSnapshotIterator.hasNext()) {
            final FieldSnapshot oldFieldSnapshot = fieldSnapshotIterator.next();
            changedFields.get(oldFieldSnapshot.field().name())
                    .ifPresent(changedField -> {
                        // Replace the old snapshot with a new one based on the changed field - this avoids the previously-detected change from being seen again
                        final FieldSnapshot newFieldSnapshot = toFieldSnapshot(oldFieldSnapshot.field(), changedField);
                        fieldSnapshotIterator.set(newFieldSnapshot);
                    });
        }

        // Reset changed fields
        changedFields = null;
    }

    private static int getFieldHash(final Object instance, final FieldAccessor field) {
        return getValueHash(getFieldValue(instance, field));
    }

    private static int getFieldHash(final Object instance, final Field field) {
        return getValueHash(getFieldValue(instance, ClassFieldAccessorCache.fieldAccessorOrThrow(instance.getClass(), field.getName())));
    }

    private static int getValueHash(final Object fieldValue) {
        if (fieldValue == null) {
            return 0;
        } else if (ClassUtils.isBasicType(fieldValue.getClass())) {
            return fieldValue.hashCode();
        } else if (fieldValue instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return 0;
            } else {
                return collection.stream()
                        .map(TrackedDto::getValueHash)
                        .reduce(0, Integer::sum);
            }
        } else if (fieldValue instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                return 0;
            } else {
                return map.entrySet().stream()
                        .map(entry -> getValueHash(entry.getKey()) + getValueHash(entry.getValue()))
                        .reduce(0, Integer::sum);
            }
        } else {
            return getDtoHash(fieldValue);
        }
    }

    private static int getDtoHash(final Object dto) {
        return ClassFieldCache.getFields(dto).stream()
                .reduce(1, (hash, field) -> hash + getFieldHash(dto, field), Integer::sum);
    }

    private static @Nullable Object getFieldValue(final Object instance, final FieldAccessor field) {
        return field.get(instance);
    }

    private FieldSnapshot toFieldSnapshot(final FieldAccessor fieldAccessor, final ChangedField changedField) {
        return new FieldSnapshot(fieldAccessor, getValueHash(changedField.value()));
    }
}
