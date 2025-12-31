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

public final class TrackedDto<T> {

    private final WeakReference<T> dtoRef;
    private final Collection<FieldAccessor> fields;
    private final Consumer<Object> trackDtoCallback;
    @Nullable
    private List<FieldSnapshot> fieldSnapshots;
    @Nullable
    private ChangedFields changedFields;

    public TrackedDto(final T dto, final Consumer<Object> trackDtoCallback) {
        this(dto, ClassFieldAccessorCache.fieldAccessors(dto.getClass()), trackDtoCallback);
    }

    public TrackedDto(final T dto, final Collection<FieldAccessor> fields, final Consumer<Object> trackDtoCallback) {
        this.dtoRef = new WeakReference<>(ObjectUtils.requireNonNull(dto, "DTO cannot be null"));
        this.fields = ObjectUtils.requireNonNull(fields, "No tracked fields provided");
        this.trackDtoCallback = ObjectUtils.requireNonNull(trackDtoCallback, "No \"track DTO\" callback provided");
    }

    public T getDto() {
        return ObjectUtils.requireNonNull(dtoRef.get(), "DTO object has been garbage collected: " + this);
    }

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

    public void snapshotEmpty() {
        if (fieldSnapshots != null) {
            throw new IllegalStateException("Field snapshots already taken for object: " + this);
        }

        fieldSnapshots = new ArrayList<>();
        fields.forEach(field -> fieldSnapshots.add(new FieldSnapshot(field, 0)));
    }

    public ChangedFields changedFields() {
        return changedFields(false);
    }

    public ChangedFields changedFields(final boolean refresh) {
        final Object dto = getDto();

        if (refresh && changedFields != null && fieldSnapshots != null) {
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
        final T dto = getDto();
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
