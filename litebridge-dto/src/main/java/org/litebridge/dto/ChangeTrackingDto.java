package org.litebridge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.annotation.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ChangeTrackingDto {

    @JsonIgnore
    private List<FieldSnapshot> fieldSnapshots;
    @JsonIgnore
    private Map<String, ChangedField> changedFields;
    @JsonIgnore
    private static final Map<Class<?>, List<Field>> trackedFieldsPerClass = new ConcurrentHashMap<>();
    @JsonIgnore
    private boolean persisted;

    public void snapshot() {
        snapshot(false);
    }

    public void snapshot(final boolean skipIfSnapshotsExist) {
        if (fieldSnapshots != null) {
            if (skipIfSnapshotsExist) {
                return;
            } else {
                throw new IllegalStateException("Field snapshots already taken for object: " + this);
            }
        }

        persisted = true;
        fieldSnapshots = new LinkedList<>();
        getTrackedFields(getClass())
                .forEach(field -> {
                    // To track changes in a map, we need to snapshot the current map values
                    if (Map.class.isAssignableFrom(field.getType())) {
                        final Map<?, ?> currentMap = (Map<?, ?>) getFieldValue(field);
                        final int overallFieldHash = getValueHash(field);

                        // Create snapshot of current map contents
                        final Map<?, Integer> mapSnapshot;

                        if (!CollectionUtils.isEmpty(currentMap)) {
                            mapSnapshot = currentMap.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> getValueHash(entry.getValue())));
                        } else {
                            mapSnapshot = Collections.emptyMap();
                        }

                        fieldSnapshots.add(new FieldSnapshot(field, getValueHash(field), mapSnapshot));
                    } else {
                        fieldSnapshots.add(new FieldSnapshot(field, getValueHash(field)));

                        // Snapshot nested DTOs
                        final TrackedField trackedField = field.getAnnotation(TrackedField.class);

                        if (Collection.class.isAssignableFrom(field.getType())) {
                            final Collection<?> collection = (Collection<?>) getFieldValue(field);

                            if (!CollectionUtils.isEmpty(collection)) {
                                for (Object listItem : (Collection<?>) getFieldValue(field)) {
                                    if (ChangeTrackingDto.class.isAssignableFrom(listItem.getClass())) {
                                        final ChangeTrackingDto nestedDto = (ChangeTrackingDto) listItem;
                                        nestedDto.snapshot(true);
                                    }
                                }
                            }
                        } else if (StringUtils.isBlank(trackedField.dbColumnName())
                                && ChangeTrackingDto.class.isAssignableFrom(field.getType())) {
                            final ChangeTrackingDto nestedDto = (ChangeTrackingDto) getFieldValue(field);

                            if (nestedDto != null) {
                                nestedDto.snapshot();
                            }
                        }
                    }
                });
    }

    private List<Field> getTrackedFields(final Class<?> clazz) {
        return trackedFieldsPerClass.computeIfAbsent(clazz, key -> ClassUtil.getAllFields(key).stream()
                .filter(field -> field.getAnnotation(TrackedField.class) != null)
                .toList());
    }

    private void snapshotEmpty() {
        if (fieldSnapshots != null) {
            throw new IllegalStateException("Field snapshots already taken for object: " + this);
        }

        fieldSnapshots = new LinkedList<>();
        ClassUtil.getAllFields(getClass()).stream()
                .filter(field -> field.getAnnotation(TrackedField.class) != null)
                .forEach(field -> fieldSnapshots.add(new FieldSnapshot(field, 0)));
    }

    public @Nullable Map<String, ChangedField> getChangedFields() {
        if (changedFields == null) {
            if (fieldSnapshots == null) {
                snapshotEmpty();
            }

            changedFields = fieldSnapshots.stream()
                    .filter(fieldSnapshot -> {
                        final int currentFieldValueHash = getValueHash(fieldSnapshot.field());
                        return currentFieldValueHash != fieldSnapshot.hash();
                    })
                    .map(fieldSnapshot -> new ChangedField(fieldSnapshot.field().getName(), fieldSnapshot.trackedField(), getFieldValue(fieldSnapshot.field()), fieldSnapshot.originalMapSnapshot()))
                    //.collect(Collectors.toMap(ChangedField::fieldName, Function.identity()));
                    .collect(Collectors.toMap(
                            ChangedField::fieldName, // Key mapper: field name
                            Function.identity(), // Value mapper: changed field itself
                            (oldValue, newValue) -> newValue, // Merge function for duplicate keys (overwrite)
                            LinkedHashMap::new // Map supplier: ensures LinkedHashMap is used to preserve order
                    ));
        }

        return changedFields;
    }

    public boolean isPersisted() {
        return persisted;
    }

    private int getValueHash(final Field field) {
        return getValueHash(getFieldValue(field));
    }

    private static int getChangeTrackingDtoHash(final ChangeTrackingDto changeTrackingDto) {
        return ClassUtil.getAllFields(changeTrackingDto.getClass()).stream()
                .filter(field -> field.getAnnotation(TrackedField.class) != null)
                .reduce(0, (hash, field) -> hash + getFieldHash(changeTrackingDto, field), Integer::sum);
    }

    private static int getFieldHash(final Object instance, Field field) {
        return getValueHash(getFieldValue(instance, field));
    }

    private static int getValueHash(Object fieldValue) {
        if (fieldValue == null) {
            return 0;
        } else if (ChangeTrackingDto.class.isAssignableFrom(fieldValue.getClass())) {
            return getChangeTrackingDtoHash((ChangeTrackingDto) fieldValue);
        } else if (fieldValue instanceof Collection) {
            final Collection<Object> collection = (Collection<Object>) fieldValue;

            if (collection.isEmpty()) {
                return 0;
            } else {
                return collection.stream()
                        .map(item -> getValueHash(item))
                        .reduce(0, Integer::sum);
            }
        } else {
            return fieldValue.hashCode();
        }
    }

    private Object getFieldValue(final Field field) {
        return getFieldValue(this, field);
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
