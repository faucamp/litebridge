package org.litebridge.tracking;


import jakarta.annotation.Nonnull;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.ObjectUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TrackedDto<T> {

    private final WeakReference<T> dtoRef;
    private final Consumer<Object> trackDtoCallback;
    private List<FieldSnapshot> fieldSnapshots;
    private Map<String, ChangedField> changedFields;

    public TrackedDto(final T dto, final Consumer<Object> trackDtoCallback) {
        this.dtoRef = new WeakReference<>(ObjectUtils.requireNonNull(dto, "DTO cannot be null"));
        this.trackDtoCallback = ObjectUtils.requireNonNull(trackDtoCallback, "No \"track DTO\" callback provided");
    }

    public @Nonnull T getDto() {
        return ObjectUtils.requireNonNull(dtoRef.get(), "DTO object has been garbage collected: " + this);
    }

    public void snapshot(final Object dto, final Set<Field> fields, final boolean overwrite) {
        if (fieldSnapshots != null) {
            if (overwrite) {
                fieldSnapshots.clear();
                changedFields = null;
            } else {
                throw new IllegalStateException("Field snapshots already taken for object: " + this);
            }
        } else {
            fieldSnapshots = new ArrayList<>();
        }

        fields.forEach(field -> {
            // To track changes in a map, we need to snapshot the current map values
            if (Map.class.isAssignableFrom(field.getType())) {
                final Map<?, ?> currentMap = (Map<?, ?>) getFieldValue(dto, field);
                final int overallFieldHash = getFieldHash(dto, field);

                // Create snapshot of current map contents
                final Map<?, Integer> mapSnapshot;

                if (!CollectionUtils.isEmpty(currentMap)) {
                    mapSnapshot = currentMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> getValueHash(entry.getValue())));
                } else {
                    mapSnapshot = Collections.emptyMap();
                }

                fieldSnapshots.add(new FieldSnapshot(field, overallFieldHash, mapSnapshot));
            } else {
                fieldSnapshots.add(new FieldSnapshot(field, getFieldHash(dto, field)));

                if (Collection.class.isAssignableFrom(field.getType())) {
                    // Snapshot nested collection
                    final Collection<?> collection = (Collection<?>) getFieldValue(this, field);

                    throw new UnsupportedOperationException("Collection tracking is not yet implemented");
//                    if (!CollectionUtils.isEmpty(collection)) {
//                        for (Object listItem : (Collection<?>) getFieldValue(field)) {
//                            if (ChangeTrackingDto.class.isAssignableFrom(listItem.getClass())) {
//                                final ChangeTrackingDto nestedDto = (ChangeTrackingDto) listItem;
//                                nestedDto.snapshot(true);
//                            }
//                        }
//                    }
                } else if (!ClassUtils.isBasicType(field.getType())) {
                    // Snapshot nested DTO
                    final Object nestedDto = getFieldValue(dto, field);

                    if (nestedDto != null) {
                        trackDtoCallback.accept(nestedDto);
                    }
                }
            }
        });
    }

    public void snapshotEmpty(final Set<Field> fields) {
        if (fieldSnapshots != null) {
            throw new IllegalStateException("Field snapshots already taken for object: " + this);
        }


        fieldSnapshots = new ArrayList<>();
        fields.forEach(field -> fieldSnapshots.add(new FieldSnapshot(field, 0)));
    }

    public @Nonnull Map<String, ChangedField> getChangedFields() {
        final Object dto = getDto();

        if (CollectionUtils.isEmpty(changedFields)) {
            if (fieldSnapshots == null) {
                throw new IllegalStateException("Field snapshots not taken for object: " + dto);
            }

            changedFields = fieldSnapshots.stream()
                    .filter(fieldSnapshot -> {
                        final int currentFieldValueHash = getFieldHash(dto, fieldSnapshot.field());

                        if (currentFieldValueHash != fieldSnapshot.hash()) {
                            // The value has changed; update internal DTO tracking if required
                            if (currentFieldValueHash != 0 && ClassFieldCache.isNestedDtoField(fieldSnapshot.field())) {
                                // This nested DTO field was null previously, so we need to track the new value
                                final Object nestedDto = getFieldValue(dto, fieldSnapshot.field());
                                trackDtoCallback.accept(nestedDto);
                            }


                        }

                        return currentFieldValueHash != fieldSnapshot.hash();
                    })
                    .map(fieldSnapshot -> new ChangedField(fieldSnapshot.field().getName(), getFieldValue(dto, fieldSnapshot.field()), fieldSnapshot.originalMapSnapshot()))
                    .collect(Collectors.toMap(
                            ChangedField::fieldName,
                            Function.identity(),
                            (oldValue, newValue) -> newValue,
                            LinkedHashMap::new));
        }

        return changedFields;
    }

    private static int getFieldHash(final Object instance, Field field) {
        return getValueHash(getFieldValue(instance, field));
    }

    private static int getValueHash(final Object fieldValue) {
        if (fieldValue == null) {
            return 0;
        } else if (ClassUtils.isBasicType(fieldValue.getClass())) {
            return fieldValue.hashCode();
        } else if (fieldValue instanceof Collection) {
            final Collection<Object> collection = (Collection<Object>) fieldValue;

            if (collection.isEmpty()) {
                return 0;
            } else {
                return collection.stream()
                        .map(TrackedDto::getValueHash)
                        .reduce(0, Integer::sum);
            }
        } else {
            return getDtoHash(fieldValue);
        }
    }

    private static int getDtoHash(final @Nonnull Object dto) {
        return ClassFieldCache.getFields(dto).stream()
                .reduce(0, (hash, field) -> hash + getFieldHash(dto, field), Integer::sum);
    }

    private static Object getFieldValue(final Object instance, final Field field) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Failed to access field: %s on object: %s".formatted(field, instance), ex);
        }
    }
}
