package org.litebridge.core.dto;


import jakarta.annotation.Nullable;
import org.litebridge.commons.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TrackedDto {

    private List<FieldSnapshot> fieldSnapshots;
    private Map<String, ChangedField> changedFields;

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

                // Snapshot nested DTOs
//                final org.litebridge.dto.TrackedField trackedField = field.getAnnotation(TrackedField.class);

//                if (Collection.class.isAssignableFrom(field.getType())) {
//                    final Collection<?> collection = (Collection<?>) getFieldValue(field);
//
//                    if (!CollectionUtils.isEmpty(collection)) {
//                        for (Object listItem : (Collection<?>) getFieldValue(field)) {
//                            if (ChangeTrackingDto.class.isAssignableFrom(listItem.getClass())) {
//                                final ChangeTrackingDto nestedDto = (ChangeTrackingDto) listItem;
//                                nestedDto.snapshot(true);
//                            }
//                        }
//                    }
//                } else if (StringUtils.isBlank(trackedField.dbColumnName())
//                        && ChangeTrackingDto.class.isAssignableFrom(field.getType())) {
//                    final ChangeTrackingDto nestedDto = (ChangeTrackingDto) getFieldValue(field);
//
//                    if (nestedDto != null) {
//                        nestedDto.snapshot();
//                    }
//                }
            }
        });
    }

    public @Nullable Map<String, ChangedField> getChangedFields(Object dto) {
        if (CollectionUtils.isEmpty(changedFields)) {
            if (fieldSnapshots == null) {
                throw new IllegalStateException("Field snapshots not taken for object: " + dto);
            }

            changedFields = fieldSnapshots.stream()
                    .filter(fieldSnapshot -> {
                        final int currentFieldValueHash = getFieldHash(dto, fieldSnapshot.field());
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

//    private void snapshotEmpty() {
//        if (fieldSnapshots != null) {
//            throw new IllegalStateException("Field snapshots already taken for object: " + this);
//        }
//
//        fieldSnapshots = new LinkedList<>();
//        ClassUtil.getAllFields(getClass()).stream()
//                .filter(field -> field.getAnnotation(TrackedField.class) != null)
//                .forEach(field -> fieldSnapshots.add(new org.litebridge.dto.FieldSnapshot(field, 0)));
//    }

//    private static int getChangeTrackingDtoHash(final ChangeTrackingDto changeTrackingDto) {
//        return ClassUtil.getAllFields(changeTrackingDto.getClass()).stream()
//                .filter(field -> field.getAnnotation(TrackedField.class) != null)
//                .reduce(0, (hash, field) -> hash + getFieldHash(changeTrackingDto, field), Integer::sum);
//    }

    private static int getFieldHash(final Object instance, Field field) {
        return getValueHash(getFieldValue(instance, field));
    }

    private static int getValueHash(Object fieldValue) {
        if (fieldValue == null) {
            return 0;
        }
//        else if (ChangeTrackingDto.class.isAssignableFrom(fieldValue.getClass())) {
//            return getChangeTrackingDtoHash((ChangeTrackingDto) fieldValue);
//        }
        else if (fieldValue instanceof Collection) {
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

    private static Object getFieldValue(final Object instance, final Field field) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Failed to access field: %s on object: %s".formatted(field, instance), ex);
        }
    }
}
