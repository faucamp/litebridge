package org.litebridgedb.tracking;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.commons.collector.MapCollector;
import org.litebridgedb.commons.type.WeakIdentitySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackedDto.class);
    private final WeakReference<DTO> dtoRef;
    private final Collection<FieldAccessor> fields;
    private final Consumer<Object> trackDtoCallback;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    @Nullable
    private List<FieldSnapshot> fieldSnapshots;
    @Nullable
    private ChangedFields changedFields;

    /**
     * Construct a {@code TrackedDto} instance that tracks all fields of the given DTO object.
     *
     * @param dto              the data transfer object (DTO) to be wrapped and tracked; must not be null
     * @param trackDtoCallback the callback function to be triggered when tracking changes
     */
    public TrackedDto(final DTO dto, final ClassFieldAccessorCache classFieldAccessorCache, final Consumer<Object> trackDtoCallback) {
        this(dto, classFieldAccessorCache.fieldAccessors(dto.getClass()), classFieldAccessorCache, trackDtoCallback);
    }

    /**
     * Construct a {@code TrackedDto} instance that wraps and tracks a given data transfer object (DTO).
     *
     * @param dto              the data transfer object (DTO) to be wrapped and tracked; must not be null
     * @param fields           the collection of {@code FieldAccessor} objects representing the fields to be tracked; must not be null
     * @param trackDtoCallback the callback function to be triggered when tracking changes; must not be null
     * @throws IllegalArgumentException if any of the parameters are null
     */
    public TrackedDto(final DTO dto, final Collection<FieldAccessor> fields, final ClassFieldAccessorCache classFieldAccessorCache, final Consumer<Object> trackDtoCallback) {
        this.dtoRef = new WeakReference<>(Objects.requireNonNull(dto, "DTO cannot be null"));
        this.fields = Objects.requireNonNull(fields, "No tracked fields provided");
        this.trackDtoCallback = Objects.requireNonNull(trackDtoCallback, "No \"track DTO\" callback provided");
        this.classFieldAccessorCache = Objects.requireNonNull(classFieldAccessorCache, "No ClassFieldAccessorCache provided");
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

        if (changedFields == null || refresh) {
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
                        if (fieldSnapshot.hash() != 0 && classFieldAccessorCache.isNestedDtoField(dto.getClass(), fieldSnapshot.field())) {
                            // This nested DTO field was null previously, so we need to track the new value
                            final Object nestedDto = Objects.requireNonNull(getFieldValue(dto, fieldSnapshot.field()));
                            trackDtoCallback.accept(nestedDto);
                        }
                    })
                    .map(fieldSnapshot -> {
                        if (fieldSnapshot.isMap()) {
                            final FieldSnapshot oldFieldSnapshot = fieldSnapshotMap.get(fieldSnapshot.field().name());
                            return new ChangedMapField(fieldSnapshot.field().name(), getFieldValue(dto, fieldSnapshot.field()), oldFieldSnapshot.mapSnapshot());
                        } else if (fieldSnapshot.isCollection()) {
                            final FieldSnapshot oldFieldSnapshot = fieldSnapshotMap.get(fieldSnapshot.field().name());
                            return new ChangedCollectionField(fieldSnapshot.field().name(), getFieldValue(dto, fieldSnapshot.field()), fieldSnapshot.listSnapshot(), oldFieldSnapshot.listSnapshot());
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
        final WeakIdentitySet<Object> dtosVisited = new WeakIdentitySet<>();

        fields.forEach(field -> {
            if (Map.class.isAssignableFrom(field.type())) {
                // To track changes in a map, we need to snapshot the current map values
                final Map<?, ?> currentMap = (Map<?, ?>) getFieldValue(dto, field);
                final int overallFieldHash = getFieldHash(dto, field, dtosVisited);

                final Map<?, Integer> mapSnapshot;

                if (!CollectionUtils.isEmpty(currentMap)) {
                    // Create snapshot of current map contents
                    mapSnapshot = currentMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> getValueHash(entry.getValue(), dtosVisited)));

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
                if (Collection.class.isAssignableFrom(field.type())) {
                    // Snapshot nested collection
                    final Collection<?> collection = (Collection<?>) getFieldValue(dto, field);
                    final int overallFieldHash = getFieldHash(dto, field, dtosVisited);
                    final List<Integer> listSnapshot;

                    if (!CollectionUtils.isEmpty(collection)) {
                        listSnapshot = collection.stream()
                                .map(item -> getValueHash(item, dtosVisited))
                                .toList();

                        // Track changes to its values if they are nested DTOs
                        final Class<?> genericType = field.genericType();

                        if (!ClassUtils.isBasicType(genericType)) {
                            collection.forEach(trackDtoCallback);
                        }
                    } else {
                        listSnapshot = Collections.emptyList();
                    }

                    fieldSnapshots.add(new FieldSnapshot(field, overallFieldHash, listSnapshot));
                } else {
                    if (!ClassUtils.isBasicType(field.type())) {
                        // Snapshot nested DTO
                        final Object nestedDto = getFieldValue(dto, field);

                        if (nestedDto != null) {
                            trackDtoCallback.accept(nestedDto);
                        }
                    }

                    fieldSnapshots.add(new FieldSnapshot(field, getFieldHash(dto, field, dtosVisited)));
                }
            }
        });

        return fieldSnapshots;
    }

    private int getFieldHash(final Object instance, final FieldAccessor field, final Set<Object> dtosVisited) {
        return getValueHash(getFieldValue(instance, field), dtosVisited);
    }

    private int getValueHash(final @Nullable Object fieldValue, final Set<Object> dtosVisited) {
        if (fieldValue == null) {
            return 0;
        } else if (ClassUtils.isBasicType(fieldValue.getClass())) {
            return fieldValue.hashCode();
        } else if (fieldValue instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return 0;
            } else {
                return collection.stream()
                        .map(item -> getValueHash(item, dtosVisited))
                        .reduce(0, Integer::sum);
            }
        } else if (fieldValue instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                return 0;
            } else {
                return map.entrySet().stream()
                        .map(entry -> getValueHash(entry.getKey(), dtosVisited) + getValueHash(entry.getValue(), dtosVisited))
                        .reduce(0, Integer::sum);
            }
        } else {
            return getDtoHash(fieldValue, dtosVisited);
        }
    }

    private int getDtoHash(final Object dto, final Set<Object> dtosVisited) {
        if (dtosVisited.contains(dto)) {
            LOGGER.trace("Circular reference detected in DTO: {}; returning defaulted hash", dto);
            return 1;
        }

        dtosVisited.add(dto);
        return classFieldAccessorCache.fieldAccessors(dto.getClass()).stream()
                .reduce(1, (hash, field) -> hash + getFieldHash(dto, field, dtosVisited), Integer::sum);
    }

    private static @Nullable Object getFieldValue(final Object instance, final FieldAccessor field) {
        return field.get(instance);
    }
}
